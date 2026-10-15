package com.example.tipshare.testutil

import android.content.SharedPreferences

/**
 * Clean in-memory implementation of Android's SharedPreferences for unit tests.
 */
class FakeSharedPreferences : SharedPreferences {

    private val storage = mutableMapOf<String, Any?>()
    private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): MutableMap<String, *> = HashMap(storage)

    override fun getString(key: String?, defValue: String?): String? {
        return (storage[key] as? String) ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return (storage[key] as? MutableSet<String>) ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return (storage[key] as? Int) ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return (storage[key] as? Long) ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return (storage[key] as? Float) ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return (storage[key] as? Boolean) ?: defValue
    }

    override fun contains(key: String?): Boolean = storage.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        if (listener != null) listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        listeners.remove(listener)
    }

    private inner class FakeEditor : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()
        private val removeKeys = mutableSetOf<String>()
        private var shouldClear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            if (key != null) temp[key] = values
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key != null) temp[key] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) removeKeys.add(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            shouldClear = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (shouldClear) {
                storage.clear()
            }
            removeKeys.forEach { storage.remove(it) }
            storage.putAll(temp)
        }
    }
}
