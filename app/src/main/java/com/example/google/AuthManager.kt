package com.example.google

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class AuthManager(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val isLoggedIn: Boolean
        get() = auth.currentUser != null && prefs.getBoolean("isLoggedIn", false)

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", value).apply()
    }
}