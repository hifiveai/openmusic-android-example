package com.vesdk.verecorder.record.demo;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.util.SparseIntArray;


import com.vesdk.vebase.demo.model.ComposerNode;
import com.vesdk.vebase.demo.model.EffectButtonItem;
import com.vesdk.vebase.demo.present.ItemGetPresenter;
import com.vesdk.vebase.demo.present.contract.ItemGetContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vesdk.vebase.demo.present.contract.ItemGetContract.MASK;
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
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_CHEEK;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_CHIN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE_MOVE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE_PLUMP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE_ROTATE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE_SPACING;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_FACE_CUT;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_FACE_OVERALL;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_FACE_SMALL;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_FOREHEAD;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_JAW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_MOUTH_MOVE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_MOUTH_SMILE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_NOSE_LEAN;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_NOSE_LONG;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_REMOVE_POUCH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_SMILE_FOLDS;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_WHITEN_TEETH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_CLOSE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_FILTER;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_BLUSHER;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_DEFAULT;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYEBROW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYESHADOW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_FACIAL;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_HAIR;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_LIP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_OPTION;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_PUPIL;

/**
 *  on 2019-07-22 13:57
 */
public class EffectPresenter extends EffectContract.Presenter {
    private static final Map<Integer, Float> DEFAULT_CAMERA_VALUE;
    private static final Map<Integer, Float> DEFAULT_LIVE_VALUE;
    static {
        @SuppressLint("UseSparseArrays") Map<Integer, Float> cameraMap = new HashMap<>();
        @SuppressLint("UseSparseArrays") Map<Integer, Float> liveMap = new HashMap<>();
        // 美颜
        // beauty face
        cameraMap.put(TYPE_BEAUTY_FACE_SMOOTH, 0.5F); // 磨皮
        cameraMap.put(TYPE_BEAUTY_FACE_WHITEN, 0.35F); //美白
        cameraMap.put(TYPE_BEAUTY_FACE_SHARPEN, 0.3F); //锐化

        cameraMap.put(TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE, 0.35F); //亮眼
        cameraMap.put(TYPE_BEAUTY_RESHAPE_REMOVE_POUCH, 0.35F); //黑眼圈
        cameraMap.put(TYPE_BEAUTY_RESHAPE_SMILE_FOLDS, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_WHITEN_TEETH, 0.35F); //白牙
        // 美型
        // beauty reshape
        cameraMap.put(TYPE_BEAUTY_RESHAPE_FACE_OVERALL, 0.35F); //瘦脸
        cameraMap.put(TYPE_BEAUTY_RESHAPE_FACE_SMALL, 0.35F); //小脸
        cameraMap.put(TYPE_BEAUTY_RESHAPE_FACE_CUT, 0.2F); //窄脸
        cameraMap.put(TYPE_BEAUTY_RESHAPE_EYE, 0.35F);//大眼
        cameraMap.put(TYPE_BEAUTY_RESHAPE_EYE_ROTATE, 0.35F); //嘴型
        cameraMap.put(TYPE_BEAUTY_RESHAPE_CHEEK, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_JAW, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_NOSE_LEAN, 0.35F); //瘦鼻
        cameraMap.put(TYPE_BEAUTY_RESHAPE_NOSE_LONG, 0.25F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_CHIN, 0.35F); //下巴
        cameraMap.put(TYPE_BEAUTY_RESHAPE_FOREHEAD, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_MOUTH_SMILE, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_EYE_SPACING, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_EYE_MOVE, 0.0F);
        cameraMap.put(TYPE_BEAUTY_RESHAPE_MOUTH_MOVE, 0.0F);
        // 美体
        cameraMap.put(TYPE_BEAUTY_BODY_THIN, 0.2f);
        cameraMap.put(TYPE_BEAUTY_BODY_LONG_LEG, 0.2f);
        cameraMap.put(TYPE_BEAUTY_BODY_SLIM_LEG, 0.2f);
        cameraMap.put(TYPE_BEAUTY_BODY_SHRINK_HEAD, 0.2f);
        cameraMap.put(TYPE_BEAUTY_BODY_SLIM_WAIST, 0.2f);
        cameraMap.put(TYPE_BEAUTY_BODY_ENLARGE_BREAST, 0.2f);
        cameraMap.put(TYPE_BEAUTY_BODY_ENHANCE_HIP, 0.5f); // 美胯
        cameraMap.put(TYPE_BEAUTY_BODY_ENHANCE_NECK, 0.2f); //天鹅颈
        cameraMap.put(TYPE_BEAUTY_BODY_SLIM_ARM, 0.2f); //瘦手臂

        // 美妆
        cameraMap.put(TYPE_MAKEUP_LIP, 0.2F); //口红 50
        cameraMap.put(TYPE_MAKEUP_HAIR, 0.2F); //头发
        cameraMap.put(TYPE_MAKEUP_BLUSHER, 0.2F); //腮红 20
        cameraMap.put(TYPE_MAKEUP_FACIAL, 0.35F); // 修容 35
        cameraMap.put(TYPE_MAKEUP_EYEBROW, 0.35F); // 眉毛 35
        cameraMap.put(TYPE_MAKEUP_EYESHADOW, 0.35F); // 眼影35
        cameraMap.put(TYPE_MAKEUP_PUPIL, 0.4F);// 美瞳 40
        // 滤镜
        // filter
        cameraMap.put(TYPE_FILTER, 0.8F);
        DEFAULT_CAMERA_VALUE = Collections.unmodifiableMap(cameraMap);

        // 美颜
        // beauty face
        liveMap.put(TYPE_BEAUTY_FACE_SMOOTH, 0.5F);
        liveMap.put(TYPE_BEAUTY_FACE_WHITEN, 0.35F);
        liveMap.put(TYPE_BEAUTY_FACE_SHARPEN, 0.3F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE, 0.5F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_REMOVE_POUCH, 0.5F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_SMILE_FOLDS, 0.35F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_WHITEN_TEETH, 0.35F);
        // 美型
        // beaury reshape
        liveMap.put(TYPE_BEAUTY_RESHAPE_FACE_OVERALL, 0.35F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_FACE_SMALL, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_FACE_CUT, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_EYE, 0.35F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_EYE_ROTATE, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_CHEEK, 0.2F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_JAW, 0.4F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_NOSE_LEAN, 0.2F); //瘦鼻
        liveMap.put(TYPE_BEAUTY_RESHAPE_NOSE_LONG, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_CHIN, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_FOREHEAD, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM, 0.15F); //嘴型
        liveMap.put(TYPE_BEAUTY_RESHAPE_MOUTH_SMILE, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_EYE_SPACING, 0.15F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_EYE_MOVE, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_MOUTH_MOVE, 0.0F);
        liveMap.put(TYPE_BEAUTY_RESHAPE_EYE_PLUMP, 0.35F);
        // 美体
        liveMap.put(TYPE_BEAUTY_BODY_THIN, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_LONG_LEG, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_SLIM_LEG, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_SHRINK_HEAD, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_SLIM_WAIST, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_ENLARGE_BREAST, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_ENHANCE_HIP, 0.5f);
        liveMap.put(TYPE_BEAUTY_BODY_ENHANCE_NECK, 0.0f);
        liveMap.put(TYPE_BEAUTY_BODY_SLIM_ARM, 0.0f);

        // 美妆
        liveMap.put(TYPE_MAKEUP_LIP, 0.5F);
        liveMap.put(TYPE_MAKEUP_HAIR, 0.5F);
        liveMap.put(TYPE_MAKEUP_BLUSHER, 0.2F);
        liveMap.put(TYPE_MAKEUP_FACIAL, 0.35F);
        liveMap.put(TYPE_MAKEUP_EYEBROW, 0.35F);
        liveMap.put(TYPE_MAKEUP_EYESHADOW, 0.35F);
        liveMap.put(TYPE_MAKEUP_PUPIL, 0.4F);
        // 滤镜
        // filter
        liveMap.put(TYPE_FILTER, 0.8F);
        DEFAULT_LIVE_VALUE = Collections.unmodifiableMap(liveMap);
    }

