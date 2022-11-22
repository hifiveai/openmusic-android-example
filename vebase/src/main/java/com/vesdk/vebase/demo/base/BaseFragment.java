package com.vesdk.vebase.demo.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * on 2020-11-17
 */
abstract public class BaseFragment<T extends IPresenter> extends Fragment implements IView {
    protected T mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setPresenter(T presenter) {
        assert presenter != null;
        mPresenter = presenter;
        mPresenter.attachView(this);
    }

    public <T extends View> T findViewById(@IdRes int id) {
        View rootView = getView();
        if (rootView == null) {
            return null;
        }
        return rootView.findViewById(id);
    }

    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }
}
