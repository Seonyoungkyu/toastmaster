package com.ykseon.toastmaster.ui.nameinput

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import com.ykseon.toastmaster.R

class NameInputDialog(val context: Context) {

    @SuppressLint("InflateParams")
    fun show(listener: (String) -> Unit) {

        val view = LayoutInflater.from(context).inflate(R.layout.name_input, null)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Input name")
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog?.dismiss()
            val editText: EditText = view.findViewById(R.id.edit_name)
            listener.invoke(editText.text.toString())
        }
        builder.show()
    }
}
