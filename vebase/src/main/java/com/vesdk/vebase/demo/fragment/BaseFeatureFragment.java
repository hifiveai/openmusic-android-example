package com.vesdk.vebase.demo.fragment;


import com.vesdk.vebase.demo.base.BaseFragment;
import com.vesdk.vebase.demo.base.IPresenter;

/**
 * 每个功能fragment的基类
 * base class of each fragment
 * @param <T>
 */
public abstract class BaseFeatureFragment<T extends IPresenter, Callback> extends BaseFragment<T> {
    private Callback mCallback;

    public BaseFeatureFragment setCallback(Callback t){
        this.mCallback =t;
        return this;
    }

    public Callback getCallback() {
        return mCallback;
    }

    public void refreshIcon(int current_feature){};

    /**
     * FragmentRigger  拦截onBackPressed方法是否生效。
     * @return
     */
    public boolean onRiggerBackPressed(){
        //需要拦截，则返回true，否则返回false
        return false ;
    }
}
