package com.asus.zenmotions.kcal.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class FileUtils {
    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    public static String readOneLine(String fileName) {
        String str;
        StringBuilder stringBuilder;
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName), 512);
            line = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No such file ");
            stringBuilder.append(fileName);
            stringBuilder.append(" for reading");
            Log.w(str, stringBuilder.toString(), e2);
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Could not read from file ");
            stringBuilder.append(fileName);
            Log.e(str, stringBuilder.toString(), e3);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
        }
        return line;
    }

    public static boolean writeLine(String fileName, String value) {
        String str;
        StringBuilder stringBuilder;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(value);
            try {
                writer.close();
            } catch (IOException e) {
            }
            return true;
        } catch (FileNotFoundException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No such file ");
            stringBuilder.append(fileName);
            stringBuilder.append(" for writing");
            Log.w(str, stringBuilder.toString(), e2);
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e3) {
                }
            }
            return false;
        } catch (IOException e4) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Could not write to file ");
            stringBuilder.append(fileName);
            Log.e(str, stringBuilder.toString(), e4);
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e5) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e6) {
                }
            }
        }
    }

    public static boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }

    public static boolean isFileReadable(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.canRead();
    }

    public static boolean isFileWritable(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.canWrite();
    }
}
