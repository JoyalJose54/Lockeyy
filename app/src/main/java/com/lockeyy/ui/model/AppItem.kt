package com.lockeyy.ui.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isLocked: Boolean
)
