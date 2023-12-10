package com.ykseon.toastmaster.ui.contextmenu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.ykseon.toastmaster.databinding.ContextMenuItemBinding
import com.ykseon.toastmaster.databinding.ContextMenuMainBinding

private const val TAG = "ContextMenuPopoup"
@RequiresApi(Build.VERSION_CODES.R)
class ContextMenuPopup(
    val context: Context,
    val items: List<ContextMenuItem>,
    val obj: Any
    ) {

    private val windowContext by lazy {
        context.createWindowContext(TYPE_APPLICATION_OVERLAY, null)
    }

    private var binding: ContextMenuMainBinding? = null
    private val windowManager by lazy {
        windowContext.getSystemService<WindowManager>()
    }

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun create(x: Int, y: Int) {
        binding = ContextMenuMainBinding.inflate(layoutInflater)

        items.forEach {menuItem ->
            val childBinding = ContextMenuItemBinding.inflate(layoutInflater)
            childBinding.menuName.text = menuItem.name

            childBinding.root.setOnClickListener {
                menuItem.action(this, obj)
            }
            binding?.root?.addView(childBinding.root)
        }
        binding?.root?.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        binding?.root?.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> true
                MotionEvent.ACTION_OUTSIDE -> {
                    dismiss()
                    true
                }
                else -> false
            }
        }
        val posX = (x - (binding?.root?.measuredWidth ?: 0) / 2).let {if (it < 0) 0 else it}
        val posY = (y - 200 - (binding?.root?.measuredHeight ?: 0)).let {if (it < 0) 0 else it}
        Log.i(TAG,"Gesture offset posX($posX), posY($posY)")
        binding?.let {windowManager?.addView(it.root, getLayoutParams(posX, posY)) }
    }
    fun show(x: Int, y: Int) {
        create(x,y)
    }

    fun dismiss() {
        binding?.let {windowManager?.removeViewImmediate(it.root) }
    }
    private fun getLayoutParams(x: Int, y: Int): WindowManager.LayoutParams {

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            x,
            y,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }
}


data class ContextMenuItem(val name: String, val action: (popup: ContextMenuPopup, obj: Any) -> Unit)