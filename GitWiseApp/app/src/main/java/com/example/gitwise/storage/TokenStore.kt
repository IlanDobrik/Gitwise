package com.example.gitwise.core.storage

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun loadToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val PREFS = "GitWise"
        private const val KEY_TOKEN = "token"
    }
}
