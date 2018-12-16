package com.asus.zenmotions.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import com.asus.zenmotions.R;
import java.util.ArrayList;

public class ShortcutPickerHelper {
    public static final int REQUEST_CREATE_SHORTCUT = 102;
    public static final int REQUEST_PICK_APPLICATION = 101;
    public static final int REQUEST_PICK_SHORTCUT = 100;
    private int lastFragmentId;
    private OnPickListener mListener;
    private PackageManager mPackageManager = this.mParent.getPackageManager();
    private Activity mParent;

    public interface OnPickListener {
        void shortcutPicked(String str, String str2, Bitmap bitmap, boolean z);
    }

    public ShortcutPickerHelper(Activity parent, OnPickListener listener) {
        this.mParent = parent;
        this.mListener = listener;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case 100:
                    processShortcut(data, 101, 102);
                    return;
                case 101:
                    completeSetCustomApp(data);
                    return;
                case 102:
                    completeSetCustomShortcut(data);
                    return;
                default:
                    return;
            }
        }
    }

    public void pickShortcut(int fragmentId) {
        pickShortcut(fragmentId, false);
    }

    public void pickShortcut(int fragmentId, boolean fullAppsOnly) {
        this.lastFragmentId = fragmentId;
        if (fullAppsOnly) {
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            Intent pickIntent = new Intent("android.intent.action.PICK_ACTIVITY");
            pickIntent.putExtra("android.intent.extra.INTENT", mainIntent);
            startFragmentOrActivity(pickIntent, 101);
            return;
        }
        Bundle bundle = new Bundle();
        ArrayList<String> shortcutNames = new ArrayList();
        shortcutNames.add(this.mParent.getString(R.string.group_applications));
        bundle.putStringArrayList("android.intent.extra.shortcut.NAME", shortcutNames);
        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList();
        shortcutIcons.add(ShortcutIconResource.fromContext(this.mParent, 17301651));
        bundle.putParcelableArrayList("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIcons);
        Intent pickIntent2 = new Intent("android.intent.action.PICK_ACTIVITY");
        pickIntent2.putExtra("android.intent.extra.INTENT", new Intent("android.intent.action.CREATE_SHORTCUT"));
        pickIntent2.putExtra("android.intent.extra.TITLE", this.mParent.getText(R.string.select_custom_app_title));
        pickIntent2.putExtras(bundle);
        startFragmentOrActivity(pickIntent2, 100);
    }

    private void startFragmentOrActivity(Intent pickIntent, int requestCode) {
        if (this.lastFragmentId == 0) {
            this.mParent.startActivityForResult(pickIntent, requestCode);
            return;
        }
        Fragment cFrag = this.mParent.getFragmentManager().findFragmentById(this.lastFragmentId);
        if (cFrag != null) {
            this.mParent.startActivityFromFragment(cFrag, pickIntent, requestCode);
        }
    }

    private void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
        String applicationName = this.mParent.getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        if (applicationName == null || !applicationName.equals(shortcutName)) {
            startFragmentOrActivity(intent, requestCodeShortcut);
            return;
        }
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        Intent pickIntent = new Intent("android.intent.action.PICK_ACTIVITY");
        pickIntent.putExtra("android.intent.extra.INTENT", mainIntent);
        startFragmentOrActivity(pickIntent, requestCodeApplication);
    }

    private void completeSetCustomApp(Intent data) {
        this.mListener.shortcutPicked(data.toUri(0), AppHelper.getFriendlyActivityName(this.mParent, this.mPackageManager, data, false), null, true);
    }

    private void completeSetCustomShortcut(Intent data) {
        Intent intent = (Intent) data.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        intent.putExtra("android.intent.extra.shortcut.NAME", data.getStringExtra("android.intent.extra.shortcut.NAME"));
        String appUri = intent.toUri(0).replaceAll("com.android.contacts.action.QUICK_CONTACT", "android.intent.action.VIEW");
        Bitmap bmp = null;
        Parcelable extra = data.getParcelableExtra("android.intent.extra.shortcut.ICON");
        if (extra != null && (extra instanceof Bitmap)) {
            bmp = (Bitmap) extra;
        }
        if (bmp == null) {
            extra = data.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
            if (extra != null && (extra instanceof ShortcutIconResource)) {
                try {
                    ShortcutIconResource iconResource = (ShortcutIconResource) extra;
                    Resources resources = this.mPackageManager.getResourcesForApplication(iconResource.packageName);
                    bmp = BitmapFactory.decodeResource(resources, resources.getIdentifier(iconResource.resourceName, null, null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.mListener.shortcutPicked(appUri, AppHelper.getFriendlyShortcutName(this.mParent, this.mPackageManager, intent), bmp, false);
    }
}
