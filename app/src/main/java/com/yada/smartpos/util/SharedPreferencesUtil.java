package com.yada.smartpos.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * SharedPreferences数据存储类
 */
public class SharedPreferencesUtil {
    private static String filename = "params";

    public static boolean setIntParam(Context context, String key, int value) {

        try {
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);

            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getIntParam(Context context, String key) {

        try {
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

            return preferences.getInt(key, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean setStringParam(Context context, String key, String value) {

        try {
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);

            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getStringParam(Context context, String key) {

        try {
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

            return preferences.getString(key, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean setReverseParam(Context context, String reverse) {
        try {
            Set<String> set = getReverseParam(context);
            set.add(reverse);
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet("reverse", set);
            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Set<String> getReverseParam(Context context) {
        Set<String> set = new HashSet<>();
        try {
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            return preferences.getStringSet("reverse", set);
        } catch (Exception e) {
            e.printStackTrace();
            return set;
        }
    }

    public static boolean remove(Context context, String key) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(key);
            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
