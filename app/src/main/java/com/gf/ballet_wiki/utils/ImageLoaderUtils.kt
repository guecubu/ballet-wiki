package com.gf.ballet_wiki.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.dispose
import coil.load
import coil.memory.MemoryCache
import com.gf.ballet_wiki.R
import okhttp3.OkHttpClient

object CoilImageLoader {

    @Volatile
    private var imageLoader: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: run {
                val okHttpClient = OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                    .header(
                                            "User-Agent",
                                            "BalletReference/1.0 (Android App; contact@ballet-wiki.com)"
                                           )
                                    .build()
                            chain.proceed(request)
                        }
                        .build()

                ImageLoader.Builder(context.applicationContext)
                        .okHttpClient(okHttpClient)
                        .memoryCache {
                            MemoryCache.Builder(context.applicationContext)
                                    .maxSizePercent(0.25)
                                    .build()
                        }
                        .diskCache {
                            DiskCache.Builder()
                                    .directory(
                                            context.applicationContext.cacheDir.resolve(
                                                    "image_cache"
                                                                                       )
                                              )
                                    .maxSizeBytes(1024L * 1024L * 250L)
                                    .build()
                        }
                        .crossfade(true)
                        .build().also { imageLoader = it }
            }
        }
    }
}

fun ImageView.loadUrl(url: String?, placeholderResId: Int = R.drawable.bg_image_placeholder) {
    this.dispose()
    if (url.isNullOrBlank()) {
        setImageResource(placeholderResId)
        return
    }
    this.load(url, CoilImageLoader.get(context)) {
        crossfade(300)
        placeholder(placeholderResId)
        error(placeholderResId)
        placeholderMemoryCacheKey(url)
    }
}

fun ImageView.loadUrlWithThumbnail(
        highResUrl: String?,
        thumbnailUrl: String?,
        placeholderResId: Int = R.drawable.bg_image_placeholder
                                  ) {
    this.dispose()
    val targetUrl = highResUrl?.takeIf { it.isNotBlank() } ?: thumbnailUrl
    if (targetUrl.isNullOrBlank()) {
        setImageResource(placeholderResId)
        return
    }
    this.load(targetUrl, CoilImageLoader.get(context)) {
        crossfade(300)
        placeholder(placeholderResId)
        error(placeholderResId)
        thumbnailUrl?.takeIf { it.isNotBlank() }?.let { thumb ->
            placeholderMemoryCacheKey(thumb)
        }
    }
}
