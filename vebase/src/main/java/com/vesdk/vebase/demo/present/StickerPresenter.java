package com.vesdk.vebase.demo.present;

import android.content.Context;


import com.vesdk.vebase.DemoApplication;

import com.vesdk.vebase.R;
import com.vesdk.vebase.demo.model.StickerItem;
import com.vesdk.vebase.demo.present.contract.StickerContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static com.vesdk.vebase.demo.present.contract.StickerContract.MASK;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_ANIMOJI;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_ARSCAN;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_STICKER;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_STICKER_2D;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_STICKER_3D;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_STICKER_COMPLEX;

/**
 *  on 2019-07-21 14:09
 */
public class StickerPresenter extends StickerContract.Presenter {
    private List<StickerItem> mStickerComplexItems;
    private List<StickerItem> mSticker2DItems;
    private List<StickerItem> mSticker3DItems;
    private List<StickerItem> mAnimojiItems;
    private List<StickerItem> mARScanItems;

    @Override
    public List<StickerItem> getItems(int type) {
        switch (type & MASK) {
            case TYPE_STICKER:
                return getStickerItems(type);
            case TYPE_ANIMOJI:
//                return getAnimojiItems();
            case TYPE_ARSCAN:
//                return getARScanItems();
                default:
                    return Collections.emptyList();
        }
    }

    private List<StickerItem> getStickerItems(int type) {
        switch (type) {
            case TYPE_STICKER_2D:
                return getSticker2DItems();
            case TYPE_STICKER_3D:
                return getSticker3DItems();
            case TYPE_STICKER_COMPLEX:
                return getStickerComplexItems();
            default:
                return Collections.emptyList();
        }
    }

