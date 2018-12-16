package com.asus.zenmotions.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import java.net.URISyntaxException;

public class AppHelper {
    private static final String SETTINGS_METADATA_NAME = "com.android.settings";

    public static String getProperSummary(Context context, PackageManager pm, Resources settingsResources, String action, String values, String entries) {
        if (!(pm == null || settingsResources == null)) {
            if (action != null) {
                if (!(values == null || entries == null)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("com.android.settings:array/");
                    stringBuilder.append(entries);
                    int resIdEntries = settingsResources.getIdentifier(stringBuilder.toString(), null, null);
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("com.android.settings:array/");
                    stringBuilder.append(values);
                    int resIdValues = settingsResources.getIdentifier(stringBuilder.toString(), null, null);
                    if (resIdEntries > 0 && resIdValues > 0) {
                        try {
                            String[] entriesArray = settingsResources.getStringArray(resIdEntries);
                            String[] valuesArray = settingsResources.getStringArray(resIdValues);
                            for (int i = 0; i < valuesArray.length; i++) {
                                if (action.equals(valuesArray[i])) {
                                    return entriesArray[i];
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return getFriendlyNameForUri(context, pm, action);
            }
        }
        return context.getResources().getString(17039842);
    }

    public static String getFriendlyActivityName(Context context, PackageManager pm, Intent intent, boolean labelOnly) {
        ActivityInfo ai = intent.resolveActivityInfo(pm, 1);
        String friendlyName = null;
        if (ai != null) {
            friendlyName = ai.loadLabel(pm).toString();
            if (friendlyName == null && !labelOnly) {
                friendlyName = ai.name;
            }
        }
        if (friendlyName != null) {
            if (!friendlyName.startsWith("#Intent;")) {
                String toUri;
                if (friendlyName == null) {
                    if (!labelOnly) {
                        toUri = intent.toUri(0);
                        return toUri;
                    }
                }
                toUri = friendlyName;
                return toUri;
            }
        }
        return context.getResources().getString(17039842);
    }

    public static String getFriendlyShortcutName(Context context, PackageManager pm, Intent intent) {
        String activityName = getFriendlyActivityName(context, pm, intent, true);
        String name = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        if (activityName != null) {
            if (!activityName.startsWith("#Intent;")) {
                if (activityName == null || name == null) {
                    return name != null ? name : intent.toUri(0);
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(activityName);
                stringBuilder.append(": ");
                stringBuilder.append(name);
                return stringBuilder.toString();
            }
        }
        return context.getResources().getString(17039842);
    }

    public static String getFriendlyNameForUri(Context context, PackageManager pm, String uri) {
        if (uri != null) {
            if (!uri.startsWith("**")) {
                try {
                    Intent intent = Intent.parseUri(uri, 0);
                    if ("android.intent.action.MAIN".equals(intent.getAction())) {
                        return getFriendlyActivityName(context, pm, intent, false);
                    }
                    return getFriendlyShortcutName(context, pm, intent);
                } catch (URISyntaxException e) {
                    return uri;
                }
            }
        }
        return null;
    }

    public static String getShortcutPreferred(Context context, PackageManager pm, String uri) {
        if (uri != null) {
            if (!uri.startsWith("**")) {
                try {
                    Intent intent = Intent.parseUri(uri, 0);
                    String name = intent.getStringExtra("android.intent.extra.shortcut.NAME");
                    if (name != null) {
                        if (!name.startsWith("#Intent;")) {
                            return name;
                        }
                    }
                    return getFriendlyActivityName(context, pm, intent, false);
                } catch (URISyntaxException e) {
                    return uri;
                }
            }
        }
        return null;
    }
}
