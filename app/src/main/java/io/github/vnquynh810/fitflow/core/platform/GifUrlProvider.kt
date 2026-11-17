package io.github.vnquynh810.fitflow.core.platform

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GifUrlProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val bundledGifFiles: Set<String> by lazy {
        runCatching { context.assets.list("gifs")?.toSet().orEmpty() }
            .getOrDefault(emptySet())
    }

    fun isBundledLocally(fileName: String): Boolean {
        val clean = fileName.trim()
        return clean.isNotEmpty() && clean in bundledGifFiles
    }

    fun resolve(fileName: String): String {
        val clean = fileName.trim()
        if (clean.isEmpty()) return ""
        return if (clean in bundledGifFiles) {
            "file:///android_asset/gifs/$clean"
        } else {
            "$REMOTE_RELEASE_BASE_URL/$clean"
        }
    }

    companion object {
        const val REMOTE_RELEASE_BASE_URL =
            "https://github.com/wind5293/Exercise_GIFs/releases/latest/download"

        fun resolveWithContext(context: Context, fileName: String): String {
            val clean = fileName.trim()
            if (clean.isEmpty()) return ""
            val hasAsset = runCatching {
                context.assets.openFd("gifs/$clean").use { true }
            }.getOrDefault(false)
            return if (hasAsset) {
                "file:///android_asset/gifs/$clean"
            } else {
                "$REMOTE_RELEASE_BASE_URL/$clean"
            }
        }
    }
}
