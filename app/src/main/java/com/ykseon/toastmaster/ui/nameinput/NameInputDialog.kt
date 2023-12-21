package com.ykseon.toastmaster.ui.nameinput

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color.BLUE
import android.graphics.Color.WHITE
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.setButtonColor

class NameInputDialog(val context: Context) {

    @SuppressLint("InflateParams")
    fun show(listener: (String) -> Unit) {

        val view = LayoutInflater.from(context).inflate(R.layout.name_input, null)
        val dialog = AlertDialog.Builder(context)
            .setTitle("Input name")
            .setView(view)
            .setPositiveButton("OK") { dialog, _ ->
                dialog?.dismiss()
                val editText: EditText = view.findViewById(R.id.edit_name)
                listener.invoke(editText.text.toString())
            }
            .create()
            .apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                setOnShowListener { _ ->
                    val editText: EditText = findViewById(R.id.edit_name)
                    editText.requestFocus()
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

                    setButtonColor()
                }
            }
        dialog.show()

    }
}
