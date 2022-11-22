package com.ss.ugc.android.editor.base.constants

class CropConstants{
    companion object {
        const val MIN_SCALE = 0.1F
        const val MAX_SCALE = 50F

        const val ARG_SEGMENT_ID = "segmentId"
        const val ARG_CROP_FRAME_SCALE = "cropFrameScale"
        const val ARG_CROP_FRAME_ROTATE_ANGLE = "cropFrameRotateAngle"
        const val ARG_CROP_FRAME_TRANSLATE_X = "cropFrameTranslateX"
        const val ARG_CROP_FRAME_TRANSLATE_Y = "cropTranslateY"
        const val ARG_VIDEO_PATH = "videoPath"
        const val ARG_VIDEO_WIDTH = "videoWidth"
        const val ARG_VIDEO_HEIGHT = "videoHeight"
        const val ARG_VIDEO_SOURCE_DURATION = "videoSourceDuration"
        const val ARG_SOURCE_TIME_RANGE_START = "sourceTimeRangeStart"
        const val ARG_SOURCE_TIME_RANGE_END = "sourceTimeRangeEnd"
        const val ARG_CURRENT_PLAY_TIME = "currentPlayTime"
        const val ARG_CLIP_INDEX = "clipIndex"
        const val ARG_MEDIA_TYPE = "mediaType"
        const val ARG_CROP_RATIO = "cropRatio"
        const val ARG_CROP_LEFT_TOP = "cropLeftTop"
        const val ARG_CROP_RIGHT_TOP = "cropRightTop"
        const val ARG_CROP_LEFT_BOTTOM = "cropLeftBottom"
        const val ARG_CROP_RIGHT_BOTTOM = "cropRightBottom"

        const val RESULT_CROP_SCALE = "crop_scale"
        const val RESULT_CROP_ROTATE_ANGLE = "crop_rotate_angle"
        const val RESULT_CROP_TRANSLATE_X = "crop_translate_x"
        const val RESULT_CROP_TRANSLATE_Y = "crop_translate_y"

        const val RESULT_SEGMENT_ID = "segment_id"
        const val RESULT_CROP_RATIO = "crop_ratio"
        const val RESULT_DATA_LEFT_TOP = "leftTop"
        const val RESULT_DATA_RIGHT_TOP = "rightTop"
        const val RESULT_DATA_LEFT_BOTTOM = "leftBottom"
        const val RESULT_DATA_RIGHT_BOTTOM = "rightBottom"
        const val TAG = "VideoFrameAdjustActivity"

        const val EXTRA_TRANS_X = "crop_transX"
        const val EXTRA_TRANS_Y = "crop_transY"
        const val EXTRA_SCALE = "crop_scale"
        const val EXTRA_DEGREE = "crop_degree"
    }
}