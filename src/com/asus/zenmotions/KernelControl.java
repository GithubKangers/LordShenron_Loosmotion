package com.asus.zenmotions;

import com.asus.zenmotions.util.FileUtils;
import java.io.File;

public final class KernelControl {
    private static String GESTURE_PATH = "/sys/kernel/touchpanel/dclicknode";
    public static final String SLIDER_SWAP_NODE = "/proc/s1302/key_rep";

    private KernelControl() {
    }

    public static void enableGestures(boolean enable) {
        if (new File(GESTURE_PATH).exists()) {
            FileUtils.writeLine(GESTURE_PATH, enable ? "1" : "0");
        }
    }

    public static boolean hasTouchscreenGestures() {
        return new File(GESTURE_PATH).exists();
    }

    public static boolean hasSlider() {
        return new File("/proc/s1302/key_rep").exists();
    }
}
