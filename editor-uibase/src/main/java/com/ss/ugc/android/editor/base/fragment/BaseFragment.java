package com.ss.ugc.android.editor.base.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public abstract class BaseFragment extends Fragment {

    protected static final String BUNDLE_KEY = "/bundle/key";
    protected View mContentView;
    protected Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(getContentView(), container, false);
        initView(mContentView, inflater);
        return mContentView;
    }

    protected void initView(View view, LayoutInflater inflater) {

    }

    @LayoutRes
    public abstract int getContentView();

    protected View findViewById(@IdRes int id) {
        return mContentView.findViewById(id);
    }


    protected void pop() {
        new FragmentHelper().closeFragment(this);
    }

}
