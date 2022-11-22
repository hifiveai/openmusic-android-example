package com.ss.ugc.android.editor.base.view

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.theme.ThemeStore.globalUIConfig
import com.ss.ugc.android.editor.base.utils.SizeUtil
import kotlinx.android.synthetic.main.editor_custom_dialog.*

class EditorDialog(context: Context, val width: Int, val height: Int) : BaseDialog(context) {

    companion object {
        const val DIALOG_SIZE_WIDTH_F = 286F
        const val DIALOG_SIZE_HEIGHT_F = 160F
    }

    var mDialogTitle: TextView? = null
    var mDialogContent: TextView? = null
    var mDialogCancel: TextView? = null
    var mDialogConfirm: TextView? = null
    var mDialogClose: ImageView? = null
    var mDialogEditTextLayout: ConstraintLayout? = null
    var mDialogEditText: EditText? = null
    var mDialogTextLimit: TextView? = null

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initViews()
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.editor_custom_dialog, null)
        setContentView(view, ViewGroup.LayoutParams(width, height))
        mDialogTitle = findViewById(R.id.editor_dialog_title)
        mDialogContent = findViewById(R.id.editor_dialog_content)
        mDialogCancel = findViewById(R.id.editor_dialog_cancel)
        mDialogConfirm = findViewById(R.id.editor_dialog_confirm)
        mDialogClose = findViewById(R.id.editor_dialog_close)
        mDialogEditTextLayout = editor_dialog_edittext_layout
        mDialogEditText = editor_dialog_edittext
        mDialogTextLimit = editor_dialog_edittext_limit

        mDialogClose?.setOnClickListener { dismiss() }
    }

    private fun switchStyle(style: DialogStyle) {
        when (style) {
            DialogStyle.STYLE_ONLY_CONTENT -> showOnlyContent()
            DialogStyle.STYLE_NO_TITLE -> showNoTitle()
            DialogStyle.STYLE_FULL_WITH_CLOSE -> showFullWithCloseBtn()
            else -> showFull()
        }
    }

    private fun showFull() {
        mDialogTitle?.visibility = View.VISIBLE
        mDialogConfirm?.visibility = View.VISIBLE
        mDialogCancel?.visibility = View.VISIBLE
//        mDialogClose?.visibility = View.GONE
        mDialogClose?.visibility = View.VISIBLE
    }

    private fun showFullWithCloseBtn() {
        mDialogTitle?.visibility = View.VISIBLE
        mDialogConfirm?.visibility = View.VISIBLE
        mDialogCancel?.visibility = View.VISIBLE
        mDialogClose?.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)//有关闭按钮，点击其他区域不可取消弹窗
    }

    private fun showNoTitle() {
        mDialogTitle?.visibility = View.GONE
        mDialogConfirm?.visibility = View.VISIBLE
        mDialogCancel?.visibility = View.VISIBLE
//        mDialogClose?.visibility = View.GONE
        mDialogClose?.visibility = View.VISIBLE
        changeContentTopMargin(29.0F)
    }

    private fun showOnlyContent() {
        mDialogTitle?.visibility = View.GONE
        mDialogConfirm?.visibility = View.GONE
        mDialogCancel?.visibility = View.GONE
        mDialogClose?.visibility = View.VISIBLE
        changeContentTopMargin(41.0F)
    }

    private fun changeContentTopMargin(top: Float) {
        mDialogContent?.apply {
            val param = layoutParams as ConstraintLayout.LayoutParams
            param.topMargin = SizeUtil.dp2px(top)
            layoutParams = param
        }
    }

    override fun show() {
        super.show()
    }

    enum class DialogStyle {
        STYLE_ONLY_CONTENT,
        STYLE_NO_TITLE,
        STYLE_FULL,
        STYLE_FULL_WITH_CLOSE
    }

    class Builder(val context: Context) {
        var mTitle: String? = null
        var mContent: String? = null
        var mConfirmListener: OnConfirmListener? = null
        var mCancelListener: OnCancelListener? = null
        var mContext: Context? = null
        var mCancelText: String? = null
        var mConfirmText: String? = null
        var mStyle: DialogStyle =
            if (EditorSDK.instance.isInitialized && globalUIConfig.showDialogCloseBtn) DialogStyle.STYLE_FULL_WITH_CLOSE else DialogStyle.STYLE_FULL
        var mEnableTextInput: Boolean = false
        var mMaxLines: Int = 0
        var mMaxChar: Int = 0
        var mShowLimit: Boolean = false
        var mTextInputChangeListener: OnTextInputChangeListener? = null
        var mTextWatcher: TextWatcher? = null
        var mEditDefaultText: String? = null
        var mWidth: Int = SizeUtil.dp2px(DIALOG_SIZE_WIDTH_F)
        var mHeight: Int = SizeUtil.dp2px(DIALOG_SIZE_HEIGHT_F)
        var mCancelable: Boolean = true

        fun setEnableEditText(
            enable: Boolean = false,
            maxLines: Int = 0,
            maxChar: Int = 0,
            showLimit: Boolean = false
        ): Builder {
            mEnableTextInput = enable
            if (mEnableTextInput) {
                mMaxLines = maxLines
                mMaxChar = maxChar
                this.mShowLimit = showLimit
            }
            return this
        }

        fun setEditTextDefaultText(s: String): Builder {
            this.mEditDefaultText = s
            return this
        }

        fun setTextInputChangeListener(listener: OnTextInputChangeListener? = null): Builder {
            this.mTextInputChangeListener = listener
            return this
        }

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setContent(content: String): Builder {
            mContent = content
            return this
        }

        fun setConfirmText(confirmText: String): Builder {
            mConfirmText = confirmText
            return this
        }

        fun setCancelText(cancelText: String): Builder {
            mCancelText = cancelText
            return this
        }

        fun setConfirmListener(confirmListener: OnConfirmListener): Builder {
            mConfirmListener = confirmListener
            return this
        }

        fun setCancelListener(cancelListener: OnCancelListener): Builder {
            mCancelListener = cancelListener
            return this
        }

        fun setStyle(style: DialogStyle): Builder {
            mStyle = style
            return this
        }

        fun setWidth(width: Int): Builder {
            mWidth = width
            return this
        }

        fun setHeight(height: Int): Builder {
            mHeight = height
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            mCancelable = cancelable
            return this
        }

        fun build(): EditorDialog {
            return EditorDialog(context, mWidth, mHeight).apply {
                setCanceledOnTouchOutside(mCancelable)
                setCancelable(mCancelable)
                mDialogTitle?.text = mTitle
                mDialogContent?.text = mContent
                mDialogCancel?.apply {
                    text = mCancelText
                    setOnClickListener {
                        dismiss()
                        mCancelListener?.onClick()
                    }
                }
                mDialogConfirm?.apply {
                    text = mConfirmText
                    setOnClickListener {
                        dismiss()
                        mConfirmListener?.onClick()
                    }
                }
                switchStyle(mStyle)
                if (mEnableTextInput) {
                    mDialogContent?.visibility = View.GONE
                    mDialogEditTextLayout?.visibility = View.VISIBLE
                    mDialogEditText?.apply {
                        filters = arrayOf(InputFilter.LengthFilter(mMaxChar))
                        maxLines = mMaxLines
                        if (mMaxLines == 1) {
                            setSingleLine()
                        }
                        if (mEditDefaultText != null) {
                            text = SpannableStringBuilder(mEditDefaultText)
                        }
                        addTextChangedListener(object : OnTextInputChangeListener() {
                            override fun onInputChange(input: String?) {
                                mTextInputChangeListener?.onInputChange(input)
                                mDialogTextLimit?.text = "${input?.length ?: 0}/$mMaxChar"
                            }
                        })
                    }
                    if (mShowLimit) {
                        mDialogTextLimit?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    interface OnCancelListener {
        fun onClick()
    }

    interface OnConfirmListener {
        fun onClick()
    }

    abstract class OnTextInputChangeListener : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            onInputChange(p0.toString())
        }

        abstract fun onInputChange(input: String?)
    }
}