package com.ss.ugc.android.editor.base.model;

import android.util.SparseArray;
import android.util.SparseIntArray;

/**
 */
public class EffectBackup {
    private boolean enable;
    private SparseIntArray mSelectMap;
    private SparseArray<Float> mProgressMap;
    private int mSelectType;
    private String mSavedFilterPath;

    public void backup(SparseIntArray selectMap, SparseArray<Float> progressMap, int selectType, String savedFilterPath) {
        mSelectMap = selectMap.clone();
        mProgressMap = progressMap.clone();
        mSelectType = selectType;
        mSavedFilterPath = savedFilterPath;
        enable = true;
    }

    public SparseIntArray getSelectMap() {
        return mSelectMap;
    }

    public SparseArray<Float> getProgressMap() {
        return mProgressMap;
    }

    public int getSelectType() {
        return mSelectType;
    }

    public String getSavedFilterPath() {
        return mSavedFilterPath;
    }

    public boolean isEnable() {
        boolean able = enable;
        enable = false;
        return able;
    }
}
