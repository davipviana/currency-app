package com.davipviana.currencyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.davipviana.currencyapp.Constants;

public class SharedPreferencesUtils {

    public static String getCurrency(Context context, boolean isBaseCurrency) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.CURRENCY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(
                isBaseCurrency ? Constants.BASE_CURRENCY : Constants.TARGET_CURRENCY,
                isBaseCurrency ? "USD" : "AUD");
    }

    public static void updateCurrency(Context context, String currency, boolean isBaseCurrency) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.CURRENCY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(isBaseCurrency ? Constants.BASE_CURRENCY : Constants.TARGET_CURRENCY, currency);
        editor.commit();
    }

    public static int getServiceRepetition(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.CURRENCY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SERVICE_REPETITION, 0);
    }

    public static void updateServiceRepetition(Context context, int serviceRepetition) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.CURRENCY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SERVICE_REPETITION, serviceRepetition);
        editor.commit();
    }

    public static int getNumDownloads(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.CURRENCY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.NUM_DOWNLOADS, 0);
    }

    public static void updateNumDownloads(Context context, int numDownloads) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.CURRENCY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.NUM_DOWNLOADS, numDownloads);
        editor.commit();
    }
















}
