package com.davipviana.currencyapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.davipviana.currencyapp.Constants;
import com.davipviana.currencyapp.helpers.CurrencyParserHelper;
import com.davipviana.currencyapp.utils.LogUtils;
import com.davipviana.currencyapp.utils.WebServiceUtils;
import com.davipviana.currencyapp.value_objects.Currency;

import org.json.JSONObject;

public class CurrencyService extends IntentService {
    public static final String TAG = CurrencyService.class.getName();

    public CurrencyService(String name) {
        super(TAG);
    }

    public CurrencyService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.log(TAG, "Currency Service has started");
        Bundle intentBundle = intent.getBundleExtra(Constants.BUNDLE);
        final ResultReceiver receiver = intentBundle.getParcelable(Constants.RECEIVER);
        Parcel parcel = Parcel.obtain();
        receiver.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending =
                android.os.ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        String url = intentBundle.getString(Constants.URL);
        String currencyName = intentBundle.getString(Constants.CURRENCY_NAME);

        Bundle bundle = new Bundle();
        if(url != null && !TextUtils.isEmpty(url)) {
            receiverForSending.send(Constants.STATUS_RUNNING, Bundle.EMPTY);
            if(WebServiceUtils.hasInternetConnection(getApplicationContext())) {
                try {
                    JSONObject obj = WebServiceUtils.requestJSONObject(url);
                    if(obj != null) {
                        Currency currency = CurrencyParserHelper.parseCurrency(obj, currencyName);
                        bundle.putParcelable(Constants.RESULT, currency);
                        receiverForSending.send(Constants.STATUS_FINISHED, bundle);
                    }
                } catch(Exception  e) {
                    bundle.putString(Intent.EXTRA_TEXT, e.toString());
                    receiverForSending.send(Constants.STATUS_ERROR, bundle);
                }
            } else {
                LogUtils.log(TAG, "No internet connection");
            }
        }
        LogUtils.log(TAG, "Currency Service has stopped");
        stopSelf();
    }
}
