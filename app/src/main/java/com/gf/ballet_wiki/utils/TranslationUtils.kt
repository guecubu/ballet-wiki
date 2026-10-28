package com.gf.ballet_wiki.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.widget.TextView
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

object TranslationManager {

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
}

fun TextView.setTranslatedText(originalText: String, sourceLanguage: String = "en", showOriginalInParentheses: Boolean = false) {
    val context = this.context
    val activeLang = TranslationManager.getActiveLanguage()

    if (activeLang == sourceLanguage || originalText.isBlank()) {
        setOriginal(originalText)
        return
    }

    val targetLangTag = TranslateLanguage.fromLanguageTag(activeLang) ?: TranslateLanguage.SPANISH
    val sourceLangTag = TranslateLanguage.fromLanguageTag(sourceLanguage) ?: TranslateLanguage.ENGLISH
    val model = TranslateRemoteModel.Builder(targetLangTag).build()
    val modelManager = RemoteModelManager.getInstance()

    setText(R.string.translating)

    val options = TranslatorOptions.Builder()
        .setSourceLanguage(sourceLangTag)
        .setTargetLanguage(targetLangTag)
        .build()
    val translator = Translation.getClient(options)

    modelManager.isModelDownloaded(model).addOnSuccessListener { isDownloaded ->
        if (isDownloaded) {
            translator.translateAndDisplay(originalText, this, showOriginalInParentheses)
        } else if (TranslationManager.isWifiConnected(context)) {
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener { translator.translateAndDisplay(originalText, this, showOriginalInParentheses) }
                .addOnFailureListener { setOriginal(originalText) }
        } else {
            AlertDialog.Builder(context)
                .setTitle(R.string.translation_dialog_title)
                .setMessage(R.string.translation_dialog_message)
                .setPositiveButton(R.string.download) { _, _ ->
                    setText(R.string.translating)
                    translator.downloadModelIfNeeded()
                        .addOnSuccessListener { translator.translateAndDisplay(originalText, this, showOriginalInParentheses) }
                        .addOnFailureListener { setOriginal(originalText) }
                }
                .setNegativeButton(R.string.view_original) { _, _ -> setOriginal(originalText) }
                .setOnCancelListener { setOriginal(originalText) }
                .show()
        }
    }.addOnFailureListener { setOriginal(originalText) }
}

private fun Translator.translateAndDisplay(originalText: String, textView: TextView, showOriginalInParentheses: Boolean) {
    translate(originalText)
        .addOnSuccessListener { translatedText ->
            if (showOriginalInParentheses && !translatedText.equals(originalText, ignoreCase = true)) {
                textView.setOriginal("$translatedText ($originalText)")
            } else {
                textView.setOriginal(translatedText)
            }
        }
        .addOnFailureListener { textView.setOriginal(originalText) }
}

private fun TextView.setOriginal(textToShow: String) {
    text = textToShow
}
