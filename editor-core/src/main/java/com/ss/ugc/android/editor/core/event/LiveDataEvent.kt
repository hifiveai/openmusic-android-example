package com.ss.ugc.android.editor.core.event

import com.bytedance.ies.nle.editor_jni.NLETrack
import com.bytedance.ies.nle.editor_jni.NLETrackSlot

data class UpdateClipRangeEvent(val startTime: Long?, val endTime: Long?, val commit: Boolean = false)

data class AdjustClipRangeEvent(val slot: NLETrackSlot, val startDiff: Long, val duration: Long, val commit: Boolean = false)

data class MainTrackSlotMoveEvent(val nleTrackSlot: NLETrackSlot, val fromIndex: Int, val toIndex: Int)

data class MainTrackSlotClipEvent(val slot: NLETrackSlot, val start: Int, val duration: Int, val side: Int)

data class TrackSlotMoveEvent(val nleTrackSlot: NLETrackSlot, val fromTrackIndex: Int, val toTrackIndex: Int, val newStart: Long, val curPosition: Long)

data class TrackSlotClipEvent(val slot: NLETrackSlot, val startDiff: Long, val duration: Long)

data class TransitionTrackSlotClickEvent(val nleTrackSlot: NLETrackSlot?, val nextSegment: NLETrackSlot?)

data class SelectedTrackSlotEvent(val nleTrack: NLETrack?, val nleTrackSlot: NLETrackSlot?)
