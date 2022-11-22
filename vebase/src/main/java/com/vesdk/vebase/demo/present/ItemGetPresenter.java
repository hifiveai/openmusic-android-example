package com.vesdk.vebase.demo.present;


import android.text.TextUtils;

import com.vesdk.vebase.R;
import com.vesdk.vebase.demo.model.ComposerNode;
import com.vesdk.vebase.demo.model.EffectButtonItem;
import com.vesdk.vebase.demo.present.contract.ItemGetContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.vesdk.vebase.demo.present.contract.ItemGetContract.MASK;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.NODE_ALL_SLIM;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.NODE_BEAUTY_4ITEMS;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.NODE_BEAUTY_CAMERA;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.NODE_RESHAPE_CAMERA;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.SUB_MASK;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_ENHANCE_HIP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_ENHANCE_NECK;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_ENLARGE_BREAST;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_LONG_LEG;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_SHRINK_HEAD;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_SLIM_ARM;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_SLIM_LEG;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_SLIM_WAIST;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_THIN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_FACE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_FACE_SHARPEN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_FACE_SMOOTH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_FACE_WHITEN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_CHIN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_FACE_OVERALL;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_NOSE_LEAN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_REMOVE_POUCH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_WHITEN_TEETH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_CLOSE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_BLUSHER;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_DEFAULT;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYEBROW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYELASH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYESHADOW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_FACIAL;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_HAIR;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_LIP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_OPTION;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_PUPIL;


/**
 *  on 2019-07-21 12:27
 */
public class ItemGetPresenter extends ItemGetContract.Presenter {

    @Override
    public List<EffectButtonItem> getItems(int type) {
        List<EffectButtonItem> items = new ArrayList<>();
        switch (type & MASK) {
            case TYPE_BEAUTY_FACE:  // 美颜
                getBeautyFaceItems(items);
                break;
            case TYPE_BEAUTY_RESHAPE: // 美型
                getBeautyReshapeItems(items);
                break;
            case TYPE_BEAUTY_BODY:  // 美体
                getBeautyBodyItems(items);
                break;
            case TYPE_MAKEUP: // 美妆
                getMakeupItems(items);
                break;
            case TYPE_MAKEUP_OPTION:
                getMakeupOptionItems(items, type);
                break;
            case TYPE_MAKEUP_DEFAULT:
                getDefaultMakeupOptionItems(items, type);
                break;
        }
        return items;
    }

    // 美颜
    private void getBeautyFaceItems(List<EffectButtonItem> items) {
        String beautyNode = beautyNode(); // NODE_BEAUTY_CAMERA
        items.add(new EffectButtonItem(R.drawable.ic_beauty_close, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_smooth2, R.string.ck_beauty_face_smooth, new ComposerNode(TYPE_BEAUTY_FACE_SMOOTH, beautyNode, "smooth")));

        String language = Locale.getDefault().getLanguage();
        if (TextUtils.equals(language, "zh")) {
            items.add(new EffectButtonItem(R.drawable.ic_beauty_whiten2, R.string.ck_beauty_face_whiten, new ComposerNode(TYPE_BEAUTY_FACE_WHITEN, beautyNode, "whiten")));
        }

        items.add(new EffectButtonItem(R.drawable.ic_beauty_sharpen2, R.string.ck_beauty_face_sharpen, new ComposerNode(TYPE_BEAUTY_FACE_SHARPEN, beautyNode, "sharp")));
    }

