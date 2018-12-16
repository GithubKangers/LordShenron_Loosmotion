package com.asus.zenmotions.kcal;

import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Utils {
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

    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public static boolean fileWritable(String filename) {
        return fileExists(filename) && new File(filename).canWrite();
    }

    public static int[] RGBfromK(int temperature) {
        double red;
        double green;
        double blue;
        StringBuilder stringBuilder;
        int[] rgb = new int[3];
        int temperature2 = temperature / 100;
        if (temperature2 <= 66) {
            red = 255.0d;
        } else {
            double red2 = 329.698727446d * Math.pow((double) (temperature2 - 60), -0.1332047592d);
            if (red2 < 0.0d) {
                red = 0.0d;
            } else {
                red = red2;
            }
            if (red > 255.0d) {
                red = 255.0d;
            }
        }
        if (temperature2 <= 66) {
            green = (99.4708025861d * Math.log((double) temperature2)) - 161.1195681661d;
            if (green < 0.0d) {
                green = 0.0d;
            }
            if (green > 255.0d) {
                green = 255.0d;
            }
        } else {
            green = 288.1221695283d * Math.pow((double) (temperature2 - 60), -0.0755148492d);
            if (green < 0.0d) {
                green = 0.0d;
            }
            if (green > 255.0d) {
                green = 255.0d;
            }
        }
        if (temperature2 >= 66) {
            blue = 255.0d;
        } else if (temperature2 <= 19) {
            blue = 0.0d;
        } else {
            double blue2 = (138.5177312231d * Math.log((double) (temperature2 - 10))) - 305.0447927307d;
            if (blue2 < 0.0d) {
                blue2 = 0.0d;
            }
            if (blue2 > 255.0d) {
                blue = 255.0d;
            } else {
                blue = blue2;
            }
            rgb[0] = (int) red;
            rgb[1] = (int) green;
            rgb[2] = (int) blue;
            stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(temperature2);
            stringBuilder.append(" ");
            stringBuilder.append(red);
            stringBuilder.append("  ");
            stringBuilder.append(green);
            stringBuilder.append(" ");
            stringBuilder.append(blue);
            Log.e("RGBfromK", stringBuilder.toString());
            return rgb;
        }
        rgb[0] = (int) red;
        rgb[1] = (int) green;
        rgb[2] = (int) blue;
        stringBuilder = new StringBuilder();
        stringBuilder.append("");
        stringBuilder.append(temperature2);
        stringBuilder.append(" ");
        stringBuilder.append(red);
        stringBuilder.append("  ");
        stringBuilder.append(green);
        stringBuilder.append(" ");
        stringBuilder.append(blue);
        Log.e("RGBfromK", stringBuilder.toString());
        return rgb;
    }

    public static int KfromRGB(double R, double G, double B) {
        double r;
        double b;
        double r2 = R / 255.0d;
        double g = G / 255.0d;
        double b2 = B / 255.0d;
        double Xr = 95.047d;
        double Yr;
        if (r2 > 0.04045d) {
            Yr = 100.0d;
            r = Math.pow((r2 + 0.055d) / 1.055d, 2.4d);
        } else {
            Yr = 100.0d;
            r = r2 / 12.92d;
        }
        if (g > 0.04045d) {
            r2 = Math.pow((g + 0.055d) / 1.055d, 2.4d);
        } else {
            r2 = g / 12.92d;
        }
        if (b2 > 0.04045d) {
            b = Math.pow((b2 + 0.055d) / 1.055d, 2.4d);
        } else {
            b = b2 / 12.92d;
        }
        r *= 100.0d;
        r2 *= 100.0d;
        b *= 100.0d;
        b2 = ((0.4124d * r) + (0.3576d * r2)) + (0.1805d * b);
        g = ((0.2126d * r) + (0.7152d * r2)) + (0.0722d * b);
        double Z = ((0.0193d * r) + (0.1192d * r2)) + (0.9505d * b);
        double x = b2 / ((b2 + g) + Z);
        double y = g / ((b2 + g) + Z);
        double Zr = 108.883d;
        return (int) ((((-449.0d * Math.pow((x - 0.332d) / (y - 0.1858d), 3.0d)) + (3525.0d * Math.pow((x - 0.332d) / (y - 0.1858d), 2.0d))) - (6823.3d * ((x - 0.332d) / (y - 0.1858d)))) + 5520.33d);
    }

    public static double clamp(double x, double min, double max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }
}
