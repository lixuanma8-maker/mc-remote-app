package org.mcremote.launcher

import android.content.Context

class LauncherPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("mc_launcher_state", Context.MODE_PRIVATE)

    var selectedVersion: String
        get() = prefs.getString("selected_version", "1.21.4") ?: "1.21.4"
        set(value) = prefs.edit().putString("selected_version", value).apply()

    var selectedRuntime: Int
        get() = prefs.getInt("selected_runtime", 1)
        set(value) = prefs.edit().putInt("selected_runtime", value).apply()

    var profileName: String
        get() = prefs.getString("profile_name", "Default") ?: "Default"
        set(value) = prefs.edit().putString("profile_name", value).apply()
}
