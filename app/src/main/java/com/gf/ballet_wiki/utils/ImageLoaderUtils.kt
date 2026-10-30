package com.gf.ballet_wiki.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.load
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
                                    .header("User-Agent", "BalletWiki/1.0 (Android App)")
                                    .build()
                            chain.proceed(request)
                        }
                        .build()

                ImageLoader.Builder(context.applicationContext)
                        .okHttpClient(okHttpClient)
                        .crossfade(enable = true)
                        .build().also { imageLoader = it }
            }
        }
    }
}

fun ImageView.loadUrl(url: String?, placeholderResId: Int = R.drawable.bg_image_placeholder) {
    if (url.isNullOrBlank()) {
        setImageResource(placeholderResId)
        return
    }
    this.load(url, CoilImageLoader.get(context)) {
        crossfade(enable = true)
        placeholder(placeholderResId)
        error(placeholderResId)
    }
}
