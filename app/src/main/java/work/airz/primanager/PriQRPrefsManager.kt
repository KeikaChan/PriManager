package work.airz.primanager

import android.content.Context
import android.preference.PreferenceManager

class PriQRPrefsManager(val context: Context) {
    companion object {
        private const val UPDATE_LIST_KEY = "key.updateList"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun putIsUpdate(isUpdate: Boolean) {
        prefs.edit().apply {
            putBoolean(UPDATE_LIST_KEY, isUpdate)
        }.commit()
    }

    fun getIsUpdate(): Boolean {
        return prefs.getBoolean(UPDATE_LIST_KEY, false)
    }
}