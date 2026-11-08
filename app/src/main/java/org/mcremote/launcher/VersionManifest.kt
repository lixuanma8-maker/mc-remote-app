package org.mcremote.launcher

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Reads official public version metadata; downloads no game files. */
object VersionManifest {
    private const val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val MAX_METADATA_BYTES = 10 * 1024 * 1024
    private val allowedMetadataHosts = setOf("piston-meta.mojang.com", "launchermeta.mojang.com")

    data class Version(val id: String, val type: String, val url: String, val sha1: String)

    data class VersionDetails(
        val id: String,
        val type: String,
        val mainClass: String,
        val assets: String,
        val javaMajorVersion: Int?,
        val clientJarSize: Long?,
        val clientDownloadUrl: String?,
        val clientSha1: String?,
        val libraryCount: Int
    )

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
                val sha1 = item.optString("sha1")
                if (id.isBlank() || !sha1.matches(Regex("[0-9a-fA-F]{40}"))) continue
                if (!isTrustedMetadataUrl(metadataUrl)) continue
                releases += Version(id, "release", metadataUrl, sha1.lowercase())
                if (releases.size == 100) break
            }
            check(releases.isNotEmpty()) { "The version manifest contained no releases" }
            return releases
        } finally {
            connection.disconnect()
        }
    }

    /** Fetches Mojang's version JSON and checks its SHA-1 against the official manifest entry. */
    fun fetchDetails(version: Version): VersionDetails {
        require(isTrustedMetadataUrl(version.url)) { "Untrusted metadata URL" }
        val connection = (URL(version.url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "MCJavaMobileLauncher/0.1")
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "Metadata service returned HTTP $code" }
            val bytes = connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= MAX_METADATA_BYTES) { "Version metadata is unexpectedly large" }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            val actualSha1 = sha1(bytes)
            require(actualSha1.equals(version.sha1, ignoreCase = true)) {
                "Metadata checksum did not match Mojang's version manifest"
            }

            val json = JSONObject(bytes.toString(Charsets.UTF_8))
            require(json.optString("id") == version.id) { "Metadata version ID did not match the selection" }
            val javaMajor = json.optJSONObject("javaVersion")?.optInt("majorVersion")?.takeIf { it > 0 }
            val client = json.optJSONObject("downloads")?.optJSONObject("client")
            val clientUrl = client?.optString("url")?.takeIf { it.isNotBlank() }
            val clientSha1 = client?.optString("sha1")?.takeIf { it.matches(Regex("[0-9a-fA-F]{40}")) }
            val clientSize = client?.optLong("size")?.takeIf { it > 0 }

            return VersionDetails(
                id = json.optString("id"),
                type = json.optString("type", version.type),
                mainClass = json.optString("mainClass", "Not specified"),
                assets = json.optString("assets", "Not specified"),
                javaMajorVersion = javaMajor,
                clientJarSize = clientSize,
                clientDownloadUrl = clientUrl,
                clientSha1 = clientSha1?.lowercase(),
                libraryCount = json.optJSONArray("libraries")?.length() ?: 0
            )
        } finally {
            connection.disconnect()
        }
    }

    fun sha1(bytes: ByteArray): String = MessageDigest.getInstance("SHA-1")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    private fun isTrustedMetadataUrl(value: String): Boolean = runCatching {
        val url = URL(value)
        url.protocol == "https" && url.host.lowercase() in allowedMetadataHosts
    }.getOrDefault(false)
}
