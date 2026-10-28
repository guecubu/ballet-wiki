package com.gf.ballet_wiki.utils

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.gf.ballet_wiki.R

object CoilImageLoader {

    @Volatile
    private var imageLoader: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: ImageLoader.Builder(context.applicationContext)
                .crossfade(true)
                .build().also { imageLoader = it }
        }
    }
}

fun ImageView.loadUrl(url: String?, placeholderResId: Int = R.drawable.ic_launcher_background) {
    if (url.isNullOrBlank()) {
        setImageResource(placeholderResId)
        return
    }
    this.load(url, CoilImageLoader.get(context)) {
        crossfade(true)
        placeholder(placeholderResId)
        error(placeholderResId)
    }
}
