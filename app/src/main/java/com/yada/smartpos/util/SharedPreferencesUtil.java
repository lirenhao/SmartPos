package com.yada.smartpos.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by YJF on 2015/8/11 0011.
 * SharedPreferences数据存储类
 */
public class SharedPreferencesUtil {
	public static String filename = "params";

	public static boolean setIntParam(Context context, String key, int value) {

		try {
			SharedPreferences preferences = context.getSharedPreferences(filename, context.MODE_PRIVATE);
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
			SharedPreferences preferences = context.getSharedPreferences(filename, context.MODE_PRIVATE);

			return preferences.getInt(key, 0);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static boolean setStringParam(Context context, String key, String value) {

		try {
			SharedPreferences preferences = context.getSharedPreferences(filename, context.MODE_PRIVATE);
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
			SharedPreferences preferences = context.getSharedPreferences(filename, context.MODE_PRIVATE);

			return preferences.getString(key, "");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
