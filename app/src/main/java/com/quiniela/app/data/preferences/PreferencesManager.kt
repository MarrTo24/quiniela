package com.quiniela.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("quiniela_prefs", Context.MODE_PRIVATE)

    // PIN del administrador — por defecto 1234
    var adminPin: String
        get() = prefs.getString(KEY_ADMIN_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        set(value) = prefs.edit().putString(KEY_ADMIN_PIN, value).apply()

    // Usuario recordado para saltar la pantalla de selección
    var rememberedUserId: String?
        get() = prefs.getString(KEY_REMEMBERED_USER, null)
        set(value) {
            if (value != null) prefs.edit().putString(KEY_REMEMBERED_USER, value).apply()
            else prefs.edit().remove(KEY_REMEMBERED_USER).apply()
        }

    fun clearRememberedUser() {
        prefs.edit().remove(KEY_REMEMBERED_USER).apply()
    }

    companion object {
        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val KEY_REMEMBERED_USER = "remembered_user_id"
        const val DEFAULT_PIN = "2406"
    }
}