    private ItemGetContract.Presenter mItemGet;

    @Override
    public void removeNodesOfType(SparseArray<ComposerNode> composerNodeMap, int type) {
        removeNodesWithMakAndType(composerNodeMap, MASK, type & MASK);
    }

    @Override
    public void removeProgressInMap(SparseArray<Float> map, int type) {
        List<Integer> nodeToRemove = new ArrayList<>(map.size());
        for (int i = 0; i < map.size(); i++) {
            int key = map.keyAt(i);
            if ((key & MASK) == type) {
                nodeToRemove.add(key);
            }
        }
        for (Integer i : nodeToRemove) {
            map.remove(i);
        }
    }

    @Override
    public void removeTypeInMap(SparseIntArray map, int type) {
        List<Integer> nodeToRemove = new ArrayList<>(map.size());
        for (int i = 0; i < map.size(); i++) {
            int key = map.keyAt(i);
            if ((key & MASK) == type) {
                nodeToRemove.add(key);
            }
        }
        for (Integer i : nodeToRemove) {
            map.delete(i);
        }
    }

    private void removeNodesWithMakAndType(SparseArray<ComposerNode> map, int mask, int type) {
        int i = 0;
        ComposerNode node;
        while (i < map.size() && (node = map.valueAt(i)) != null) {
            if ((node.getId() & mask) == type) {
                map.removeAt(i);
            } else {
                i++;
            }
        }
    }

