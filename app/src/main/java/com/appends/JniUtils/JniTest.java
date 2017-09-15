package com.appends.JniUtils;

/**
 * Created by Administrator on 2017/9/2 0002.
 */

public class JniTest {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String stringComeFromJNI();
}
