package com.vesdk.vebase.demo.base;

public interface IPresenter {
    void attachView(IView view);
    void detachView();
}
