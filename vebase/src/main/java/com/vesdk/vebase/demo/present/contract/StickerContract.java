package com.vesdk.vebase.demo.present.contract;


import com.vesdk.vebase.demo.base.BasePresenter;
import com.vesdk.vebase.demo.base.IView;
import com.vesdk.vebase.demo.model.StickerItem;

import java.util.List;

/**
 *  on 2019-07-21 12:24
 */
public interface StickerContract {
    int OFFSET = 8;
    int MASK = ~0xff;
    int TYPE_STICKER = 1 << OFFSET;
    int TYPE_ANIMOJI = 2 << OFFSET;
    int TYPE_ARSCAN = 3 << OFFSET;
    int TYPE_STICKER_2D = TYPE_STICKER + 1;
    int TYPE_STICKER_COMPLEX = TYPE_STICKER + 2;
    int TYPE_STICKER_3D = TYPE_STICKER + 3;

    interface View extends IView {

    }

    abstract class Presenter extends BasePresenter<View> {
//        public abstract List<StickerItem> getItems();
        public abstract List<StickerItem> getItems(int type);
    }
}
