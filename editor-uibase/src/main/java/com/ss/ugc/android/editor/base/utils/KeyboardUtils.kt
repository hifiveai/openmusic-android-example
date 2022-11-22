package com.ss.ugc.android.editor.base.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.ResultReceiver
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow

/**
 * time : 2020/12/28
 *
 * description :
 *
 */
object KeyboardUtils {
    
    fun show(
            editText: EditText,
            flag: Int,
            focus: Boolean,
            cursorEnd: Boolean = true,
            receiver: ResultReceiver? = null
    ) {
        editText.apply {
            visibility = View.VISIBLE
            isFocusable = true
            isFocusableInTouchMode = true

            if (focus) {
                requestFocus()
            }

            val imm: InputMethodManager? =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.showSoftInput(editText, flag, receiver)

            if (cursorEnd) {
                setSelection(text.toString().length)
            }
        }
    }

    fun hide(editText: EditText) {
        val imm: InputMethodManager? =
                editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun hideWherever(view: View) {
        val imm: InputMethodManager? =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var popupWindow: PopupWindow? = null

    /**
     *  背景：考虑界面设置adjustNothing,无法直接获取view的高度变化
     *  解决方式：添加一个空白的window,监听其中的view的大小变化
     */
    fun observerKeyboard(activity: Activity, keyboardListener: OnKeyboardListener) {
        val rootView = View(activity)
        popupWindow = PopupWindow(activity).apply {
            rootView.layoutParams = ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            contentView = rootView
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
            setBackgroundDrawable(ColorDrawable(0))
            isTouchable = false
            postOnUiThread {
                showAtLocation(activity.window.decorView, Gravity.NO_GRAVITY, 0, 0);
            }
        }

        if (onGlobalLayoutListener == null) {
            onGlobalLayoutListener = getGlobalObserver(rootView, keyboardListener)
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun getGlobalObserver(
        rootView: View,
        keyboardListener: OnKeyboardListener
    ): ViewTreeObserver.OnGlobalLayoutListener {
        val wrapper = HeightWrapper()
        val rootRect = Rect()
        (rootView.context as Activity).window.decorView.getWindowVisibleDisplayFrame(rootRect)

        return ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            var height = r.height()
            if (wrapper.height === 0) {
                wrapper.height = height
            } else {
                val diff: Int = wrapper.height - height

                if (diff < -200 && rootRect.height() == height) {
                    keyboardListener.onKeyboardHidden()
                }
                wrapper.height = height
            }
        }
    }

    fun clearObserver(activity: Activity){
        val rootView = activity.window.decorView
        popupWindow?.apply {
            dismiss()
            null
        }
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        onGlobalLayoutListener = null
    }

}

class HeightWrapper {
    var height = 0
}


interface OnKeyboardListener {
    fun onKeyboardHidden()
}