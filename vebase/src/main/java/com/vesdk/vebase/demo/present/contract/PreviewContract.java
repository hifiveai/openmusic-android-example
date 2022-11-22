package com.vesdk.vebase.demo.present.contract;


import android.view.SurfaceView;

import com.ss.android.vesdk.VECameraCapture;
import com.ss.android.vesdk.VERecorder;
import com.vesdk.vebase.demo.base.BasePresenter;
import com.vesdk.vebase.demo.base.IView;
import com.vesdk.vebase.demo.model.ComposerNode;
import com.vesdk.vebase.demo.model.FuncItem;

import java.io.File;
import java.util.List;


/**
 *  on 2019-07-20 17:26
 */
public interface PreviewContract {
    interface View extends IView {
        void onStartTask();

        void changeDuetImage(String imagePath);
    }

    abstract class Presenter extends BasePresenter<View> {

        public abstract void initRecorder(SurfaceView surfaceView);
        public abstract VERecorder getRecorder();
        public abstract VECameraCapture getCapture();

        public abstract void setComposerNodes(String[] nodes);

        public abstract void updateComposerNode(ComposerNode node, boolean update);

        public abstract void setSticker(File file);

        public abstract void onFilterSelected(File file);

        public abstract void onFilterValueChanged(float cur);

        public abstract void restoreComposer();

        public abstract void onNormalDown();

        public abstract void onNormalUp();
        public abstract void onSwitchDuet();
    }
}
