package com.tutor.noviolence.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.tutor.noviolence.models.UserModel;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "my_shared_pref";

    private static SharedPrefManager mInstance;
    private Context mCtx;

    private SharedPrefManager(Context ctx){
        mCtx = ctx;
    }

    public static synchronized SharedPrefManager getInstance(Context ctx){
        if(mInstance == null){
            mInstance = new SharedPrefManager(ctx);
        }
        return mInstance;
    }

    public void saveUser(UserModel user){
        SharedPreferences sharedPref = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("id", user.getId());
        editor.putString("email", user.getEmail());
        editor.putString("username", user.getUsername());

        editor.apply();

    }
    public boolean isLoggedIn(){
        SharedPreferences sharedPref = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if(sharedPref.getInt("id", -1) != -1){
            return true;
        }
        return false;
    }
    public UserModel getUser(){
        SharedPreferences sharedPref = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        UserModel user = new UserModel(
                sharedPref.getInt("id", -1),
                sharedPref.getString("email", null),
                sharedPref.getString("username", null)
        );

        return user;
    }

    public void clear(){
        SharedPreferences sharedPref = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }
}
