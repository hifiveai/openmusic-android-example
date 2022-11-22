package com.ss.ugc.android.editor.bottom.panel.adjust;


import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.ies.nlemediajava.FilterType;
import com.ss.ugc.android.editor.base.constants.TypeConstants;
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment;
import com.ss.ugc.android.editor.base.resource.ResourceItem;
import com.ss.ugc.android.editor.base.resource.ResourceListListener;
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig;
import com.ss.ugc.android.editor.base.resource.base.IResourceProvider;
import com.ss.ugc.android.editor.base.theme.OptPanelViewConfig;
import com.ss.ugc.android.editor.base.theme.ThemeStore;
import com.ss.ugc.android.editor.base.utils.CommonUtils;
import com.ss.ugc.android.editor.base.view.EditorDialog;
import com.ss.ugc.android.editor.base.view.ProgressBar;
import com.ss.ugc.android.editor.bottom.R;
import com.ss.ugc.android.editor.core.api.keyframe.KeyframeUpdate;
import com.ss.ugc.android.editor.core.utils.DLog;
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class AdjustFragment extends BaseUndoRedoFragment<AdjustViewModel> implements AdjustRVAdapter.OnItemClickListener {

    protected int mType = 0;
    //    private SparseIntArray mSelectMap ; // 用于保存当前类型选中了哪个
//    private SparseArray<Float> mProgressMap ; // 用于保存每个小项的具体值 亮度 对比度的值等
    //是否来自底部功能区 用于判断添加整段滤镜还是单段clip滤镜 默认false
    private boolean mFromFunctionView;

    private ProgressBar pbFilter;
    private RecyclerView rvFilter;
    private AdjustRVAdapter adapter;
    private RelativeLayout resetButton;
    private TextView tvResetButton;
    private ImageView iv_panel_reset;

    private AdjustDataResource dataResource = new AdjustDataResource();

    /**
     * stringTypes要包含所有{@link #getStringType(int)} 中的类型，否则重置的时候会漏掉
     * 如果是全局 需要加"_global"后缀
     */
    private final String[] stringTypes = {
            FilterType.BRIGHTNESS,
            FilterType.LIGHT_SENSATION,
            FilterType.TONE,
            FilterType.CONTRAST,
            FilterType.TEMPERATURE,
            FilterType.SATURATION,
            FilterType.FADE,
            FilterType.HIGHLIGHT,
            FilterType.SHADOW,
            FilterType.VIGNETTING,
            FilterType.SHARPEN
    };
    // 如果是全局 需要加"_global"后缀
    private final HashSet<String> stringTypeSet = new HashSet<>(Arrays.asList(stringTypes));

    public AdjustFragment setType(int type) {
        mType = type;
        return this;
    }

    public AdjustFragment setSelectMap(SparseIntArray selectMap) {
//        mSelectMap = selectMap;
        return this;
    }

    public AdjustFragment setProgressMap(SparseArray<Float> progressMap) {
//        mProgressMap = progressMap;
        return this;
    }

    public AdjustFragment setFromFunction(boolean isFromFunctionView) {
        mFromFunctionView = isFromFunctionView;
        return this;
    }

    @Override
    public int getContentViewLayoutId() {
        return R.layout.btm_panel_adjust;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPanelName(getString(R.string.ck_adjust));
        pbFilter = view.findViewById(R.id.pb_filter);
        rvFilter = view.findViewById(R.id.rc_filter);

        resetButton = view.findViewById(R.id.reset_container);
        tvResetButton = view.findViewById(R.id.tv_reset);
        iv_panel_reset = view.findViewById(R.id.iv_panel_reset);
        resetButton.setVisibility(View.VISIBLE);

        final int savePosition = getViewModel().getSavePosition(mFromFunctionView);
        DLog.d("savePosition：" + savePosition);
        pbFilter.setVisibility(savePosition == -1 ? View.INVISIBLE : View.VISIBLE);
        setPbIntensity(savePosition);

        OptPanelViewConfig config = ThemeStore.INSTANCE.getOptPanelViewConfig();
        if (config != null) {
            if (config.getSlidingBarColor() != 0) {
                pbFilter.setActiveLineColor(config.getSlidingBarColor());
            }
        }
        pbFilter.setOnProgressChangedListener(new ProgressBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser, int eventAction) {
                if (isFormUser) {
                    DLog.d("拖动进度onProgressChanged isFormUser:" + progress);
                    dispatchProgress(progress, eventAction);
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    return;
                }
                if (getViewModel().hasSlots()) {
                    showDialog();
                }
            }
        });
        refreshResetState();

        getViewModel().getKeyframeEvent().observe(getViewLifecycleOwner(), new Observer<KeyframeUpdate>() {
            @Override
            public void onChanged(@Nullable KeyframeUpdate keyframeUpdate) {
                int position = getViewModel().getSavePosition(mFromFunctionView);
                if (position >= 0) {
                    setPbIntensity(position);
                }
            }
        });
        final View listContainer = view.findViewById(R.id.cke_bottom_adjust_list);
        final View listLoading = view.findViewById(R.id.cke_bottom_adjust_loading);
        final View listError = view.findViewById(R.id.cke_bottom_adjust_error);
        IResourceProvider resourceProvider = getResourceProvider();
        if (resourceProvider != null) {
            String panel = DefaultResConfig.ADJUST_PANEL;

            resourceProvider.fetchResourceList(panel, true, new ResourceListListener<ResourceItem>() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(@NonNull List<? extends ResourceItem> dataList) {
                    if (isDetached() || !isAdded()) {
                        return;
                    }
                    listLoading.setVisibility(View.GONE);
                    listContainer.setVisibility(View.VISIBLE);
                    adapter = new AdjustRVAdapter(getContext(), (List<ResourceItem>) dataList, AdjustFragment.this);
                    adapter.setCurrentPosition(savePosition);

                    rvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvFilter.setAdapter(adapter);
                }

                @Override
                public void onFailure(@Nullable Exception exception, @Nullable String tips) {
                    if (isDetached() || !isAdded()) {
                        return;
                    }
                    listLoading.setVisibility(View.GONE);
                    listError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private boolean resetState = true;

    private void refreshResetState() {
        if (!isAdded()) {
            return;
        }
        boolean hasFilter = getViewModel().hasEffectSlots();
        if (resetState == hasFilter) {
            return;
        }
        resetState = hasFilter;
        resetButton.setClickable(hasFilter);
        tvResetButton.setTextColor(hasFilter ? getResources().getColor(R.color.white) : getResources().getColor(R.color.colorGrey));
        iv_panel_reset.setImageResource(hasFilter ? R.drawable.ic_reset_white : R.drawable.ic_reset_gray);
    }

    private void showDialog() {
        String content = getViewModel().hasKeyframe() ? getString(R.string.ck_adjust_reset_content_current) : getString(R.string.ck_adjust_reset_content);
        new EditorDialog.Builder(this.getActivity())
                .setTitle(getString(R.string.ck_adjust_reset_title))
                .setContent(content)
                .setCancelText(getString(R.string.ck_cancel))
                .setConfirmText(getString(R.string.ck_confirm))
                .setCancelable(false)
                .setConfirmListener(new EditorDialog.OnConfirmListener() {
                    @Override
                    public void onClick() {
                        getViewModel().resetAllFilterIntensity(mFromFunctionView, stringTypeSet);
                        setPbIntensity(adapter.getCurrentPosition());
                        refreshResetState();
                    }
                })
                .build()
                .show();

    }


    private void dispatchProgress(float progress, int eventAction) {
        int position = adapter.getCurrentPosition();
        if (position == -1) {
            return;
        }

        float intensity = isNegative(position) ? (progress - 0.5f) * 2 : progress;
        ResourceItem filterItem = adapter.getItem(position);
        getViewModel().setFilter(filterItem.getPath(), mFromFunctionView, intensity, getStringType(position), position);
        if (eventAction == MotionEvent.ACTION_UP) {
            refreshResetState();
            getViewModel().done();
        }
    }

    /**
     * 1.设置进度条是否双向
     * 2.改变值
     */
    private float setPbIntensity(int position) {
        boolean defaultNegative = isNegative(position);
        float intensity = getViewModel().getSaveIntensity(mFromFunctionView, getStringType(position));
        pbFilter.setNegativeable(defaultNegative);
        // 设置是否双向
        pbFilter.setProgress(defaultNegative ? intensity / 2 + 0.5f : intensity);
        return intensity;
    }

    /**
     * 判断进度条是否双向
     *
     * @param position
     * @return
     */
    private boolean isNegative(int position) {
        return dataResource.getDefaultNegative(getType(position));
    }

    // 0 -> 0.5f  (mProgress - 0.5f)
    // 1 ->
    private float calculateProgress(boolean defaultNegative, float intensity) {
        if (defaultNegative) {
            return 0F;
        } else {
            return intensity;
        }
    }

    @Override
    public void onItemClick(File file, int type, int position) {
        pbFilter.setVisibility(View.VISIBLE);

        adapter.setCurrentPosition(position);

        float intensity = setPbIntensity(position);
        DLog.d(file == null ? "调节为空" : file.getAbsolutePath() + " type:" + getStringType(position) + " intensity" + intensity);

        getViewModel().setFilter(file == null ? "" : file.getAbsolutePath(), mFromFunctionView, intensity, getStringType(position), position);
        getViewModel().done();

        refreshResetState();
    }


    private int getType(int position) {
        int adjustType = 0;
        if (position == 0) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_LD();
        } else if (position == 1) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_BGD();
        } else if (position == 2) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_SD();
        } else if (position == 3) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_DBD();
        } else if (position == 4) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_BHD();
        } else if (position == 5) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_GG();
        } else if (position == 6) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_SW();
        } else if (position == 7) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_RH();
        } else if (position == 8) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_YY();
        } else if (position == 9) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_TS();
        } else if (position == 10) {
            adjustType = TypeConstants.Companion.getTYPE_ADJUST_AJ();
        }
        return adjustType;
    }

    /**
     * 新加StringType注意同时在{@link #stringTypes}中更新
     */
    private String getStringType(int position) {
        String adjustType = "";
        if (position == 0) {
            adjustType = FilterType.BRIGHTNESS;
        } else if (position == 1) {
            adjustType = FilterType.LIGHT_SENSATION; //曝光(光感)
        } else if (position == 2) {
            adjustType = FilterType.TONE;  //色调
        } else if (position == 3) {
            adjustType = FilterType.CONTRAST;
        } else if (position == 4) {
            adjustType = FilterType.TEMPERATURE;
        } else if (position == 5) {
            adjustType = FilterType.SATURATION;
        } else if (position == 6) {
            adjustType = FilterType.FADE;
        } else if (position == 7) {
            adjustType = FilterType.HIGHLIGHT;
        } else if (position == 8) {
            adjustType = FilterType.SHADOW;
        } else if (position == 9) {
            adjustType = FilterType.VIGNETTING;
        } else if (position == 10) {
            adjustType = FilterType.SHARPEN;
        }
//        return  adjustType;
        return mFromFunctionView ? adjustType + "_global" : adjustType;
    }


    @NotNull
    @Override
    public AdjustViewModel provideEditorViewModel() {
        return EditViewModelFactory.Companion.viewModelProvider(this).get(AdjustViewModel.class);
    }

    @Override
    public void onUpdateUI() {
        int position = getViewModel().getSavePosition(mFromFunctionView);

        if (position == -1) {
            pop();
            return;
        }
        float intensity = getViewModel().getSaveIntensity(mFromFunctionView, getStringType(position));
        Log.d("Jeff", "nleModel.setFilterPosition: " + "position=" + position);

        pbFilter.setVisibility(position == -1 ? View.INVISIBLE : View.VISIBLE);
        setPbIntensity(position);
        if (adapter.getCurrentPosition() != position) {
            adapter.setCurrentPosition(position);
        }
        refreshResetState();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //对齐IOS,退出后不记忆选中item
        getViewModel().savePosition(false, -1);
    }
}
