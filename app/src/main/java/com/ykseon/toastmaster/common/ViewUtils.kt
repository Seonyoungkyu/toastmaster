package com.ykseon.toastmaster.common

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color

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