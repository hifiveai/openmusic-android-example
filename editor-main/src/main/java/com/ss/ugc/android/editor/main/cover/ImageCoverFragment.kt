package com.ss.ugc.android.editor.main.cover

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.core.api.video.ImageCoverInfo
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.main.R
import com.ss.ugc.android.editor.picker.mediapicker.PickerActivity
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig
import kotlinx.android.synthetic.main.fragment_image_cover.*

class ImageCoverFragment : BaseUndoRedoFragment<ImageCoverViewModel>() {

    override fun getContentViewLayoutId() = R.layout.fragment_image_cover

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomBar()

        image_cover.setOnClickListener {
            val intent = Intent(activity, PickerActivity::class.java)
            val maxSize = 188743680L //long long long long类型
            intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize) //default 180MB (Optional)
            intent.putExtra(PickerConfig.MAX_SELECT_COUNT, 1) //default 40 (Optional)
            intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_EXCLUDE_GIF)
            activity?.startActivityForResult(intent, ActivityForResultCode.COVER_IMAGE_REQUEST_CODE)
        }

        ImageLoader.loadBitmap(
            view.context,
            viewModel.getImageCover(),
            image_cover,
            ImageOption.Builder().scaleType(ImageView.ScaleType.CENTER_CROP)
                .build()
        )
    }

    override fun provideEditorViewModel() =
        EditViewModelFactory.viewModelProvider(this).get(ImageCoverViewModel::class.java)

    override fun onUpdateUI() {}

    fun resetImageCover() {
        image_cover.setImageResource(R.drawable.bg_image_cover)
    }

    fun updateImageCover(
        context: Context,
        imageCoverInfo: ImageCoverInfo
    ): Boolean {
        image_cover?.let {
            ImageLoader.loadBitmap(
                context,
                imageCoverInfo.path,
                image_cover,
                ImageOption.Builder().scaleType(ImageView.ScaleType.CENTER_CROP)
                    .build()
            )
            viewModel.addImageCover(imageCoverInfo)
            return true
        }
        return false
    }
}