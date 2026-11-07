package org.mcremote.launcher

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

/** First UI slice for an on-device Minecraft: Java Edition launcher. */
class MainActivity : Activity() {
    private val runtimeChoices = listOf("Managed runtime (not installed)", "Java 21", "Java 17", "Java 8")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 25, 20)
        window.navigationBarColor = Color.rgb(16, 25, 20)
        window.decorView.systemUiVisibility = 0

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
        val versionSpinner = spinner(listOf("1.21.4", "1.20.4", "1.19.4", "1.12.2"))
        page.addView(versionSpinner, margin(top = 8))

        page.addView(sectionTitle("JAVA RUNTIME"), margin(top = 24))
        val runtimeSpinner = spinner(runtimeChoices)
        page.addView(runtimeSpinner, margin(top = 8))
        page.addView(label("Modern Minecraft generally needs Java 21 or 17; older releases may need Java 8. Android uses a compatible ARM runtime—not the desktop JDK.", 14, Color.rgb(173, 187, 177)), margin(top = 8))

        val runtimeButton = button("Runtime setup") {
            Toast.makeText(this, "Runtime downloads and installation are not implemented yet.", Toast.LENGTH_LONG).show()
        }
        page.addView(runtimeButton, margin(top = 16))

        page.addView(sectionTitle("PROFILE"), margin(top = 24))
        page.addView(label("Default profile", 16, Color.WHITE), margin(top = 8))
        page.addView(label("Game files and saves stay in this app's private storage.", 14, Color.rgb(173, 187, 177)), margin(top = 4))

        val launchButton = button("Play") {
            val info = runCatching { nativeRuntimeInfo() }.getOrDefault("Native runtime bridge unavailable")
            Toast.makeText(this, "Game launching is not wired up yet. $info", Toast.LENGTH_LONG).show()
        }
        page.addView(launchButton, margin(top = 28))
        page.addView(label("Prototype UI only — no game files, account login, or JVM are bundled.", 13, Color.rgb(225, 190, 120)), margin(top = 12))

        setContentView(scroll)
    }

    private fun sectionTitle(text: String) = label(text, 12, Color.rgb(133, 216, 157), true)

    private fun label(text: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
        setPadding(dp(12), dp(4), dp(12), dp(4))
        setBackgroundColor(Color.rgb(35, 49, 40))
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

    private external fun nativeRuntimeInfo(): String

    companion object {
        init { System.loadLibrary("launcher") }
    }
}
