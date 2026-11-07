package org.mcremote.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Reads official public version metadata; downloads no game files. */
object VersionManifest {
    private const val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    data class Version(val id: String, val type: String, val url: String)

    fun fetchReleases(): List<Version> {
        val connection = (URL(MANIFEST_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "MCJavaMobileLauncher/0.1")
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "Version service returned HTTP $code" }
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(body)
            val versions = root.getJSONArray("versions")
            val releases = ArrayList<Version>(minOf(versions.length(), 100))
            for (index in 0 until versions.length()) {
                val item = versions.getJSONObject(index)
                if (item.optString("type") != "release") continue
                val id = item.optString("id")
                val metadataUrl = item.optString("url")
                if (id.isBlank() || metadataUrl.isBlank()) continue
                // Do not accept arbitrary URLs from a response before any future metadata fetch.
                val host = runCatching { URL(metadataUrl).host }.getOrNull()
                if (host != "piston-meta.mojang.com" && host != "launchermeta.mojang.com") continue
                releases += Version(id, "release", metadataUrl)
                if (releases.size == 100) break
            }
            check(releases.isNotEmpty()) { "The version manifest contained no releases" }
            return releases
        } finally {
            connection.disconnect()
        }
    }
}
