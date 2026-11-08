package org.mcremote.launcher

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences by lazy { LauncherPreferences(this) }
    private val releases = mutableListOf<VersionManifest.Version>()
    private lateinit var versionsAdapter: ArrayAdapter<String>
    private lateinit var versionSpinner: Spinner
    private lateinit var runtimeSpinner: Spinner
    private lateinit var versionStatus: TextView
    private lateinit var metadataStatus: TextView
    private lateinit var profileSummary: TextView
    private lateinit var profileNameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 25, 20)
        window.navigationBarColor = Color.rgb(16, 25, 20)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(Color.rgb(16, 25, 20))
        }
        val scroll = ScrollView(this).apply { addView(page) }

        page.addView(label("MC JAVA", 13, Color.rgb(133, 216, 157), true))
        page.addView(label("Launcher", 32, Color.WHITE, true), margin(top = 8))
        page.addView(label("Minecraft: Java Edition on Android", 16, Color.rgb(190, 202, 194)), margin(top = 4))

        page.addView(sectionTitle("GAME VERSION"), margin(top = 32))
        versionsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf("Loading versions…")).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        versionSpinner = Spinner(this).apply {
            adapter = versionsAdapter
            setPadding(dp(12), dp(4), dp(12), dp(4))
            setBackgroundColor(Color.rgb(35, 49, 40))
        }
        page.addView(versionSpinner, margin(top = 8))
        versionStatus = label("Fetching official release list…", 13, Color.rgb(173, 187, 177))
        page.addView(versionStatus, margin(top = 8))
        page.addView(button("Refresh versions") { loadVersions() }, margin(top = 8))

        page.addView(sectionTitle("VERSION DETAILS"), margin(top = 24))
        metadataStatus = label("No version metadata inspected yet.", 14, Color.rgb(173, 187, 177))
        page.addView(metadataStatus, margin(top = 8))
        page.addView(button("Inspect selected version") { inspectSelectedVersion() }, margin(top = 8))

        page.addView(sectionTitle("PROFILE"), margin(top = 24))
        profileNameInput = EditText(this).apply {
            setText(preferences.profileName)
            hint = "Profile name"
            inputType = InputType.TYPE_CLASS_TEXT
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(173, 187, 177))
            setBackgroundColor(Color.rgb(35, 49, 40))
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        page.addView(profileNameInput, margin(top = 8))
        profileSummary = label("No profile selected yet", 13, Color.rgb(225, 190, 120))
        page.addView(profileSummary, margin(top = 8))
        page.addView(button("Create profile folder") { createProfileFolder() }, margin(top = 8))

        page.addView(sectionTitle("JAVA RUNTIME"), margin(top = 24))
        val runtimeChoices = listOf("Managed runtime (not installed)", "Java 21", "Java 17", "Java 8")
        runtimeSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, runtimeChoices)
            setSelection(preferences.selectedRuntime.coerceIn(0, runtimeChoices.lastIndex))
            setPadding(dp(12), dp(4), dp(12), dp(4))
            setBackgroundColor(Color.rgb(35, 49, 40))
        }
        page.addView(runtimeSpinner, margin(top = 8))
        page.addView(label("Use a runtime that matches the selected version. Android builds still need a compatible ARM JVM and native launch support.", 14, Color.rgb(173, 187, 177)), margin(top = 8))
        page.addView(button("Runtime setup") {
            Toast.makeText(this, "Android runtime installation is not implemented yet.", Toast.LENGTH_LONG).show()
        }, margin(top = 16))

        page.addView(button("Save profile") {
            val name = profileNameInput.text?.toString()?.trim().orEmpty()
            val newName = if (name.isNotBlank()) name else "Default"
            preferences.profileName = newName
            profileSummary.text = "Profile '${newName}' ready."
            Toast.makeText(this, "Profile saved.", Toast.LENGTH_SHORT).show()
        }, margin(top = 8))

        page.addView(button("Play") {
            val selected = selectedVersionId()
            val runtimeIndex = runtimeSpinner.selectedItemPosition.coerceIn(0, 3)
            preferences.selectedRuntime = runtimeIndex
            preferences.selectedVersion = selected
            val profileDir = preferences.ensureProfileDirectory()
            val summary = "Selected version: $selected • Java ${runtimeLabel(runtimeIndex)} • Profile: ${preferences.profileName} • Directory: ${profileDir.absolutePath}"
            preferences.saveLaunchSummary(summary)
            profileSummary.text = summary
            Toast.makeText(this, "Game launch is not implemented yet.", Toast.LENGTH_LONG).show()
        }, margin(top = 28))

        page.addView(label("Version metadata is fetched from Mojang and its SHA-1 is checked against the official manifest. This verifies download integrity, not app safety. Game files and runtime are not downloaded, and Minecraft does not launch yet.", 13, Color.rgb(225, 190, 120)), margin(top = 12))

        setContentView(scroll)
        loadVersions()
    }

    private fun selectedVersionId(): String =
        if (versionSpinner.selectedItem is String && versionSpinner.selectedItem.toString().startsWith("1.")) {
            versionSpinner.selectedItem.toString()
        } else {
            preferences.selectedVersion
        }

    private fun runtimeLabel(index: Int): String = when (index) {
        0 -> "runtime not installed"
        1 -> "21"
        2 -> "17"
        else -> "8"
    }

    private fun loadVersions() {
        versionStatus.text = "Fetching official release list…"
        worker.execute {
            val result = runCatching { VersionManifest.fetchReleases() }
            mainHandler.post {
                if (isFinishing || isDestroyed) return@post
                result.onSuccess { fetched ->
                    releases.clear()
                    releases.addAll(fetched)
                    val items = releases.map { it.id }
                    versionsAdapter.clear()
                    versionsAdapter.addAll(items)
                    versionsAdapter.notifyDataSetChanged()
                    val preferred = preferences.selectedVersion
                    val selectedIndex = items.indexOf(preferred).coerceAtLeast(0)
                    versionSpinner.setSelection(selectedIndex)
                    versionStatus.text = "${items.size} stable versions loaded from Mojang."
                    profileSummary.text = "Profile '${preferences.profileName}' • Current version: ${items[selectedIndex]}"
                }.onFailure { error ->
                    versionStatus.text = "Could not load versions: ${error.message ?: "network error"}"
                }
            }
        }
    }

    private fun inspectSelectedVersion() {
        val id = selectedVersionId()
        val version = releases.firstOrNull { it.id == id }
        if (version == null) {
            metadataStatus.text = "Version list is not ready. Refresh it and try again."
            return
        }
        metadataStatus.text = "Fetching and verifying Mojang metadata for $id…"
        worker.execute {
            val result = runCatching { VersionManifest.fetchDetails(version) }
            mainHandler.post {
                if (isFinishing || isDestroyed) return@post
                result.onSuccess { details ->
                    val java = details.javaMajorVersion?.toString() ?: "not specified"
                    val jarSize = details.clientJarSize?.let { "${it / (1024 * 1024)} MB" } ?: "not specified"
                    metadataStatus.text = "${details.id} (${details.type})\nJava: $java • Assets: ${details.assets}\nMain class: ${details.mainClass}\nLibraries: ${details.libraryCount} • Client JAR: $jarSize\nMojang metadata SHA-1 verified. No game files downloaded."
                    preferences.saveLaunchSummary(metadataStatus.text.toString())
                }.onFailure { error ->
                    metadataStatus.text = "Could not inspect version: ${error.message ?: "network error"}"
                }
            }
        }
    }

    private fun createProfileFolder() {
        val profileName = profileNameInput.text?.toString()?.trim().ifBlank { preferences.profileName }
        val dir = preferences.ensureProfileDirectory(profileName)
        val summary = "Profile folder ready: ${dir.absolutePath}"
        preferences.profileName = profileName
        profileSummary.text = summary
        Toast.makeText(this, summary, Toast.LENGTH_LONG).show()
    }

    private fun sectionTitle(text: String) = label(text, 12, Color.rgb(133, 216, 157), true)

    private fun label(text: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun margin(top: Int = 0): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(top) }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
