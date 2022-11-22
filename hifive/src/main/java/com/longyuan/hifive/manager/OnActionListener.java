package com.longyuan.hifive.manager;

/**
 * #duxiaxing
 * #date: 2022/7/25
 */
public interface OnActionListener<T> {
    boolean isNeedProgress();
    void onStart();
    void onProgress(int progress);
    void onFail(String errorInfo);
    void onSuccess(T model);
}
