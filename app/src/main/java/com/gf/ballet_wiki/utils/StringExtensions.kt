package com.gf.ballet_wiki.utils

import android.text.format.DateFormat
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.normalize(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    return regex.replace(normalized, "")
}

fun String.search(query: String, ignoreCase: Boolean = true): Boolean {
    return this.normalize().contains(query.normalize(), ignoreCase)
}

enum class DateStyle {
    FULL,
    YEAR_ONLY
}

fun String?.toLocalizedDate(style: DateStyle = DateStyle.FULL): String {
    if (this.isNullOrBlank()) return ""
    val formatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                           )
    var localDate: LocalDate? = null
    for (formatter in formatters) {
        try {
            localDate = LocalDate.parse(this, formatter)
            break
        }
        catch (e: Exception) {
            // Prueba el siguiente formato si este falla
        }
    }
    if (localDate == null) return this

    return try {
        when (style) {
            DateStyle.YEAR_ONLY -> localDate.year.toString()
            DateStyle.FULL -> {
                val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMdd")
                localDate.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
            }
        }
    }
    catch (e: Exception) {
        this
    }

}