package com.davipviana.currencyapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.davipviana.currencyapp.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WebServiceUtils {

    public static final String TAG = WebServiceUtils.class.getName();

    public static JSONObject requestJSONObject(String serviceURL) {
        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(serviceURL);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);

            int statusCode = urlConnection.getResponseCode();
            if(statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                LogUtils.log(TAG, "Unauthorized access!");
            } else if(statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LogUtils.log(TAG, "404 page not found");
            } else if(statusCode != HttpURLConnection.HTTP_OK) {
                LogUtils.log(TAG, "URL Response error");
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(in));
        } catch(MalformedURLException e) {
            LogUtils.log(TAG, e.getMessage());
        } catch(SocketTimeoutException e) {
            LogUtils.log(TAG, e.getMessage());
        } catch(IOException e) {
            LogUtils.log(TAG, e.getMessage());
        } catch(JSONException e) {
            LogUtils.log(TAG, e.getMessage());
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    private static String convertInputStreamToString(InputStream inStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
        String responseText;
        try {
            while((responseText = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseText);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));

        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
