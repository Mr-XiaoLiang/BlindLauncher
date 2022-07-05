package com.lollipop.blindlauncher.utils

import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

class LSettings(
    private val context: Context
) {

    private val preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    var isOpenVoice by def(true)

    var isFirstLaunch  by def(true)

    fun def(def: Boolean): BooleanDelegate {
        return BooleanDelegate(preferences, def)
    }

    fun def(def: String): StringDelegate {
        return StringDelegate(preferences, def)
    }

    class BooleanDelegate(
        private val preferences: SharedPreferences,
        private val def: Boolean
    ) {
        operator fun setValue(
            target: Any,
            property: KProperty<*>,
            value: Boolean
        ) {
            preferences.edit().putBoolean(property.name, value).apply()
        }

        operator fun getValue(
            target: Any,
            property: KProperty<*>
        ): Boolean {
            return preferences.getBoolean(property.name, def)
        }
    }

    class StringDelegate(
        private val preferences: SharedPreferences,
        private val def: String
    ) {
        operator fun setValue(
            target: Any,
            property: KProperty<*>,
            value: String
        ) {
            preferences.edit().putString(property.name, value).apply()
        }

        operator fun getValue(
            target: Any,
            property: KProperty<*>
        ): String {
            return preferences.getString(property.name, def) ?: def
        }
    }

}