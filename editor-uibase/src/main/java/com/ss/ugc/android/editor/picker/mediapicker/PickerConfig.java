package com.ss.ugc.android.editor.picker.mediapicker;

/**
 */


public class PickerConfig {
    public static final String LOG_TAG = "MediaPicker";

    /**
     * 最大图片选择次数，int类型，默认40
     */
    public static final String MAX_SELECT_COUNT = "max_select_count";

    public static final int DEFAULT_SELECTED_MAX_COUNT = 40;

    /**
     * 最大文件大小，int类型，默认180m
     */
    public static final String MAX_SELECT_SIZE = "max_select_size";

    public static final long DEFAULT_SELECTED_MAX_SIZE = 188743680;

    /**
     * 图片选择模式，默认选视频和图片
     */
    public static final String SELECT_MODE = "select_mode";

    /**
     * 选择类型 add select replace
     * {@link PickType}
     */
    public static final String PICK_TYPE = "pick_type";

    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * 默认选择集
     */
    public static final String DEFAULT_SELECTED_LIST = "default_list";
    /**
     * 预览集
     */
    public static final String PRE_RAW_LIST = "pre_raw_List";
    public static final int RESULT_CODE = 19901026;
    public static final int RESULT_UPDATE_CODE = 1990;
    public static final int PICKER_IMAGE_INCLUDE_GIF = 100;
    public static final int PICKER_IMAGE_VIDEO = 101;
    public static final int PICKER_VIDEO = 102;
    public static final int PICKER_IMAGE_EXCLUDE_GIF = 103;
    public static int GridSpanCount = 3;
    public static int GridSpace = 4;
    /**
     * 最小视频播放时间
     */
    public static final String MIN_VIDEO_TIME_THRESHOLD = "min_video_time_threshold";
    public static final long NO_TIME_LIMIT = -1;
}
