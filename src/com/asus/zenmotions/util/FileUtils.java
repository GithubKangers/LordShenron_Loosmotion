package com.asus.zenmotions.util;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public final class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    public static String readOneLine(String fileName) {
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName), 512);
            line = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not read from file ");
            stringBuilder.append(fileName);
            Log.e(str, stringBuilder.toString(), e2);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                }
            }
        }
        return line;
    }

    public static boolean writeLine(String fileName, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not write to file ");
            stringBuilder.append(fileName);
            Log.e(str, stringBuilder.toString(), e);
            return false;
        }
    }
}