    private List<StickerItem> getSticker2DItems() {
        if (mSticker2DItems != null) return mSticker2DItems;

        mSticker2DItems = new ArrayList<>();
        Context context = DemoApplication.context();

        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_landiaoxueying), R.drawable.icon_landiaoxueying, "stickers/landiaoxueying", context.getString(R.string.ck_sticker_tip_landiaoxueying)));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_weilandongrizhuang), R.drawable.icon_weilandongrizhuang, "stickers/weilandongrizhuang", context.getString(R.string.ck_sticker_tip_weilandongrizhuang)));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_tiaowuhuoji), R.drawable.icon_tiaowuhuoji, "stickers/tiaowuhuoji", context.getString(R.string.ck_sticker_tip_tiaowuhuoji)));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_lizishengdan), R.drawable.icon_lizishengdan, "stickers/lizishengdan", context.getString(R.string.ck_sticker_tip_lizishengdan)));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_heimaoyanjing), R.drawable.icon_heimaoyanjing, "stickers/heimaoyanjing"));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_chitushaonv), R.drawable.icon_chitushaonv, "stickers/chitushaonv"));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_huahua), R.drawable.icon_huahua, "stickers/huahua", context.getString(R.string.ck_sticker_tip_huahua)));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_zhaocaimao), R.drawable.icon_zhaocaimao, "stickers/zhaocaimao"));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_wochaotian), R.drawable.icon_wochaotian, "stickers/wochaotian"));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_xiatiandefeng), R.drawable.icon_xiatiandefeng, "stickers/xiatiandefeng", context.getString(R.string.ck_sticker_tip_xiatiandefeng)));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_shengrikuaile), R.drawable.icon_shengrikuaile, "stickers/shengrikuaile"));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_zhutouzhuer), R.drawable.icon_zhutouzhuer, "stickers/zhutouzhuer"));
        mSticker2DItems.add(new StickerItem(context.getString(R.string.ck_sticker_huanletuchiluobo), R.drawable.icon_huanletuchiluobo, "stickers/huanletuchiluobo"));
        return mSticker2DItems;
    }

    private List<StickerItem> getSticker3DItems() {
        if (mSticker3DItems != null) return mSticker3DItems;

        mSticker3DItems = new ArrayList<>();
        Context context = DemoApplication.context();

        mSticker3DItems.add(new StickerItem(context.getString(R.string.ck_sticker_zhuluojimaoxian), R.drawable.icon_zhuluojimaoxian, "stickers/zhuluojimaoxian", context.getString(R.string.ck_sticker_tip_zhuluojimaoxian)));
        mSticker3DItems.add(new StickerItem(context.getString(R.string.ck_sticker_nuannuandoupeng), R.drawable.icon_nuannuandoupeng, "stickers/nuannuandoupeng", context.getString(R.string.ck_sticker_tip_nuannuandoupeng)));
        mSticker3DItems.add(new StickerItem(context.getString(R.string.ck_sticker_haoqilongbao), R.drawable.icon_haoqilongbao, "stickers/haoqilongbao", context.getString(R.string.ck_sticker_tip_haoqilongbao)));
        mSticker3DItems.add(new StickerItem(context.getString(R.string.ck_sticker_konglongshiguangji), R.drawable.icon_konglongshiguangji, "stickers/konglongshiguangji", context.getString(R.string.ck_sticker_tip_konglongshiguangji)));
        mSticker3DItems.add(new StickerItem(context.getString(R.string.ck_sticker_konglongceshi), R.drawable.icon_konglongceshi, "stickers/konglongceshi", context.getString(R.string.ck_sticker_tip_konglongceshi)));
        return mSticker3DItems;
    }

    private List<StickerItem> getStickerComplexItems() {
        if (mStickerComplexItems != null) return mStickerComplexItems;

        mStickerComplexItems = new ArrayList<>();
        Context context = DemoApplication.context();

        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_maobing), R.drawable.icon_maobing, "stickers/maobing", context.getString(R.string.ck_sticker_tip_snap_with_cats)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_kongquegongzhu), R.drawable.icon_kongquegongzhu, "stickers/kongquegongzhu", context.getString(R.string.ck_sticker_tip_kongquegongzhu)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_eldermakup), R.drawable.icon_eldermakup, "stickers/eldermakup"));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_kidmakup), R.drawable.icon_kidmakup, "stickers/kidmakup"));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_zisemeihuo), R.drawable.icon_zisemeihuo, "stickers/zisemeihuo", context.getString(R.string.ck_sticker_tip_zisemeihuo)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_yanlidoushini), R.drawable.icon_yanlidoushini, "stickers/yanlidoushini", context.getString(R.string.ck_sticker_tip_yanlidoushini)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_xiaribingshuang), R.drawable.icon_xiaribingshuang, "stickers/xiaribingshuang", context.getString(R.string.ck_sticker_tip_xiaribingshuang)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_biaobaqixi), R.drawable.icon_biaobaiqixi, "stickers/biaobaiqixi", context.getString(R.string.ck_sticker_tip_biaobaqixi)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_cinamiheti), R.drawable.icon_cinamiheti, "stickers/cinamiheti", context.getString(R.string.ck_sticker_tip_cinamiheti)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_shuiliandong), R.drawable.icon_shuiliandong, "stickers/shuiliandong", context.getString(R.string.ck_sticker_tip_shuiliandong)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_mofabaoshi), R.drawable.icon_mofabaoshi, "stickers/mofabaoshi", context.getString(R.string.ck_sticker_tip_mofabaoshi)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_shangke), R.drawable.icon_shangke, "stickers/shangke", context.getString(R.string.ck_sticker_tip_shangke)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_baibianfaxing), R.drawable.icon_baibianfaxing, "stickers/baibianfaxing", context.getString(R.string.ck_sticker_tip_baibianfaxing)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_qianduoduo), R.drawable.icon_qianduoduo, "stickers/qianduoduo", context.getString(R.string.ck_sticker_tip_qianduoduo)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_meihaoxinqing), R.drawable.icon_meihaoxinqing, "stickers/meihaoxinqing", context.getString(R.string.ck_sticker_tip_meihaoxinqing)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_jiancedanshenyinyuan), R.drawable.icon_jiancedanshenyinyuan, "stickers/jiancedanshenyinyuan", context.getString(R.string.ck_sticker_tip_jiancedanshenyinyuan)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_shuihaimeigeqiutian), R.drawable.icon_shuihaimeigeqiutian, "stickers/shuihaimeigeqiutian", context.getString(R.string.ck_sticker_tip_shuihaimeigeqiutian)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_kejiganqueaixiong), R.drawable.icon_kejiganqueaixiong, "stickers/kejiganqueaixiong", context.getString(R.string.ck_sticker_tip_kejiganqueaixiong)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_mengguiyaotang), R.drawable.icon_mengguiyaotang, "stickers/mengguiyaotang", context.getString(R.string.ck_sticker_tip_mengguiyaotang)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_dianjita), R.drawable.icon_dianjita, "stickers/dianjita", context.getString(R.string.ck_sticker_tip_dianjita)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_katongnan), R.drawable.icon_katongnan, "stickers/katongnan"));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_katongnv), R.drawable.icon_katongnv, "stickers/katongnv"));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_jiamian), R.drawable.icon_jiamian, "stickers/jiamian", context.getString(R.string.ck_sticker_tip_jiamian)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_huanlongshu), R.drawable.icon_huanlongshu, "stickers/huanlongshu", context.getString(R.string.ck_sticker_tip_huanlonghsu)));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_gongzhumianju), R.drawable.icon_gongzhumianju, "stickers/gongzhumianju"));
        mStickerComplexItems.add(new StickerItem(context.getString(R.string.ck_sticker_shenshi), R.drawable.icon_shenshi, "stickers/shenshi"));
        return mStickerComplexItems;
    }


}
