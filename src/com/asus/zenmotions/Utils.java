package com.asus.zenmotions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Utils {
    public static void writeValue(String filename, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static void writeColor(String filename, int value) {
        writeValue(filename, String.valueOf(((long) value) * 2));
    }

    public static void writeGamma(String filename, int value) {
        writeValue(filename, String.valueOf(value));
    }

    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public static boolean fileWritable(String filename) {
        return fileExists(filename) && new File(filename).canWrite();
    }

    public static String readLine(String filename) {
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(filename), 1024);
            String line2 = br.readLine();
            try {
                br.close();
            } catch (IOException e) {
            }
            return line2;
        } catch (IOException e2) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e4) {
                }
            }
        }
    }

    public static boolean getFileValueAsBoolean(String filename, boolean defValue) {
        String fileValue = readLine(filename);
        if (fileValue != null) {
            return fileValue.equals("0") ^ 1;
        }
        return defValue;
    }

    public static String getFileValue(String filename, String defValue) {
        String fileValue = readLine(filename);
        if (fileValue != null) {
            return fileValue;
        }
        return defValue;
    }
}
