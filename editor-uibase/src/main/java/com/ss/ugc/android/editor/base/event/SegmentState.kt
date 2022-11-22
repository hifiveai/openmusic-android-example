package com.ss.ugc.android.editor.base.event

import com.ss.ugc.android.editor.base.data.SegmentInfo


/**
 * time : 2020/12/30
 *
 * description :
 *
 */
class SegmentState(val segment: SegmentInfo, var newAdd: Boolean = false) {
    companion object {
        fun empty() = SegmentState(SegmentInfo(null, null))
    }
}