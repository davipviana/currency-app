package com.davipviana.currencyapp.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class CurrencyReceiver extends ResultReceiver {

    private Receiver receiver;

    public CurrencyReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver newReceiver) {
        this.receiver = newReceiver;
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }
}