    // 美型
    private void getBeautyReshapeItems(List<EffectButtonItem> items) {
        String reshapeNode = reshapeNode();
        items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_cheek_reshape, R.string.ck_beauty_reshape_face_overall, new ComposerNode(TYPE_BEAUTY_RESHAPE_FACE_OVERALL, reshapeNode, "Internal_Deform_Overall")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_eye_reshape, R.string.ck_beauty_reshape_eye, new ComposerNode(TYPE_BEAUTY_RESHAPE_EYE, reshapeNode, "Internal_Deform_Eye")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_reshape_chin, R.string.ck_beauty_reshape_chin, new ComposerNode(TYPE_BEAUTY_RESHAPE_CHIN, reshapeNode, "Internal_Deform_Chin")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_reshape_nose_lean, R.string.ck_beauty_reshape_nose_lean, new ComposerNode(TYPE_BEAUTY_RESHAPE_NOSE_LEAN, reshapeNode, "Internal_Deform_Nose")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_reshape_mouth_zoom, R.string.ck_beauty_reshape_mouth_zoom, new ComposerNode(TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM, reshapeNode, "Internal_Deform_ZoomMouth")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_smooth, R.string.ck_beauty_face_remove_pouch, new ComposerNode(TYPE_BEAUTY_RESHAPE_REMOVE_POUCH, NODE_BEAUTY_4ITEMS, "BEF_BEAUTY_REMOVE_POUCH")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_smooth, R.string.ck_beauty_face_brighten_eye, new ComposerNode(TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE, NODE_BEAUTY_4ITEMS, "BEF_BEAUTY_BRIGHTEN_EYE")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_smooth, R.string.ck_beauty_face_whiten_teeth, new ComposerNode(TYPE_BEAUTY_RESHAPE_WHITEN_TEETH, NODE_BEAUTY_4ITEMS, "BEF_BEAUTY_WHITEN_TEETH")));
    }

    // 美体
    private void getBeautyBodyItems(List<EffectButtonItem> items) {
        items.add(new EffectButtonItem(R.drawable.ic_beauty_close, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_thin, R.string.ck_beauty_body_thin, new ComposerNode(TYPE_BEAUTY_BODY_THIN, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_THIN")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_long_leg, R.string.ck_beauty_body_long_leg, new ComposerNode(TYPE_BEAUTY_BODY_LONG_LEG, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_LONG_LEG")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_shrink_head, R.string.ck_beauty_body_shrink_head, new ComposerNode(TYPE_BEAUTY_BODY_SHRINK_HEAD, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_SHRINK_HEAD")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_slim_leg, R.string.ck_beauty_body_leg_slim, new ComposerNode(TYPE_BEAUTY_BODY_SLIM_LEG, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_SLIM_LEG")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_thin, R.string.ck_beauty_body_waist_slim, new ComposerNode(TYPE_BEAUTY_BODY_SLIM_WAIST, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_SLIM_WAIST")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_enlarge_breast, R.string.ck_beauty_body_breast_enlarge, new ComposerNode(TYPE_BEAUTY_BODY_ENLARGE_BREAST, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_ENLARGR_BREAST")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_enhance_hip, R.string.ck_beauty_body_hip_enhance, new ComposerNode(TYPE_BEAUTY_BODY_ENHANCE_HIP, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_ENHANCE_HIP")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_enhance_neck, R.string.ck_beauty_body_neck_enhance, new ComposerNode(TYPE_BEAUTY_BODY_ENHANCE_NECK, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_ENHANCE_NECK")));
        items.add(new EffectButtonItem(R.drawable.ic_beauty_body_slim_arm, R.string.ck_beauty_body_arm_slim, new ComposerNode(TYPE_BEAUTY_BODY_SLIM_ARM, NODE_ALL_SLIM, "BEF_BEAUTY_BODY_SLIM_ARM")));

    }

    // 美妆
    private void getMakeupItems(List<EffectButtonItem> items) {
        items.add(new EffectButtonItem(R.drawable.ic_beauty_close, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_makeup_blusher, new ComposerNode(TYPE_MAKEUP_BLUSHER)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_makeup_lip, new ComposerNode(TYPE_MAKEUP_LIP)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_facial, R.string.ck_makeup_facial, new ComposerNode(TYPE_MAKEUP_FACIAL)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_makeup_pupil, new ComposerNode(TYPE_MAKEUP_PUPIL)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_hair, R.string.ck_makeup_hair, new ComposerNode(TYPE_MAKEUP_HAIR)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_makeup_eye, new ComposerNode(TYPE_MAKEUP_EYESHADOW)));
        items.add(new EffectButtonItem(R.drawable.ic_makeup_eyebrow, R.string.ck_makeup_eyebrow, new ComposerNode(TYPE_MAKEUP_EYEBROW)));
    }

    private void getDefaultMakeupOptionItems(List<EffectButtonItem> items, int type) {
        //腮红 日常20
        items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_richang, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/richang", "Internal_Makeup_Blusher")));
        //口红 西柚色 50
        items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_xiyouse, new ComposerNode(TYPE_MAKEUP_LIP, "lip/xiyouse", "Internal_Makeup_Lips")));
        //修容 03 35
        items.add(new EffectButtonItem(R.drawable.ic_makeup_facial, R.string.ck_facial_3, new ComposerNode(TYPE_MAKEUP_FACIAL, "facial/xiurong03", "Internal_Makeup_Facial")));
        //美瞳 可可棕 40
        items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_kekezong, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/kekezong", "Internal_Makeup_Pupil")));
        //眼影 气质粉 35
        items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_qizhifen, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/qizhifen", "Internal_Makeup_Eye")));
        //眉毛 BK02 35
        items.add(new EffectButtonItem(R.drawable.ic_makeup_eyebrow, R.string.ck_eyebrow_BKO2, new ComposerNode(TYPE_MAKEUP_EYEBROW, "eyebrow/BK02", "Internal_Makeup_Brow")));

        items.add(new EffectButtonItem(R.drawable.ic_makeup_hair, R.string.ck_hair_anlan, new ComposerNode(TYPE_MAKEUP_HAIR, "hair/anlan", "")));

    }

    private void getMakeupOptionItems(List<EffectButtonItem> items, int type) {
        switch (type & SUB_MASK) {
            case TYPE_MAKEUP_LIP:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_fuguhong, new ComposerNode(TYPE_MAKEUP_LIP, "lip/fuguhong", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_shaonvfen, new ComposerNode(TYPE_MAKEUP_LIP, "lip/shaonvfen", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_yuanqiju, new ComposerNode(TYPE_MAKEUP_LIP, "lip/yuanqiju", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_xiyouse, new ComposerNode(TYPE_MAKEUP_LIP, "lip/xiyouse", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_xiguahong, new ComposerNode(TYPE_MAKEUP_LIP, "lip/xiguahong", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_sironghong, new ComposerNode(TYPE_MAKEUP_LIP, "lip/sironghong", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_zangjuse, new ComposerNode(TYPE_MAKEUP_LIP, "lip/zangjuse", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_meizise, new ComposerNode(TYPE_MAKEUP_LIP, "lip/meizise", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_shanhuse, new ComposerNode(TYPE_MAKEUP_LIP, "lip/shanhuse", "Internal_Makeup_Lips")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_lip, R.string.ck_lip_doushafen, new ComposerNode(TYPE_MAKEUP_LIP, "lip/doushafen", "Internal_Makeup_Lips")));
                break;
            case TYPE_MAKEUP_BLUSHER:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_weixunfen, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/weixun", "Internal_Makeup_Blusher")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_richang, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/richang", "Internal_Makeup_Blusher")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_mitao, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/mitao", "Internal_Makeup_Blusher")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_tiancheng, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/tiancheng", "Internal_Makeup_Blusher")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_qiaopi, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/qiaopi", "Internal_Makeup_Blusher")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_xinji, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/xinji", "Internal_Makeup_Blusher")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_blusher, R.string.ck_blusher_shaishang, new ComposerNode(TYPE_MAKEUP_BLUSHER, "blush/shaishang", "Internal_Makeup_Blusher")));
                break;
            case TYPE_MAKEUP_EYELASH:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyelash, R.string.ck_eyelash_nongmi, new ComposerNode(TYPE_MAKEUP_EYELASH, "eyelash/nongmi")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyelash, R.string.ck_eyelash_shanxing, new ComposerNode(TYPE_MAKEUP_EYELASH, "eyelash/shanxing")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyelash, R.string.ck_eyelash_wumei, new ComposerNode(TYPE_MAKEUP_EYELASH, "eyelash/wumei")));
                break;
            case TYPE_MAKEUP_PUPIL:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_hunxuezong, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/hunxuezong", "Internal_Makeup_Pupil")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_kekezong, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/kekezong", "Internal_Makeup_Pupil")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_mitaofen, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/mitaofen", "Internal_Makeup_Pupil")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_shuiguanghei, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/shuiguanghei", "Internal_Makeup_Pupil")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_xingkonglan, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/xingkonglan", "Internal_Makeup_Pupil")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_pupil, R.string.ck_pupil_chujianhui, new ComposerNode(TYPE_MAKEUP_PUPIL, "pupil/chujianhui", "Internal_Makeup_Pupil")));
                break;
            case TYPE_MAKEUP_HAIR:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_hair, R.string.ck_hair_anlan, new ComposerNode(TYPE_MAKEUP_HAIR, "hair/anlan", "")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_hair, R.string.ck_hair_molv, new ComposerNode(TYPE_MAKEUP_HAIR, "hair/molv", "")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_hair, R.string.ck_hair_shenzong, new ComposerNode(TYPE_MAKEUP_HAIR, "hair/shenzong", "")));
                break;
            case TYPE_MAKEUP_EYESHADOW:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_dadizong, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/dadizong", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_wanxiahong, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/wanxiahong", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_shaonvfen, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/shaonvfen", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_qizhifen, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/qizhifen", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_meizihong, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/meizihong", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_jiaotangzong, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/jiaotangzong", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_yuanqiju, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/yuanqiju", "Internal_Makeup_Eye")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eye, R.string.ck_eye_naichase, new ComposerNode(TYPE_MAKEUP_EYESHADOW, "eyeshadow/naichase", "Internal_Makeup_Eye")));
                break;
            case TYPE_MAKEUP_EYEBROW:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyebrow, R.string.ck_eyebrow_BRO1, new ComposerNode(TYPE_MAKEUP_EYEBROW, "eyebrow/BR01", "Internal_Makeup_Brow")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyebrow, R.string.ck_eyebrow_BKO1, new ComposerNode(TYPE_MAKEUP_EYEBROW, "eyebrow/BK01", "Internal_Makeup_Brow")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyebrow, R.string.ck_eyebrow_BKO2, new ComposerNode(TYPE_MAKEUP_EYEBROW, "eyebrow/BK02", "Internal_Makeup_Brow")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_eyebrow, R.string.ck_eyebrow_BKO3, new ComposerNode(TYPE_MAKEUP_EYEBROW, "eyebrow/BK03", "Internal_Makeup_Brow")));
                break;
            case TYPE_MAKEUP_FACIAL:
                items.add(new EffectButtonItem(R.drawable.ic_none, R.string.ck_close, new ComposerNode(TYPE_CLOSE)));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_facial, R.string.ck_facial_1, new ComposerNode(TYPE_MAKEUP_FACIAL, "facial/xiurong01", "Internal_Makeup_Facial")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_facial, R.string.ck_facial_2, new ComposerNode(TYPE_MAKEUP_FACIAL, "facial/xiurong02", "Internal_Makeup_Facial")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_facial, R.string.ck_facial_3, new ComposerNode(TYPE_MAKEUP_FACIAL, "facial/xiurong03", "Internal_Makeup_Facial")));
                items.add(new EffectButtonItem(R.drawable.ic_makeup_facial, R.string.ck_facial_4, new ComposerNode(TYPE_MAKEUP_FACIAL, "facial/xiurong04", "Internal_Makeup_Facial")));
                break;
        }
    }

    private String beautyNode() {
        return NODE_BEAUTY_CAMERA;
    }

    private String reshapeNode() {
        return NODE_RESHAPE_CAMERA;
    }
}
