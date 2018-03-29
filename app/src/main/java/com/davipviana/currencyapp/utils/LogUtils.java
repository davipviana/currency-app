package com.davipviana.currencyapp.utils;

public class LogUtils {

    private static StringBuffer stringBuffer = new StringBuffer();

    public interface LogListener {
        void onLogged(StringBuffer log);
    }

    private static LogListener logListener;

    public static void log(String tag, String message) {

    }
}
