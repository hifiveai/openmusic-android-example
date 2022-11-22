package com.vesdk.vebase.demo.present.contract;



import com.vesdk.vebase.demo.base.BasePresenter;
import com.vesdk.vebase.demo.base.IView;
import com.vesdk.vebase.demo.model.FilterItem;

import java.util.List;

/**
 *  on 2019-07-21 12:22
 */
public interface FilterContract {
    interface View extends IView {

    }

    abstract class Presenter extends BasePresenter<View> {
        public abstract List<FilterItem> getItems();
    }
}
