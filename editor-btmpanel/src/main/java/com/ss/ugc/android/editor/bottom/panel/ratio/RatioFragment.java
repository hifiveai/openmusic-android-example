package com.ss.ugc.android.editor.bottom.panel.ratio;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ss.ugc.android.editor.base.EditorSDK;
import com.ss.ugc.android.editor.base.constants.TypeConstants;
import com.ss.ugc.android.editor.base.theme.FuncBarViewConfig;
import com.ss.ugc.android.editor.base.theme.ThemeStore;
import com.ss.ugc.android.editor.base.utils.UIUtils;
import com.ss.ugc.android.editor.bottom.R;
import com.ss.ugc.android.editor.core.api.canvas.CanvasRatio;
import com.ss.ugc.android.editor.core.api.canvas.ORIGINAL;
import com.ss.ugc.android.editor.core.api.canvas.RATIO_16_9;
import com.ss.ugc.android.editor.core.api.canvas.RATIO_1_1;
import com.ss.ugc.android.editor.core.api.canvas.RATIO_3_4;
import com.ss.ugc.android.editor.core.api.canvas.RATIO_4_3;
import com.ss.ugc.android.editor.core.api.canvas.RATIO_9_16;
import com.ss.ugc.android.editor.core.utils.DLog;
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory;
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RatioFragment extends BaseUndoRedoFragment<RatioViewModel>
        implements RatioListAdapter.OnItemClickListener {

    private RecyclerView rv;
    private ImageView iv;
    public int mType = TypeConstants.getTYPE_RATIO();
    private RatioListAdapter adapter;
    private SparseIntArray mSelectMap = new SparseIntArray();
    private FuncBarViewConfig funcBarViewConfig = ThemeStore.INSTANCE.getFunctionBarViewConfig();

    @Override
    public int getContentViewLayoutId() {
        return R.layout.btm_panel_ratio;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideBottomBar();
        iv = view.findViewById(R.id.iv_back);
        if (funcBarViewConfig.getBackIconDrawableRes() != 0) {
            iv.setImageDrawable(ContextCompat.getDrawable(requireContext(), funcBarViewConfig.getBackIconDrawableRes()));
        }
        int marginStart = funcBarViewConfig.getBackIconMarginStart();
        if (marginStart > 0) {
            if (iv.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) iv.getLayoutParams()).leftMargin = UIUtils.INSTANCE.dp2px(requireContext(), marginStart);
            }
        }
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFragment();
            }
        });
        rv = view.findViewById(R.id.rc_fuc_bottom);

        adapter = new RatioListAdapter(getContext(), getItems(), this);
        adapter.setType(mType);
        mSelectMap.put(mType, getViewModel().getSavedIndex());
        adapter.setSelectMap(mSelectMap);

        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);

        getViewModel().setBusinessRatioList();
        if (EditorSDK.getInstance().config.getBusinessCanvasRatioList().contains(ORIGINAL.INSTANCE)) {
            getViewModel().insertOriginal();
        }

        DLog.d("ratioViewModel" + getViewModel());
    }

    private List<RatioItem> getItems() {
        List<RatioItem> items = new ArrayList<>();
        List<CanvasRatio> canvasList = EditorSDK.getInstance().config.getBusinessCanvasRatioList();
        for (CanvasRatio ratio : canvasList) {
            if (RATIO_9_16.INSTANCE.equals(ratio)) {
                items.add(new RatioItem("9:16", 0));
            } else if (RATIO_3_4.INSTANCE.equals(ratio)) {
                items.add(new RatioItem("3:4", 0));
            } else if (RATIO_1_1.INSTANCE.equals(ratio)) {
                items.add(new RatioItem("1:1", 0));
            } else if (RATIO_4_3.INSTANCE.equals(ratio)) {
                items.add(new RatioItem("4:3", 0));
            } else if (RATIO_16_9.INSTANCE.equals(ratio)) {
                items.add(new RatioItem("16:9", 0));
            } else {
                items.add(new RatioItem(getString(R.string.ck_ratio_origin), 0));
            }
        }
        return items;
    }

    @Override
    public void onItemClick(String title, int position) {
        if (mSelectMap.get(mType, 0) != position) {
            getViewModel().updateCanvasResolution(position);
        }
        mSelectMap.put(mType, position);
        adapter.notifyDataSetChanged();
    }

    @NotNull
    @Override
    public RatioViewModel provideEditorViewModel() {
        return EditViewModelFactory.Companion.viewModelProvider(this).get(RatioViewModel.class);
    }

    @Override
    public void onUpdateUI() {
        mSelectMap.put(mType, getViewModel().getSavedIndex());
        adapter.notifyDataSetChanged();
    }

}
