package com.vesdk.vebase.demo.present.contract;


import com.vesdk.vebase.demo.base.BasePresenter;
import com.vesdk.vebase.demo.base.IView;


/**
 *  on 2019-07-20 17:26
 */
public interface WelcomeContract {
    interface View extends IView {
        void onStartTask();
        void onEndTask(boolean result);
    }

    abstract class Presenter extends BasePresenter<View> {
        public abstract void startTask();
        public abstract int getVersionCode();
        public abstract String getVersionName();
        public abstract boolean resourceReady();
    }
}
