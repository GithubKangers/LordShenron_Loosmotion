package com.asus.zenmotions.util;

import android.app.ActivityManagerNative;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.media.session.MediaSessionLegacyHelper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import java.net.URISyntaxException;

public class Action {
    private static final int MSG_INJECT_KEY_DOWN = 1066;
    private static final int MSG_INJECT_KEY_UP = 1067;
    private static boolean sTorchEnabled = false;

    public static void processAction(Context context, String action, boolean isLongpress) {
        processActionWithOptions(context, action, isLongpress, true);
    }

    public static void processActionWithOptions(Context context, String action, boolean isLongpress, boolean collapseShade) {
        if (action != null) {
            if (!action.equals(ActionConstants.ACTION_NULL)) {
                int intent = 0;
                boolean isKeyguardShowing = false;
                try {
                    isKeyguardShowing = WindowManagerGlobal.getWindowManagerService().isKeyguardLocked();
                } catch (RemoteException e) {
                    Log.w("Action", "Error getting window manager service", e);
                }
                if (action.equals(ActionConstants.ACTION_HOME)) {
                    triggerVirtualKeypress(3, isLongpress);
                } else if (action.equals(ActionConstants.ACTION_BACK)) {
                    triggerVirtualKeypress(4, isLongpress);
                } else if (action.equals(ActionConstants.ACTION_SEARCH)) {
                    triggerVirtualKeypress(84, isLongpress);
                } else {
                    if (!action.equals(ActionConstants.ACTION_MENU)) {
                        if (!action.equals(ActionConstants.ACTION_MENU_BIG)) {
                            AudioManager am;
                            if (action.equals(ActionConstants.ACTION_IME_NAVIGATION_LEFT)) {
                                triggerVirtualKeypress(21, isLongpress);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_IME_NAVIGATION_RIGHT)) {
                                triggerVirtualKeypress(22, isLongpress);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_IME_NAVIGATION_UP)) {
                                triggerVirtualKeypress(19, isLongpress);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_IME_NAVIGATION_DOWN)) {
                                triggerVirtualKeypress(20, isLongpress);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_TORCH)) {
                                try {
                                    CameraManager cameraManager = (CameraManager) context.getSystemService("camera");
                                    String[] cameraIdList = cameraManager.getCameraIdList();
                                    int length = cameraIdList.length;
                                    while (intent < length) {
                                        String cameraId = cameraIdList[intent];
                                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                                        Boolean flashAvailable = (Boolean) characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                                        int orient = ((Integer) characteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
                                        if (flashAvailable != null && flashAvailable.booleanValue() && orient == 1) {
                                            cameraManager.setTorchMode(cameraId, sTorchEnabled ^ 1);
                                            sTorchEnabled ^= 1;
                                            break;
                                        }
                                        intent++;
                                    }
                                } catch (CameraAccessException e2) {
                                }
                                return;
                            } else if (action.equals(ActionConstants.ACTION_POWER)) {
                                ((PowerManager) context.getSystemService("power")).goToSleep(SystemClock.uptimeMillis());
                                return;
                            } else if (action.equals(ActionConstants.ACTION_IME)) {
                                if (!isKeyguardShowing) {
                                    context.sendBroadcastAsUser(new Intent("android.settings.SHOW_INPUT_METHOD_PICKER"), new UserHandle(-2));
                                    return;
                                }
                                return;
                            } else if (action.equals(ActionConstants.ACTION_VOICE_SEARCH)) {
                                Intent intent2 = new Intent("android.intent.action.SEARCH_LONG_PRESS");
                                intent2.setFlags(268435456);
                                try {
                                    SearchManager searchManager = (SearchManager) context.getSystemService("search");
                                    if (searchManager != null) {
                                        searchManager.stopSearch();
                                    }
                                    startActivity(context, intent2);
                                } catch (ActivityNotFoundException e3) {
                                    Log.e("aospActions:", "No activity to handle assist long press action.", e3);
                                }
                                return;
                            } else if (action.equals(ActionConstants.ACTION_VIB)) {
                                AudioManager am2 = (AudioManager) context.getSystemService("audio");
                                if (am2 != null && ActivityManagerNative.isSystemReady()) {
                                    if (am2.getRingerMode() != 1) {
                                        am2.setRingerMode(1);
                                        Vibrator vib = (Vibrator) context.getSystemService("vibrator");
                                        if (vib != null) {
                                            vib.vibrate(50);
                                        }
                                    } else {
                                        am2.setRingerMode(2);
                                        new ToneGenerator(5, 85).startTone(24);
                                    }
                                }
                                return;
                            } else if (action.equals(ActionConstants.ACTION_SILENT)) {
                                am = (AudioManager) context.getSystemService("audio");
                                if (am != null && ActivityManagerNative.isSystemReady()) {
                                    if (am.getRingerMode() != 0) {
                                        am.setRingerMode(0);
                                    } else {
                                        am.setRingerMode(2);
                                        new ToneGenerator(5, 85).startTone(24);
                                    }
                                }
                                return;
                            } else if (action.equals(ActionConstants.ACTION_VIB_SILENT)) {
                                am = (AudioManager) context.getSystemService("audio");
                                if (am != null && ActivityManagerNative.isSystemReady()) {
                                    if (am.getRingerMode() == 2) {
                                        am.setRingerMode(1);
                                        Vibrator vib2 = (Vibrator) context.getSystemService("vibrator");
                                        if (vib2 != null) {
                                            vib2.vibrate(50);
                                        }
                                    } else if (am.getRingerMode() == 1) {
                                        am.setRingerMode(0);
                                    } else {
                                        am.setRingerMode(2);
                                        new ToneGenerator(5, 85).startTone(24);
                                    }
                                }
                                return;
                            } else if (action.equals(ActionConstants.ACTION_CAMERA)) {
                                startActivity(context, new Intent("android.media.action.STILL_IMAGE_CAMERA", null));
                                return;
                            } else if (action.equals(ActionConstants.ACTION_MEDIA_PREVIOUS)) {
                                dispatchMediaKeyWithWakeLock(88, context);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_MEDIA_NEXT)) {
                                dispatchMediaKeyWithWakeLock(87, context);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_MEDIA_PLAY_PAUSE)) {
                                dispatchMediaKeyWithWakeLock(85, context);
                                return;
                            } else if (action.equals(ActionConstants.ACTION_WAKE_DEVICE)) {
                                PowerManager powerManager = (PowerManager) context.getSystemService("power");
                                if (!powerManager.isScreenOn()) {
                                    powerManager.wakeUp(SystemClock.uptimeMillis());
                                }
                                return;
                            } else {
                                Intent intent3 = null;
                                try {
                                    startActivity(context, Intent.parseUri(action, 0));
                                    return;
                                } catch (URISyntaxException e4) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("URISyntaxException: [");
                                    stringBuilder.append(action);
                                    stringBuilder.append("]");
                                    Log.e("aospActions:", stringBuilder.toString());
                                    return;
                                }
                            }
                        }
                    }
                    triggerVirtualKeypress(82, isLongpress);
                }
            }
        }
    }

    public static boolean isActionKeyEvent(String action) {
        if (!(action.equals(ActionConstants.ACTION_HOME) || action.equals(ActionConstants.ACTION_BACK) || action.equals(ActionConstants.ACTION_SEARCH) || action.equals(ActionConstants.ACTION_MENU) || action.equals(ActionConstants.ACTION_MENU_BIG))) {
            if (!action.equals(ActionConstants.ACTION_NULL)) {
                return false;
            }
        }
        return true;
    }

    private static void startActivity(Context context, Intent intent) {
        if (intent != null) {
            intent.addFlags(872415232);
            context.startActivityAsUser(intent, new UserHandle(-2));
        }
    }

    private static void dispatchMediaKeyWithWakeLock(int keycode, Context context) {
        if (ActivityManagerNative.isSystemReady()) {
            KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 0, keycode, 0);
            MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(keyEvent, true);
            MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(KeyEvent.changeAction(keyEvent, 1), true);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0029  */
    public static void triggerVirtualKeypress(int r22, boolean r23) {
        /*
        r13 = r22;
        r14 = android.hardware.input.InputManager.getInstance();
        r15 = android.os.SystemClock.uptimeMillis();
        r0 = 0;
        r1 = 0;
        r2 = 21;
        if (r13 == r2) goto L_0x0022;
    L_0x0010:
        r2 = 22;
        if (r13 == r2) goto L_0x0022;
    L_0x0014:
        r2 = 19;
        if (r13 == r2) goto L_0x0022;
    L_0x0018:
        r2 = 20;
        if (r13 != r2) goto L_0x001d;
    L_0x001c:
        goto L_0x0022;
    L_0x001d:
        r2 = 72;
        r1 = r2;
        r0 = r2;
        goto L_0x0025;
    L_0x0022:
        r2 = 6;
        r1 = r2;
        r0 = r2;
    L_0x0025:
        r17 = r1;
        if (r23 == 0) goto L_0x002b;
    L_0x0029:
        r0 = r0 | 128;
    L_0x002b:
        r18 = r0;
        r19 = new android.view.KeyEvent;
        r5 = 0;
        r7 = 0;
        r8 = 0;
        r9 = -1;
        r10 = 0;
        r12 = 257; // 0x101 float:3.6E-43 double:1.27E-321;
        r0 = r19;
        r1 = r15;
        r3 = r15;
        r6 = r13;
        r11 = r18;
        r0.<init>(r1, r3, r5, r6, r7, r8, r9, r10, r11, r12);
        r12 = r19;
        r11 = 0;
        r14.injectInputEvent(r12, r11);
        r19 = new android.view.KeyEvent;
        r5 = 1;
        r20 = 257; // 0x101 float:3.6E-43 double:1.27E-321;
        r0 = r19;
        r11 = r17;
        r21 = r12;
        r12 = r20;
        r0.<init>(r1, r3, r5, r6, r7, r8, r9, r10, r11, r12);
        r1 = 0;
        r14.injectInputEvent(r0, r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.asus.zenmotions.util.Action.triggerVirtualKeypress(int, boolean):void");
    }
}
