package com.davipviana.currencyapp;

import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.davipviana.currencyapp.adapters.CurrencyAdapter;
import com.davipviana.currencyapp.database.CurrencyDatabaseAdapter;
import com.davipviana.currencyapp.database.CurrencyTableHelper;
import com.davipviana.currencyapp.receivers.CurrencyReceiver;
import com.davipviana.currencyapp.services.CurrencyService;
import com.davipviana.currencyapp.utils.AlarmUtils;
import com.davipviana.currencyapp.utils.LogUtils;
import com.davipviana.currencyapp.utils.NotificationUtils;
import com.davipviana.currencyapp.utils.SharedPreferencesUtils;
import com.davipviana.currencyapp.value_objects.Currency;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements CurrencyReceiver.Receiver {

    private String baseCurrency = Constants.CURRENCY_CODES[30];
    private String targetCurrency = Constants.CURRENCY_CODES[0];
    private CurrencyTableHelper currencyTableHelper;

    private static final String TAG = MainActivity.class.getName();
    private int serviceRepetition = AlarmUtils.REPEAT.REPEAT_EVERY_MINUTE.ordinal();

    private CoordinatorLayout logLayout;
    private FloatingActionButton floatingActionButton;

    private boolean isLogVisible = true;
    private boolean isFabVisible = true;

    private ListView baseCurrencyList;
    private ListView targetCurrencyList;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resetDownloads();
        initCurrencies();
        initDB();
        initToolbar();
        initSpinner();
        initCurrencyList();
        initLineChart();
        addActionButtonListener();
        showLogs();

        logLayout = (CoordinatorLayout) findViewById(R.id.log_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceRepetition = SharedPreferencesUtils.getServiceRepetition(this);
        retrieveCurrencyExchangeRate();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.setLogListener(null);
    }

    @Override
    public void onReceiveResult(int resultCode, final Bundle resultData) {
        switch(resultCode) {
            case Constants.STATUS_RUNNING:
                LogUtils.log(TAG, "Currency Service Running!");
                break;

            case Constants.STATUS_FINISHED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Currency currencyParcel = resultData.getParcelable(Constants.RESULT);
                        if(currencyParcel != null) {
                            String message = "Currency: " + currencyParcel.getBase() + " - " +
                                    currencyParcel.getName() + ": " + currencyParcel.getRate();
                            LogUtils.log(TAG, message);
                            long id = currencyTableHelper.insertCurrency(currencyParcel);
                            Currency currency = currencyParcel;
                            try {
                                currency = currencyTableHelper.getCurrency(id);
                            } catch(SQLException e) {
                                e.printStackTrace();
                                LogUtils.log(TAG, "Currency retrieval has failed");
                            }
                            if(currency != null) {
                                String dbMessage = "Currency (DB): " + currency.getBase() + " - " +
                                        currency.getName() + ": " + currency.getRate();
                                LogUtils.log(TAG, dbMessage);
                                NotificationUtils.showNotificationMessage(getApplicationContext(),
                                        "Currency Exchange Rate", dbMessage);
                            }

                            if(NotificationUtils.isAppInBackground(MainActivity.this)) {
                                int numDownloads = SharedPreferencesUtils.getNumDownloads(getApplicationContext());
                                SharedPreferencesUtils.updateNumDownloads(getApplicationContext(), ++numDownloads);
                                if(numDownloads == Constants.MAX_DOWNLOADS) {
                                    LogUtils.log(TAG, "Max downloads for the background processing has been reached.");
                                    serviceRepetition = AlarmUtils.REPEAT.REPEAT_EVERY_DAY.ordinal();
                                    retrieveCurrencyExchangeRate();
                                }
                            } else {
                                updateLineChart();
                            }
                        }
                    }
                });
                break;

            case Constants.STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                LogUtils.log(TAG, error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    private void initDB() {
        CurrencyDatabaseAdapter currencyDatabaseAdapter = new CurrencyDatabaseAdapter(this);
        currencyTableHelper = new CurrencyTableHelper(currencyDatabaseAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    private void initSpinner() {
        final Spinner spinner = (Spinner) findViewById(R.id.time_frequency);
        spinner.setSaveEnabled(true);
        spinner.setSelection(SharedPreferencesUtils.getServiceRepetition(this), false);
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SharedPreferencesUtils.updateServiceRepetition(MainActivity.this, position);
                        serviceRepetition = position;
                        if (position >= AlarmUtils.REPEAT.values().length) {
                            AlarmUtils.stopService();
                        } else {
                            retrieveCurrencyExchangeRate();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    private void initCurrencyList() {
        baseCurrencyList = (ListView) findViewById(R.id.base_currency_list);
        targetCurrencyList = (ListView) findViewById(R.id.target_currency_list);

        CurrencyAdapter baseCurrencyAdapter = new CurrencyAdapter(this);
        CurrencyAdapter targetCurrencyAdapter = new CurrencyAdapter(this);

        baseCurrencyList.setAdapter(baseCurrencyAdapter);
        targetCurrencyList.setAdapter(targetCurrencyAdapter);

        int baseCurrencyIndex = retrieveIndexOf(baseCurrency);
        int targetCurrencyIndex = retrieveIndexOf(targetCurrency);

        baseCurrencyList.setItemChecked(baseCurrencyIndex, true);
        targetCurrencyList.setItemChecked(targetCurrencyIndex, true);

        baseCurrencyList.setSelection(baseCurrencyIndex);
        targetCurrencyList.setSelection(targetCurrencyIndex);

        addCurrencySelectionListener();
    }

    private int retrieveIndexOf(String currency) {
        return Arrays.asList(Constants.CURRENCY_CODES).indexOf(currency);
    }

    private void addCurrencySelectionListener() {
        baseCurrencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                baseCurrency = Constants.CURRENCY_CODES[position];
                LogUtils.log(TAG, "Base Currency has changed to: " + baseCurrency);
                SharedPreferencesUtils.updateCurrency(MainActivity.this, baseCurrency, true);
                retrieveCurrencyExchangeRate();
            }
        });

        targetCurrencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                targetCurrency = Constants.CURRENCY_CODES[position];
                LogUtils.log(TAG, "Target Currency has changed to: " + targetCurrency);
                SharedPreferencesUtils.updateCurrency(MainActivity.this, targetCurrency, false);
                retrieveCurrencyExchangeRate();
            }
        });
    }

    private void initLineChart() {
        lineChart = (LineChart) findViewById(R.id.line_chart);
        lineChart .setNoDataText("No Data");
        lineChart .setHighlightEnabled(true);
        lineChart .setTouchEnabled(true);
        lineChart .setDragEnabled(true);
        lineChart .setScaleEnabled(true);
        lineChart .setDrawGridBackground(false);
        lineChart .setPinchZoom(true);

        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.BLUE);
        lineChart .setData(lineData);

        Legend legend = lineChart .getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(ColorTemplate.getHoloBlue());

        XAxis xAxis = lineChart .getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis yAxis = lineChart .getAxisLeft();
        yAxis.setTextColor(Color.BLACK);
        yAxis.setAxisMaxValue(120f);
        yAxis.setDrawGridLines(true);

        YAxis yAxisRight = lineChart .getAxisRight();
        yAxisRight.setEnabled(false);
    }

    private void updateLineChart() {
        lineChart .setDescription("Currency Exchange Rate: " + baseCurrency + " - " + targetCurrency);
        ArrayList<Currency> currencies = currencyTableHelper.getCurrencyHistory(baseCurrency, targetCurrency);
        LineData lineData = lineChart.getData();
        lineData.clearValues();
        for(Currency currency : currencies) {
            addChartEntry(currency.getDate(), currency.getRate());
        }
    }

    private void addChartEntry(String date, double value) {
        LineData lineData = lineChart.getData();
        if(lineData != null) {
            LineDataSet lineDataSet = lineData.getDataSetByIndex(0);
            if(lineDataSet == null) {
                lineDataSet = createSet();
                lineData.addDataSet(lineDataSet);
            }

            if(!lineChart.getData().getXVals().contains(date)) {
                lineData.addXValue(date);
            }
            lineData.addEntry(new Entry((float) value, lineDataSet.getEntryCount()), 0);
            lineChart.notifyDataSetChanged();
        }
    }

    private LineDataSet createSet() {
        LineDataSet lineDataSet = new LineDataSet(null, "Value");
        lineDataSet.setDrawCubic(true);
        lineDataSet.setCubicIntensity(0.2f);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ColorTemplate.getHoloBlue());
        lineDataSet.setCircleColor(ColorTemplate.getHoloBlue());
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleSize(4f);
        lineDataSet.setFillAlpha(65);
        lineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        lineDataSet.setHighLightColor(Color.CYAN);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(10f);

        return lineDataSet;
    }

    private void initCurrencies() {
        baseCurrency = SharedPreferencesUtils.getCurrency(this, true);
        targetCurrency = SharedPreferencesUtils.getCurrency(this, false);
    }

    private void showLogs() {
        final TextView logText = (TextView) findViewById(R.id.log_text);
        LogUtils.setLogListener(new LogUtils.LogListener() {
            @Override
            public void onLogged(final StringBuffer log) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText(log.toString());
                        logText.invalidate();

                    }
                });
            }
        });
    }

    private void retrieveCurrencyExchangeRate() {
        if(serviceRepetition < AlarmUtils.REPEAT.values().length) {
            CurrencyReceiver receiver = new CurrencyReceiver(new Handler());
            receiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), CurrencyService.class);
            intent.setExtrasClassLoader(CurrencyService.class.getClassLoader());

            Bundle bundle = new Bundle();
            String url = Constants.CURRENCY_URL + baseCurrency;
            bundle.putString(Constants.URL, url);
            bundle.putParcelable(Constants.RECEIVER, receiver);
            bundle.putInt(Constants.REQUEST_ID, Constants.REQUEST_ID_NUM);
            bundle.putString(Constants.CURRENCY_NAME, targetCurrency);
            bundle.putString(Constants.CURRENCY_BASE, baseCurrency);
            intent.putExtra(Constants.BUNDLE, bundle);
//        startService(intent);
            AlarmUtils.startService(this, intent,
                    AlarmUtils.REPEAT.values()[serviceRepetition]);
        }
    }

    private void resetDownloads() {
        SharedPreferencesUtils.updateNumDownloads(this, 0);
    }

    private void addActionButtonListener() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, floatingActionButton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.clear_database:
                                currencyTableHelper.clearCurrencyTable();
                                LogUtils.log(TAG, "Currency Database has been cleared.");
                                lineChart.clearValues();
                                updateLineChart();
                                break;
                            case R.id.graph:
                                findViewById(R.id.currency_list_layout).setVisibility(View.GONE);
                                lineChart.setVisibility(View.VISIBLE);
                                updateLineChart();
                                break;
                            case R.id.selection:
                                findViewById(R.id.currency_list_layout).setVisibility(View.VISIBLE);
                                lineChart.setVisibility(View.GONE);
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_clear_logs:
                LogUtils.clearLogs();
                return true;

            case R.id.action_show_logs:
                isLogVisible = !isLogVisible;
                item.setIcon(isLogVisible ? R.drawable.ic_keyboard_hide : R.drawable.ic_keyboard);
                logLayout.setVisibility(isLogVisible ? View.VISIBLE : View.GONE);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}