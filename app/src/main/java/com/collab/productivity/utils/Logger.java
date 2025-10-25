package com.collab.productivity.utils;

import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {
    private static final String APP_TAG = "Notionary";
    private static File logFile;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    public static void init(File filesDir) {
        try {
            logFile = new File(filesDir, "app_log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(APP_TAG, "Failed to create log file", e);
        }
    }

    public static void d(String tag, String message) {
        Log.d(APP_TAG + ":" + tag, message);
        writeToFile("DEBUG", tag, message);
    }

    public static void e(String tag, String message, Throwable e) {
        Log.e(APP_TAG + ":" + tag, message, e);
        writeToFile("ERROR", tag, message + "\n" + Log.getStackTraceString(e));
    }

    public static void i(String tag, String message) {
        Log.i(APP_TAG + ":" + tag, message);
        writeToFile("INFO", tag, message);
    }

    private static synchronized void writeToFile(String level, String tag, String message) {
        if (logFile == null) return;

        try {
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(String.format("%s %s/%s: %s\n",
                dateFormat.format(new Date()),
                level,
                tag,
                message));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(APP_TAG, "Failed to write to log file", e);
        }
    }

    public static File getLogFile() {
        return logFile;
    }
}
