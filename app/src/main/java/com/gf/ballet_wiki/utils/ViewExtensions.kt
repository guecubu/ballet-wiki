package com.gf.ballet_wiki.utils

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

fun MaterialCardView.applyDynamicCleanStyle(
        backgroundColorInt: Int,
        strokeColorInt: Int,
        cornerRadiusDp: Float = 20f,
        strokeWidthDp: Int = 2,
        elevationDp: Float = 4f
                                           ) {
    val density = context.resources.displayMetrics.density
    cardElevation = elevationDp * density
    backgroundTintList = null
    foreground = null

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = cornerRadiusDp * density
        setColor(backgroundColorInt)
        setStroke((strokeWidthDp * density).toInt(), strokeColorInt)
    }
}

fun MaterialCardView.applyCustomCardStyle(backgroundColorRes: Int, strokeColorRes: Int) {
    val density = context.resources.displayMetrics.density
    cardElevation = 4f * density
    backgroundTintList = null
    foreground = null

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 20f * density
        setColor(ContextCompat.getColor(context, backgroundColorRes))
        setStroke(
                (2 * density).toInt(),
                ContextCompat.getColor(context, strokeColorRes)
                 )
    }
}

fun View.fixAllMaterialCardsRecursively() {
    if (this is MaterialCardView) {
        //        val density = context.resources.displayMetrics.density
        val bgColor = cardBackgroundColor.defaultColor
        val currentStrokeWidth = strokeWidth
        val currentStrokeColor = strokeColorStateList?.defaultColor ?: 0

        cardElevation = cardElevation // Mantiene la elevación que tenga
        backgroundTintList = null
        foreground = null
        clipToOutline = true
        // Empuja el contenido interior para liberar el espacio exacto del borde lateral
        if (currentStrokeWidth > 0) {
            setContentPadding(
                    currentStrokeWidth, currentStrokeWidth, currentStrokeWidth, currentStrokeWidth
                             )
        }

        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius // Respeta el corner radius del XML
            setColor(bgColor)
            if (currentStrokeWidth > 0 && currentStrokeColor != 0) {
                setStroke(currentStrokeWidth, currentStrokeColor)
            }
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            //            if (child is ImageView && currentStrokeWidth > 0) {
            //                (child.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            //                    params.setMargins(currentStrokeWidth, params.topMargin, currentStrokeWidth, params.bottomMargin)
            //                    child.layoutParams = params
            //                }
            //            }
            child.fixAllMaterialCardsRecursively()
        }
    }
    else if (this is ViewGroup) {
        for (i in 0 until childCount) {
            getChildAt(i).fixAllMaterialCardsRecursively()
        }
    }
}

fun RecyclerView.fixCardsInAdapter() {
    addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            view.fixAllMaterialCardsRecursively()
        }

        override fun onChildViewDetachedFromWindow(view: View) {}
    })
}