    @Override
    public String[] generateComposerNodes(SparseArray<ComposerNode> composerNodeMap) {
        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (int i = 0; i < composerNodeMap.size(); i++) {
            ComposerNode node = composerNodeMap.valueAt(i);
            if (set.contains(node.getNode())) {
                continue;
            } else {
                set.add(node.getNode());
            }
            if (isAhead(node)) {
                list.add(0, node.getNode());
            } else {
                list.add(node.getNode());
            }
        }

        return list.toArray(new String[0]);
    }

    @Override
    public void generateDefaultBeautyNodes(SparseArray<ComposerNode> composerNodeMap) {
        if (mItemGet == null) {
            mItemGet = new ItemGetPresenter();
            mItemGet.attachView(getView());
        }
        List<EffectButtonItem> beautyItems = new ArrayList<>();
        beautyItems.addAll(mItemGet.getItems(TYPE_BEAUTY_FACE));
//        beautyItems.addAll(mItemGet.getItems(TYPE_BEAUTY_RESHAPE));

        for (EffectButtonItem item : beautyItems) {
            if (item.getNode().getId() == TYPE_CLOSE) {
                continue;
            }
            if(getDefaultMap().containsKey(item.getNode().getId())) {
                item.getNode().setValue(getDefaultMap().get(item.getNode().getId()));
                composerNodeMap.put(item.getNode().getId(), item.getNode());
            }
        }
    }

    @Override
    public SparseArray<ComposerNode> addDefaultBodyNodes(SparseArray<ComposerNode> composerNodeMap) {
        if (mItemGet == null) {
            mItemGet = new ItemGetPresenter();
            mItemGet.attachView(getView());
        }

        List<EffectButtonItem> beautyItems = new ArrayList<>();
        beautyItems.addAll(mItemGet.getItems(TYPE_BEAUTY_BODY));

        for (EffectButtonItem item : beautyItems) {
            if (item.getNode().getId() == TYPE_CLOSE) {
                continue;
            }
            if(getDefaultMap().containsKey(item.getNode().getId())) {
                item.getNode().setValue(getDefaultMap().get(item.getNode().getId()));
                composerNodeMap.put(item.getNode().getId(), item.getNode());
            }
        }
        return composerNodeMap;
    }

    @Override
    public SparseArray<ComposerNode> addDefaultReshapeNodes(SparseArray<ComposerNode> composerNodeMap) {
        if (mItemGet == null) {
            mItemGet = new ItemGetPresenter();
            mItemGet.attachView(getView());
        }

        List<EffectButtonItem> beautyItems = new ArrayList<>();
        beautyItems.addAll(mItemGet.getItems(TYPE_BEAUTY_RESHAPE));

        for (EffectButtonItem item : beautyItems) {
            if (item.getNode().getId() == TYPE_CLOSE) {
                continue;
            }
            if(getDefaultMap().containsKey(item.getNode().getId())) {
                item.getNode().setValue(getDefaultMap().get(item.getNode().getId()));
                composerNodeMap.put(item.getNode().getId(), item.getNode());
            }
        }
        return composerNodeMap;
    }

    @Override
    public SparseArray<ComposerNode> addDefaultMakeupNodes(SparseArray<ComposerNode> composerNodeMap) {
        if (mItemGet == null) {
            mItemGet = new ItemGetPresenter();
            mItemGet.attachView(getView());
        }

        List<EffectButtonItem> beautyItems = new ArrayList<>();
        beautyItems.addAll(mItemGet.getItems(TYPE_MAKEUP_DEFAULT));

        for (EffectButtonItem item : beautyItems) {
            if (item.getNode().getId() == TYPE_CLOSE) {
                continue;
            }
            if(getDefaultMap().containsKey(item.getNode().getId())) {
                item.getNode().setValue(getDefaultMap().get(item.getNode().getId()));
                composerNodeMap.put(item.getNode().getId(), item.getNode());
            }
        }
        return composerNodeMap;
    }

    @Override
    public float getDefaultValue(int type) {
        Float value =  getDefaultMap().get(type);
        return value == null ? 0F : value;
    }

    @Override
    public boolean hasIntensity(int type) {
        int parent = type & MASK;
        return parent == TYPE_BEAUTY_FACE || parent == TYPE_BEAUTY_RESHAPE ||
                parent == TYPE_BEAUTY_BODY || parent == TYPE_MAKEUP || parent == TYPE_MAKEUP_OPTION;
    }

    private boolean isAhead(ComposerNode node) {
        return (node.getId() & MASK) == TYPE_MAKEUP_OPTION;
    }

    private Map<Integer, Float> getDefaultMap() {
        PreviewActivity.EffectType type = PreviewActivity.EffectType.CAMERA;
        if (getView() != null && getView().getEffectType() != null) {
            type = getView().getEffectType();
        }
        switch (type) {
            case VIDEO:
                return DEFAULT_LIVE_VALUE;
            case CAMERA:
                return DEFAULT_CAMERA_VALUE;
        }
        return DEFAULT_CAMERA_VALUE;
    }
}
