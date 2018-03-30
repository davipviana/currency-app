package com.davipviana.currencyapp.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogUtils {

    private static StringBuffer stringBuffer = new StringBuffer();

    public interface LogListener {
        void onLogged(StringBuffer log);
    }

    private static LogListener logListener;

    public static void log(String tag, String message) {
        Log.d(tag, message);
        StringBuilder stb = new StringBuilder();
        String date = formatDate(Calendar.getInstance());
        stb.append(date);
        stb.append(" ");
        stb.append(tag);
        stb.append(" ");
        stb.append(message);
        stb.append("\n\n");
        stringBuffer.insert(0, stb.toString());
        printLogs();
    }

    private static String formatDate(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd hh:mm:ss");
        return simpleDateFormat.format(calendar.getTime());
    }

    private static void printLogs() {
        if(logListener != null) {
            logListener.onLogged(stringBuffer);
        }
    }

    public static void clearLogs() {
        stringBuffer = new StringBuffer();
        printLogs();
    }

    public static void setLogListener(LogListener newLogListener) {
        logListener = newLogListener;
    }
}
