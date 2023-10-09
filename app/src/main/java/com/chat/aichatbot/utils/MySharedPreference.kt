package com.chat.aichatbot.utils

import android.content.Context
import android.text.TextUtils

class MySharedPreference(context: Context) {
    private val appSharedPrefs = context.getSharedPreferences("have", Context.MODE_PRIVATE)

    fun setPreferences(key: String?, value: String?) {
        val prefsEditor = appSharedPrefs?.edit()
        prefsEditor?.putString(key, value)
        prefsEditor?.apply()
    }

    fun getPreferences(key: String?): String? {
        val json = appSharedPrefs?.getString(key, "")
        return if (TextUtils.isEmpty(json)) {
            ""
        } else json
    }

    fun setIntPreference(key: String, value: Int) {
        val prefsEditor = appSharedPrefs?.edit()
        prefsEditor?.putInt(key, value)
        prefsEditor?.apply()
    }

    fun getIntPreference(key: String): Int {
        return appSharedPrefs?.getInt(key, -1) ?: -1
    }

    fun setBoolPreference(key: String, value: Boolean?) {
        val prefsEditor = appSharedPrefs?.edit()
        prefsEditor?.putBoolean(key, value!!)
        prefsEditor?.apply()
    }

    fun getBoolPreference(key: String): Boolean {
        return appSharedPrefs?.getBoolean(key, false) ?: false
    }
}