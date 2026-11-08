package org.mcremote.launcher

import android.content.Context
import java.io.File

class LauncherPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("mc_launcher_state", Context.MODE_PRIVATE)
    private val filesDir = context.filesDir

    var selectedVersion: String
        get() = prefs.getString("selected_version", "1.21.4") ?: "1.21.4"
        set(value) = prefs.edit().putString("selected_version", value).apply()

    var selectedRuntime: Int
        get() = prefs.getInt("selected_runtime", 1)
        set(value) = prefs.edit().putInt("selected_runtime", value).apply()

    var profileName: String
        get() = prefs.getString("profile_name", "Default") ?: "Default"
        set(value) = prefs.edit().putString("profile_name", value).apply()

    var lastVersionSummary: String
        get() = prefs.getString("last_version_summary", "") ?: ""
        set(value) = prefs.edit().putString("last_version_summary", value).apply()

    fun profileDirectory(profile: String = profileName): File {
        val safeName = profile.replace(Regex("[^A-Za-z0-9._-]+"), "_").ifBlank { "Default" }
        return File(File(filesDir, "profiles"), safeName)
    }

    fun ensureProfileDirectory(profile: String = profileName): File {
        val dir = profileDirectory(profile)
        dir.mkdirs()
        return dir
    }

    fun saveLaunchSummary(summary: String) {
        lastVersionSummary = summary
    }
}
