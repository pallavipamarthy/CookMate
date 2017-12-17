package com.axiom.cookmate.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountUtils {
    public static final String ACC_NAME_PREFERENCE = "acc_name_pref";
    public static final String ACC_NAME = "acc_name";
    public static final String ACC_PIC_PREFERENCE = "acc_pic_pref";
    public static final String ACC_PIC = "acc_pic";
    public static final String ACC_EMAIL_PREFERENCE = "acc_email_pref";
    public static final String ACC_EMAIL = "acc_email";
    public static final String ACC_LOGIN_CHECK_PREF = "login_check_pref";
    public static final String ACC_LOGIN_CHECK = "login_check";
    public static final String FIREBASE_USER_ID_PREFERENCE = "firebase_user_id_pref";
    public static final String FIREBASE_USER_ID = "firebase_user_id";

    public static void saveAccountName(Context context, String name) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_NAME_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACC_NAME, name);
        editor.commit();
    }

    public static void saveAccountPhoto(Context context, String photo) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_PIC_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACC_PIC, photo);
        editor.commit();
    }

    public static void saveAccountEmail(Context context, String email) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_EMAIL_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACC_EMAIL, email);
        editor.commit();
    }

    public static String getAccountName(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_NAME_PREFERENCE, Context.MODE_PRIVATE);
        String accountName = (String) sharedPref.getString(ACC_NAME, null);
        return accountName;
    }

    public static String getAccountPhoto(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_PIC_PREFERENCE, Context.MODE_PRIVATE);
        String accountPhoto = (String) sharedPref.getString(ACC_PIC, null);
        return accountPhoto;
    }

    public static String getAccountEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_EMAIL_PREFERENCE, Context.MODE_PRIVATE);
        String accountEmail = (String) sharedPref.getString(ACC_EMAIL, null);
        return accountEmail;
    }

    public static void removeAccountName(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_NAME_PREFERENCE, Context.MODE_PRIVATE);
        String accountName = (String) sharedPref.getString(ACC_NAME, null);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(accountName);
        editor.commit();
    }

    public static void removeAccountPhoto(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_PIC_PREFERENCE, Context.MODE_PRIVATE);
        String accountPhoto = (String) sharedPref.getString(ACC_PIC, null);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(accountPhoto);
        editor.commit();
    }

    public static void removeAccountEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_EMAIL_PREFERENCE, Context.MODE_PRIVATE);
        String accountEmail = (String) sharedPref.getString(ACC_EMAIL, null);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(accountEmail);
        editor.commit();
    }

    public static void userLoginCheck(Context context, boolean login) {
        SharedPreferences prefs = context.getSharedPreferences(ACC_LOGIN_CHECK_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ACC_LOGIN_CHECK, login);
        editor.commit();
    }

    public static boolean getUserLogin(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(ACC_LOGIN_CHECK_PREF, Context.MODE_PRIVATE);
        Boolean loginCheck = (Boolean) sharedPref.getBoolean(ACC_LOGIN_CHECK, false);
        return loginCheck;
    }

    public static void saveFirebaseUserId(Context context, String id) {
        SharedPreferences sharedPref = context.getSharedPreferences(FIREBASE_USER_ID_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(FIREBASE_USER_ID, id);
        editor.commit();
    }

    public static String getFirebaseUserId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FIREBASE_USER_ID_PREFERENCE, Context.MODE_PRIVATE);
        String userId = (String) sharedPref.getString(FIREBASE_USER_ID, null);
        return userId;
    }

    public static void removeFirebaseUserId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FIREBASE_USER_ID_PREFERENCE, Context.MODE_PRIVATE);
        String userId = (String) sharedPref.getString(FIREBASE_USER_ID, null);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(userId);
        editor.commit();
    }
}
