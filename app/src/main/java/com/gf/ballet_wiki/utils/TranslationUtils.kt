package com.gf.ballet_wiki.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.gf.ballet_wiki.R
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Decoupled Singleton Translation Infrastructure.
 * Manages ML Kit translation clients, persistent caching, active language detection,
 * Wi-Fi verification, and atomic mutex locking for single-alert dialog prompts.
 * Strictly decoupled from Android UI View widgets (no TextView extensions).
 */
object TranslationManager {
    private val dialogShowingLock = AtomicBoolean(false)

    fun getActiveLanguage(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (!appLocales.isEmpty) appLocales.get(0)?.language ?: "es"
        else Locale.getDefault().language
    }

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun preloadActiveLanguageModel(context: Context) {
        val activeLang = getActiveLanguage()
        if (activeLang == "en") return

        val targetLangTag = TranslateLanguage.fromLanguageTag(activeLang) ?: return
        val model = TranslateRemoteModel.Builder(targetLangTag).build()

        RemoteModelManager.getInstance().isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (!isDownloaded && isWifiConnected(context)) {
                    val conditions = DownloadConditions.Builder().requireWifi().build()
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(targetLangTag)
                        .build()
                    Translation.getClient(options).downloadModelIfNeeded(conditions)
                }
            }
    }

    internal fun showDownloadDialogIfNeeded(
            context: Context,
            onConfirmed: () -> Unit,
            onCancelled: () -> Unit
                                           ) {
        if (dialogShowingLock.compareAndSet(false, true)) {
            try {
                AlertDialog.Builder(context)
                        .setTitle(R.string.translation_dialog_title)
                        .setMessage(R.string.translation_dialog_message)
                        .setPositiveButton(R.string.download) { _, _ ->
                            dialogShowingLock.set(false)
                            onConfirmed()
                        }
                        .setNegativeButton(R.string.view_original) { _, _ ->
                            dialogShowingLock.set(false)
                            onCancelled()
                        }
                        .setOnCancelListener {
                            dialogShowingLock.set(false)
                            onCancelled()
                        }
                        .show()
            }
            catch (e: Exception) {
                dialogShowingLock.set(false)
                onCancelled()
            }
        }
        else {
            onCancelled()
        }
    }

    /**
     * Pure string-based translation method.
     * Decoupled from UI widgets. Calls [onTranslating] when network/download work begins,
     * and delivers the final translated (or fallback) text to [onTranslated].
     */
    fun translate(
            context: Context,
            originalText: String,
            sourceLanguage: String = "en",
            onTranslating: (() -> Unit)? = null,
            onTranslated: (String) -> Unit
                 ) {
        val activeLang = getActiveLanguage()

        if (activeLang == sourceLanguage || originalText.isBlank()) {
            onTranslated(originalText)
            return
        }

        val sessionManager = SessionManager.getInstance(context)
        val cached = sessionManager.getCachedTranslation(originalText, activeLang)
        if (!cached.isNullOrBlank()) {
            onTranslated(cached)
            return
        }

        val targetLangTag =
                TranslateLanguage.fromLanguageTag(activeLang) ?: TranslateLanguage.SPANISH
        val sourceLangTag =
                TranslateLanguage.fromLanguageTag(sourceLanguage) ?: TranslateLanguage.ENGLISH
        val model = TranslateRemoteModel.Builder(targetLangTag).build()
        val modelManager = RemoteModelManager.getInstance()

        onTranslating?.invoke()

        val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLangTag)
                .setTargetLanguage(targetLangTag)
                .build()
        val translator = Translation.getClient(options)

        modelManager.isModelDownloaded(model).addOnSuccessListener { isDownloaded ->
            if (isDownloaded) {
                translator.translateStringWithCache(
                        originalText, activeLang, sessionManager, onTranslated
                                                   )
            }
            else if (isWifiConnected(context)) {
                val conditions = DownloadConditions.Builder().requireWifi().build()
                translator.downloadModelIfNeeded(conditions)
                        .addOnSuccessListener {
                            translator.translateStringWithCache(
                                    originalText, activeLang, sessionManager, onTranslated
                                                               )
                        }
                        .addOnFailureListener { onTranslated(originalText) }
            }
            else {
                showDownloadDialogIfNeeded(
                        context = context,
                        onConfirmed = {
                            onTranslating?.invoke()
                            translator.downloadModelIfNeeded()
                                    .addOnSuccessListener {
                                        translator.translateStringWithCache(
                                                originalText, activeLang, sessionManager,
                                                onTranslated
                                                                           )
                                    }
                                    .addOnFailureListener { onTranslated(originalText) }
                        },
                        onCancelled = { onTranslated(originalText) }
                                          )
            }
        }.addOnFailureListener { onTranslated(originalText) }
    }

    fun translateList(
            context: Context,
            items: List<String>,
            sourceLanguage: String = "en",
            onTranslated: (List<String>) -> Unit
                     ) {
        if (items.isEmpty()) {
            onTranslated(emptyList())
            return
        }
        val results = arrayOfNulls<String>(items.size)
        var completed = 0
        items.forEachIndexed { index, item ->
            if (item.isBlank()) {
                results[index] = item
                completed++
                if (completed == items.size) {
                    onTranslated(results.filterNotNull())
                }
            }
            else {
                translate(
                        context = context,
                        originalText = item,
                        sourceLanguage = sourceLanguage,
                        onTranslating = null,
                        onTranslated = { translatedItem ->
                            results[index] = translatedItem
                            completed++
                            if (completed == items.size) {
                                onTranslated(results.filterNotNull())
                            }
                        }
                         )
            }
        }
    }

    private fun Translator.translateStringWithCache(
            originalText: String,
            activeLang: String,
            sessionManager: SessionManager,
            onTranslated: (String) -> Unit
                                                   ) {
        if (originalText.isBlank()) {
            onTranslated(originalText)
            return
        }

        val paragraphs = originalText.split("\n\n")
        if (paragraphs.size <= 1) {
            translate(originalText)
                .addOnSuccessListener { translatedText ->
                    sessionManager.cacheTranslation(originalText, activeLang, translatedText)
                    onTranslated(translatedText)
                }
                    .addOnFailureListener { onTranslated(originalText) }
        }
        else {
            val translatedParagraphs = arrayOfNulls<String>(paragraphs.size)
            var completedCount = 0

            paragraphs.forEachIndexed { index, paragraph ->
                if (paragraph.isBlank()) {
                    translatedParagraphs[index] = paragraph
                    completedCount++
                    if (completedCount == paragraphs.size) {
                        val fullTranslation =
                                translatedParagraphs.filterNotNull().joinToString("\n\n")
                        sessionManager.cacheTranslation(originalText, activeLang, fullTranslation)
                        onTranslated(fullTranslation)
                    }
                }
                else {
                    translate(paragraph)
                        .addOnSuccessListener { transPara ->
                            translatedParagraphs[index] = transPara
                            completedCount++
                            if (completedCount == paragraphs.size) {
                                val fullTranslation =
                                        translatedParagraphs.filterNotNull().joinToString("\n\n")
                                sessionManager.cacheTranslation(
                                        originalText, activeLang, fullTranslation
                                                               )
                                onTranslated(fullTranslation)
                            }
                        }
                            .addOnFailureListener {
                                translatedParagraphs[index] = paragraph
                                completedCount++
                                if (completedCount == paragraphs.size) {
                                    val fullTranslation = translatedParagraphs.filterNotNull()
                                            .joinToString("\n\n")
                                    sessionManager.cacheTranslation(
                                            originalText, activeLang, fullTranslation
                                                                   )
                                    onTranslated(fullTranslation)
                                }
                            }
                }
            }
        }
    }
}

/**
 * Pure string utility for composing titles with original title in parentheses.
 */
fun buildTitleString(translatedTitle: String, originalTitle: String?): String {
    if (originalTitle.isNullOrBlank()) {
        return translatedTitle
    }
    if (translatedTitle.trim().equals(originalTitle.trim(), ignoreCase = true)) {
        return translatedTitle
    }
    return "$translatedTitle ($originalTitle)"
}
