package com.davipviana.currencyapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.davipviana.currencyapp.Constants;
import com.davipviana.currencyapp.utils.LogUtils;
import com.davipviana.currencyapp.value_objects.Currency;

import java.util.ArrayList;

public class CurrencyTableHelper {

    public static final String TAG = CurrencyTableHelper.class.getName();

    private CurrencyDatabaseAdapter currencyDatabaseAdapter;

    public CurrencyTableHelper(CurrencyDatabaseAdapter adapter) {
        this.currencyDatabaseAdapter = adapter;
    }

    public long insertCurrency(Currency currency) {
        ArrayList<Currency> currencies = getCurrencyHistory(currency.getBase(), currency.getName(), currency.getDate());
        if(currencies.size() == 0) {
            LogUtils.log(TAG, "No records found in DB");

            ContentValues initialValues = new ContentValues();
            initialValues.put(Constants.KEY_BASE, currency.getBase());
            initialValues.put(Constants.KEY_DATE, currency.getDate());
            initialValues.put(Constants.KEY_RATE, currency.getRate());
            initialValues.put(Constants.KEY_NAME, currency.getName());
            long id = currencyDatabaseAdapter.getWritableDatabase().insert(
                    Constants.CURRENCY_TABLE, null, initialValues);
            currencyDatabaseAdapter.getWritableDatabase().close();
            return id;
        } else {
            LogUtils.log(TAG, "Found record on DB");

        }
        return currencies.get(0).getId();
    }

    public ArrayList<Currency> getCurrencyHistory(String base, String name, String date) {
        ArrayList<Currency> currencies = new ArrayList<>();
        Cursor cursor = currencyDatabaseAdapter.getWritableDatabase().query(
                Constants.CURRENCY_TABLE,
                new String[] { Constants.KEY_ID, Constants.KEY_BASE, Constants. KEY_DATE,
                        Constants.KEY_RATE, Constants.KEY_NAME},
                Constants.KEY_BASE + " = '" + base + "' AND " + Constants.KEY_NAME + " = " +
                        "'" + name + "' AND " + Constants.KEY_DATE + " = '" + date + "'",
                null, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                currencies.add(parseCurrency(cursor));
            }
            while(cursor.moveToNext()) {
                currencies.add(parseCurrency(cursor));
            }
        }

        return currencies;
    }

    public ArrayList<Currency> getCurrencyHistory(String base, String name) {
        ArrayList<Currency> currencies = new ArrayList<>();
        Cursor cursor = currencyDatabaseAdapter.getWritableDatabase().query(
                Constants.CURRENCY_TABLE,
                new String[] { Constants.KEY_ID, Constants.KEY_BASE, Constants. KEY_DATE,
                        Constants.KEY_RATE, Constants.KEY_NAME},
                Constants.KEY_BASE + " = '" + base + "' AND " + Constants.KEY_NAME + " = " +
                        "'" + name + "'",
                null, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                currencies.add(parseCurrency(cursor));
            }
            while(cursor.moveToNext()) {
                currencies.add(parseCurrency(cursor));
            }
        }

        return currencies;
    }

    public Currency getCurrency(long id) throws SQLException {
        Cursor cursor = currencyDatabaseAdapter.getWritableDatabase().query(
                Constants.CURRENCY_TABLE,
                new String[] { Constants.KEY_ID, Constants.KEY_BASE, Constants. KEY_DATE,
                        Constants.KEY_RATE, Constants.KEY_NAME},
                Constants.KEY_ID + " = " + id, null, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                return parseCurrency(cursor);
            }
        }
        return null;
    }

    private Currency parseCurrency(Cursor cursor) {

        Currency currency = new Currency();
        currency.setId(cursor.getLong(cursor.getColumnIndex(Constants.KEY_ID)));
        currency.setBase(cursor.getString(cursor.getColumnIndex(Constants.KEY_BASE)));
        currency.setName(cursor.getString(cursor.getColumnIndex(Constants.KEY_NAME)));
        currency.setRate(cursor.getDouble(cursor.getColumnIndex(Constants.KEY_RATE)));
        currency.setDate(cursor.getString(cursor.getColumnIndex(Constants.KEY_DATE)));

        return currency;
    }

    public void clearCurrencyTable() {
        currencyDatabaseAdapter.getWritableDatabase().delete(Constants.CURRENCY_TABLE, null, null);
    }
}
