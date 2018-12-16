package com.asus.zenmotions.util;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.view.DisplayInfo;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.List;

public class DeviceUtils {
    private static final int DEVICE_HYBRID = 1;
    private static final int DEVICE_PHONE = 0;
    private static final int DEVICE_TABLET = 2;
    private static final String SETTINGS_METADATA_NAME = "com.android.settings";

    public static class FilteredDeviceFeaturesArray {
        public String[] entries;
        public String[] values;
    }

    public static boolean deviceSupportsRemoteDisplay(Context ctx) {
        return ((DisplayManager) ctx.getSystemService("display")).getWifiDisplayStatus().getFeatureState() != 0;
    }

    public static boolean deviceSupportsUsbTether(Context context) {
        return ((ConnectivityManager) context.getSystemService("connectivity")).getTetherableUsbRegexs().length != 0;
    }

    public static boolean deviceSupportsMobileData(Context context) {
        return ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
    }

    public static boolean deviceSupportsBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean deviceSupportsNfc(Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }

    public static boolean deviceSupportsLte(Context context) {
        return ((TelephonyManager) context.getSystemService("phone")).getLteOnCdmaMode() == 1;
    }

    public static boolean deviceSupportsGps(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.location.gps");
    }

    public static boolean adbEnabled(ContentResolver resolver) {
        return Global.getInt(resolver, "adb_enabled", 0) == 1;
    }

    public static boolean deviceSupportsVibrator(Context ctx) {
        return ((Vibrator) ctx.getSystemService("vibrator")).hasVibrator();
    }

    public static boolean deviceSupportsTorch(Context context) {
        return true;
    }

    public static FilteredDeviceFeaturesArray filterUnsupportedDeviceFeatures(Context context, String[] valuesArray, String[] entriesArray) {
        if (!(valuesArray == null || entriesArray == null)) {
            if (context != null) {
                List<String> finalEntries = new ArrayList();
                List<String> finalValues = new ArrayList();
                FilteredDeviceFeaturesArray filteredDeviceFeaturesArray = new FilteredDeviceFeaturesArray();
                for (int i = 0; i < valuesArray.length; i++) {
                    if (isSupportedFeature(context, valuesArray[i])) {
                        finalEntries.add(entriesArray[i]);
                        finalValues.add(valuesArray[i]);
                    }
                }
                filteredDeviceFeaturesArray.entries = (String[]) finalEntries.toArray(new String[finalEntries.size()]);
                filteredDeviceFeaturesArray.values = (String[]) finalValues.toArray(new String[finalValues.size()]);
                return filteredDeviceFeaturesArray;
            }
        }
        return null;
    }

    private static boolean isSupportedFeature(Context context, String action) {
        if ((!action.equals(ActionConstants.ACTION_TORCH) || deviceSupportsTorch(context)) && ((!action.equals(ActionConstants.ACTION_VIB) || deviceSupportsVibrator(context)) && (!action.equals(ActionConstants.ACTION_VIB_SILENT) || deviceSupportsVibrator(context)))) {
            return true;
        }
        return false;
    }

    private static int getScreenType(Context con) {
        WindowManager wm = (WindowManager) con.getSystemService("window");
        DisplayInfo outDisplayInfo = new DisplayInfo();
        wm.getDefaultDisplay().getDisplayInfo(outDisplayInfo);
        int shortSizeDp = (Math.min(outDisplayInfo.logicalHeight, outDisplayInfo.logicalWidth) * 160) / outDisplayInfo.logicalDensityDpi;
        if (shortSizeDp < 600) {
            return 0;
        }
        if (shortSizeDp < 720) {
            return 1;
        }
        return 2;
    }

    public static boolean isPhone(Context con) {
        return getScreenType(con) == 0;
    }

    public static boolean isHybrid(Context con) {
        return getScreenType(con) == 1;
    }

    public static boolean isTablet(Context con) {
        return getScreenType(con) == 2;
    }
}
