package com.theshire.app

import android.content.Context
import android.content.SharedPreferences

object PhotoPreferences {
    
    private const val PREF_NAME = "photo_jardin"
    private const val KEY_PHOTO_URI = "photo_uri"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun sauvegarderPhoto(context: Context, uri: String) {
        getPreferences(context).edit().putString(KEY_PHOTO_URI, uri).apply()
    }
    
    fun getPhoto(context: Context): String? {
        return getPreferences(context).getString(KEY_PHOTO_URI, null)
    }
    
    fun supprimerPhoto(context: Context) {
        getPreferences(context).edit().remove(KEY_PHOTO_URI).apply()
    }
}
