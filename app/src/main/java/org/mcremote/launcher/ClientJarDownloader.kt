package org.mcremote.launcher

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/** Downloads only Mojang's official client JAR into app-private storage and verifies its SHA-1. */
object ClientJarDownloader {
    private const val MAX_CLIENT_BYTES = 1024L * 1024L * 1024L
    private const val REDIRECT_LIMIT = 5
    private val allowedHosts = setOf("piston-data.mojang.com", "launcher.mojang.com")

    data class Result(val file: File, val bytes: Long, val sha1: String)

    fun download(
        details: VersionManifest.VersionDetails,
        destination: File,
        onProgress: (downloaded: Long, expected: Long) -> Unit = { _, _ -> }
    ): Result {
        val downloadUrl = requireNotNull(details.clientDownloadUrl) { "Mojang metadata has no client download URL" }
        val expectedSha1 = requireNotNull(details.clientSha1) { "Mojang metadata has no client SHA-1" }
        require(expectedSha1.matches(Regex("[0-9a-fA-F]{40}"))) { "Invalid client SHA-1 in metadata" }
        val expectedSize = requireNotNull(details.clientJarSize) { "Mojang metadata has no client size" }
        require(expectedSize in 1..MAX_CLIENT_BYTES) { "Client JAR size is missing or exceeds the 1 GiB safety limit" }
        require(details.id.matches(Regex("[A-Za-z0-9._-]{1,64}")) && details.id != "." && details.id != "..") {
            "Invalid version ID"
        }

        val versionDir = File(destination, details.id)
        require(versionDir.mkdirs() || versionDir.isDirectory) { "Could not create version directory" }
        val target = File(versionDir, "${details.id}.jar")
        if (target.isFile && target.length() == expectedSize && sha1(target).equals(expectedSha1, ignoreCase = true)) {
            onProgress(expectedSize, expectedSize)
            return Result(target, expectedSize, expectedSha1.lowercase())
        }

        val usable = versionDir.usableSpace
        require(usable >= expectedSize + 16L * 1024L * 1024L) { "Not enough free storage for the client JAR" }
        val temporary = File(versionDir, "${details.id}.jar.part")
        if (temporary.exists()) temporary.delete()

        try {
            val downloadedHash = MessageDigest.getInstance("SHA-1")
            val connection = openTrustedConnection(downloadUrl)
            try {
                val code = connection.responseCode
                require(code == HttpURLConnection.HTTP_OK) { "Client download returned HTTP $code" }
                val advertisedLength = connection.getHeaderFieldLong("Content-Length", -1L)
                require(advertisedLength < 0 || advertisedLength == expectedSize) {
                    "Client download size did not match Mojang metadata"
                }
                var count = 0L
                connection.inputStream.use { input ->
                    temporary.outputStream().buffered().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            count += read
                            require(count <= expectedSize && count <= MAX_CLIENT_BYTES) {
                                "Client download exceeded the expected size"
                            }
                            downloadedHash.update(buffer, 0, read)
                            output.write(buffer, 0, read)
                            onProgress(count, expectedSize)
                        }
                    }
                }
                require(count == expectedSize) { "Client download was incomplete: $count of $expectedSize bytes" }
                val actualSha1 = downloadedHash.digest().joinToString("") { "%02x".format(it) }
                require(actualSha1.equals(expectedSha1, ignoreCase = true)) {
                    "Client JAR SHA-1 did not match Mojang's version metadata"
                }
                moveIntoPlace(temporary, target)
                return Result(target, count, actualSha1)
            } finally {
                connection.disconnect()
            }
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }

    private fun openTrustedConnection(initialUrl: String): HttpURLConnection {
        var current = URL(initialUrl)
        repeat(REDIRECT_LIMIT + 1) { redirectCount ->
            require(isTrustedUrl(current)) { "Download URL is not an approved Mojang HTTPS host" }
            val connection = (current.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = false
                setRequestProperty("Accept", "application/java-archive, application/octet-stream")
                setRequestProperty("User-Agent", "MCJavaMobileLauncher/0.1")
            }
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                require(redirectCount < REDIRECT_LIMIT && !location.isNullOrBlank()) {
                    "Too many or invalid redirects from Mojang download host"
                }
                current = URL(current, location)
            } else {
                return connection
            }
        }
        error("Too many redirects from Mojang download host")
    }

    private fun isTrustedUrl(url: URL): Boolean =
        url.protocol == "https" && url.host.lowercase() in allowedHosts

    private fun sha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun moveIntoPlace(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
