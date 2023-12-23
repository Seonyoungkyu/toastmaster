package com.ykseon.toastmaster.common

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

fun AlertDialog.setButtonColor(): AlertDialog {
    val mode = context.resources.configuration.uiMode

    if (mode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
        getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.YELLOW)
    } else {
        getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
    }

    if (mode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
        getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.YELLOW)
    } else {
        getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
    }
    return this
}

fun Offset.isInsideBounds(rect: Rect): Boolean {
    return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
}
