package com.example.gitwise.config

import android.content.Context
import com.example.gitwise.datatypes.Person
import com.google.gson.Gson


private const val PREF_NAME = "config"
private const val KEY_CONFIG = "config"


data class Config(
    val person: Person?,
    val commit: Boolean,
    val branchName: String?,
)

val DEFAULT_CONFIG = Config(null, false, null)

fun saveConfig(context: Context, config: Config) {
    val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val configJson = Gson().toJson(config)

    with (sharedPref.edit()) {
        putString(KEY_CONFIG, configJson)
        apply()
    }
}

fun getConfig(context: Context): Config {
    val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val configJson = sharedPref.getString(KEY_CONFIG, null)

    val config = try {
        Gson().fromJson(configJson, Config::class.java) ?: DEFAULT_CONFIG
    } catch (e: Exception) {
        DEFAULT_CONFIG
    }

    return config
}

fun clearConfig(context: Context) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .clear()
        .apply()
}