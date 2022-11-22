package com.ss.ugc.android.editor.bottom.videoeffect;

import com.bytedance.ies.nle.editor_jni.NLETrack;
import com.bytedance.ies.nle.editor_jni.NLETrackSlot;
import com.bytedance.ies.nle.editor_jni.NLETrackType;
import com.ss.ugc.android.editor.bottom.R;

/**
 * @author bytedance
 */
public class EffectApplyItem {
    private int type;
    private NLETrack mTrack;
    private NLETrackSlot mSlot;
    public EffectApplyItem(NLETrack mTrack, NLETrackSlot slot) {
        this.mTrack = mTrack;
        this.mSlot = slot;
    }

    public NLETrackSlot getSlot() {
        return mSlot;
    }

    public int getTrackNameResId() {
        if (mTrack.getTrackType() == NLETrackType.VIDEO) {
            if (mTrack.getMainTrack()) {
                return R.string.ck_main_track_video;
            } else {
                return R.string.ck_pip;
            }
        } else {
            return R.string.ck_global;
        }
    }

    public NLETrack getTrack() {
        return mTrack;
    }

    public int getType() {
        if (mTrack != null) {
            if (mTrack.getExtraTrackType() == NLETrackType.EFFECT) {
               return MaterialEffect.APPLY_TYPE_ALL;
            }  else if (!mTrack.getMainTrack()) {
                return MaterialEffect.APPLY_TYPE_SUB;
            }
        }
        return MaterialEffect.APPLY_TYPE_MAIN;
    }
}
