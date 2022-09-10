package com.tutor.noviolence.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager {
    private val TAG = SessionManager::class.java.simpleName

    var sharedPref : SharedPreferences

    var editor : SharedPreferences.Editor
    var context : Context

    //Shared pref mode
    val PRIVATE_MODE = 0

    // Shared preferences file name
    private val PREF_NAME = "NoViolenceLogin"

    private val KEY_IS_LOGGEDIN = "isLoggedIn"

    constructor(context: Context){
        this.context = context
        sharedPref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = sharedPref.edit()
    }

    public fun setLogin(isLoggedIn : Boolean){
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn)

        editor.commit()
        Log.d(TAG, "User login session modified!");
    }

    public fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean(KEY_IS_LOGGEDIN, false)
    }

}
