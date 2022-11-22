package com.ss.ugc.android.editor.main.cover.imageCover

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

/**
 * @since 2019-08-08
 */
@Keep
//@Serializable(with = CutSameDataSerializer::class)
class CutSameData(
    // 素材ID，对应于草稿中的id
    var id: String = "",
    // 需要裁减的长度
    var duration: Long = 0,
    /**
     * 用于承载内容
     * 当mediaType为[0,1] 保存素材路径
     * 当mediaType为2     保存文本信息
     */
    var path: String = "",
    var text: String = "",
    /**
     * 所承载的信息
     * 0 图片
     * 1 视频
     * 2 文字
     * @see MediaConstant.MEDIA_TYPE_IMAGE
     * @see MediaConstant.MEDIA_TYPE_VIDEO
     * @see MediaConstant.MEDIA_TYPE_TEXT
     */
//    var mediaType: Int = TYPE_IMAGE,
    var mediaType: Int = 0,
    // 判断是否锁定为不可修改
    var lock: Boolean = false,
    var seted: Boolean = false,

    // 开始时间，裁减后的开始时间
    var start: Long = 0L,
    // 草稿中的长宽
    var width: Int = Int.MAX_VALUE,
    var height: Int = Int.MAX_VALUE,
    // 在模板中的开始时间
    var videoStartFrame: Int = 0,
    // 变换参数
    var translateX: Float = 0f,
    var translateY: Float = 0f,
    // 用于与裁减框对比的缩放因子
    var scaleFactor: Float = 1f,

    /**
     * VE左上角 X 偏移比例
     */
    var veTranslateLUX: Float = 0F,
    /**
     * VE左上角 Y 偏移比例
     */
    var veTranslateLUY: Float = 0F,

    /**
     * VE右下角 X 偏移比例
     */
    var veTranslateRDX: Float = 1F,
    /**
     * VE右下角 Y 偏移比例
     */
    var veTranslateRDY: Float = 1F,

    /**
     * 0 为跟画布
     * 1 为跟视频
     * @see MediaConstant.EDIT_TYPE_CANVAS
     * @see MediaConstant.EDIT_TYPE_VIDEO
     */
    var editType: Int = 0,
    /**
     *  是否画中画
     */
    var isSubVideo: Boolean = false,

    // 是否来自拍摄
    var isFromRecord: Boolean = false,
    // 视频总时长
    var totalDuration: Long = 0L,

    // 关联视频分组
    var relationVideoGroup: String = "",

    // 是否漫画效果
    var isCartoon: Boolean = false,

    // 原素材路径
    var sourcePath: String = "",

    // 漫画素材路径
    var cartoonPath: String = "",

    // 音量
    var volume: Float = 0F,

    // 是否有关键帧
    var hasKeyframe: Boolean = false,

    // 道具特效信息
    var propsInfoJson: String = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
//        parcel.readParcelable(RectF::class.java.classLoader) ?: RectF(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: ""
    )

    @Synchronized
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(duration)
        parcel.writeString(path)
        parcel.writeString(text)
        parcel.writeInt(mediaType)
        parcel.writeByte(if (lock) 1 else 0)
        parcel.writeByte(if (seted) 1 else 0)
        parcel.writeLong(start)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeInt(videoStartFrame)
        parcel.writeFloat(translateX)
        parcel.writeFloat(translateY)
        parcel.writeFloat(scaleFactor)
        parcel.writeFloat(veTranslateLUX)
        parcel.writeFloat(veTranslateLUY)
        parcel.writeFloat(veTranslateRDX)
        parcel.writeFloat(veTranslateRDY)
//        parcel.writeParcelable(clip, flags)
        parcel.writeInt(editType)
        parcel.writeByte(if (isSubVideo) 1 else 0)
        parcel.writeByte(if (isFromRecord) 1 else 0)
        parcel.writeLong(totalDuration)
        parcel.writeString(relationVideoGroup)
        parcel.writeByte(if (isCartoon) 1 else 0)
        parcel.writeString(sourcePath)
        parcel.writeString(cartoonPath)
        parcel.writeFloat(volume)
        parcel.writeByte(if (hasKeyframe) 1 else 0)
        parcel.writeString(propsInfoJson)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CutSameData

        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<CutSameData> {
        @Synchronized
        override fun createFromParcel(parcel: Parcel): CutSameData {
            return CutSameData(
                parcel
            )
        }

        @Synchronized
        override fun newArray(size: Int): Array<CutSameData?> {
            return arrayOfNulls(size)
        }
    }
}

//@Serializer(forClass = CutSameData::class)
//private object CutSameDataSerializer : KSerializer<CutSameData> {
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CutSameData") {
//        element("id", String.serializer().descriptor, isOptional = true)
//        element("a", String.serializer().descriptor, isOptional = true)
//        element("duration", Long.serializer().descriptor, isOptional = true)
//        element("b", Long.serializer().descriptor, isOptional = true)
//        element("path", String.serializer().descriptor, isOptional = true)
//        element("c", String.serializer().descriptor, isOptional = true)
//        element("text", String.serializer().descriptor, isOptional = true)
//        element("d", String.serializer().descriptor, isOptional = true)
//        element("mediaType", Int.serializer().descriptor, isOptional = true)
//        element("e", Int.serializer().descriptor, isOptional = true)
//        element("lock", Boolean.serializer().descriptor, isOptional = true)
//        element("f", Boolean.serializer().descriptor, isOptional = true)
//        element("seted", Boolean.serializer().descriptor, isOptional = true)
//        element("g", Boolean.serializer().descriptor, isOptional = true)
//        element("start", Long.serializer().descriptor, isOptional = true)
//        element("h", Long.serializer().descriptor, isOptional = true)
//        element("width", Int.serializer().descriptor, isOptional = true)
//        element("i", Int.serializer().descriptor, isOptional = true)
//        element("height", Int.serializer().descriptor, isOptional = true)
//        element("j", Int.serializer().descriptor, isOptional = true)
//        element("videoStartFrame", Int.serializer().descriptor, isOptional = true)
//        element("k", Int.serializer().descriptor, isOptional = true)
//        element("translateX", Float.serializer().descriptor, isOptional = true)
//        element("l", Float.serializer().descriptor, isOptional = true)
//        element("translateY", Float.serializer().descriptor, isOptional = true)
//        element("m", Float.serializer().descriptor, isOptional = true)
//        element("scaleFactor", Float.serializer().descriptor, isOptional = true)
//        element("n", Float.serializer().descriptor, isOptional = true)
//        element("veTranslateLUX", Float.serializer().descriptor, isOptional = true)
//        element("o", Float.serializer().descriptor, isOptional = true)
//        element("veTranslateLUY", Float.serializer().descriptor, isOptional = true)
//        element("p", Float.serializer().descriptor, isOptional = true)
//        element("veTranslateRDX", Float.serializer().descriptor, isOptional = true)
//        element("q", Float.serializer().descriptor, isOptional = true)
//        element("veTranslateRDY", Float.serializer().descriptor, isOptional = true)
//        element("r", Float.serializer().descriptor, isOptional = true)
//        element("editType", Int.serializer().descriptor, isOptional = true)
//        element("t", Int.serializer().descriptor, isOptional = true)
//        element("isSubVideo", Boolean.serializer().descriptor, isOptional = true)
//        element("u", Boolean.serializer().descriptor, isOptional = true)
//        element("isFromRecord", Boolean.serializer().descriptor, isOptional = true)
//        element("v", Boolean.serializer().descriptor, isOptional = true)
//        element("totalDuration", Long.serializer().descriptor, isOptional = true)
//        element("relationVideoGroup", String.serializer().descriptor, isOptional = true)
//        element("isCartoon", Boolean.serializer().descriptor, isOptional = true)
//        element("sourcePath", String.serializer().descriptor, isOptional = true)
//        element("volume", Float.serializer().descriptor, isOptional = true)
//        element("hasKeyframe", Boolean.serializer().descriptor, isOptional = true)
//        element("propsInfoJson", String.serializer().descriptor, isOptional = true)
//    }
//
//    override fun serialize(encoder: Encoder, value: CutSameData) {
//        val output = encoder.beginStructure(descriptor)
//        output.encodeStringElement(descriptor, 0, value.id)
//        output.encodeLongElement(descriptor, 2, value.duration)
//        output.encodeStringElement(descriptor, 4, value.path)
//        output.encodeStringElement(descriptor, 6, value.text)
//        output.encodeIntElement(descriptor, 8, value.mediaType)
//        output.encodeBooleanElement(descriptor, 10, value.lock)
//        output.encodeBooleanElement(descriptor, 12, value.seted)
//        output.encodeLongElement(descriptor, 14, value.start)
//        output.encodeIntElement(descriptor, 16, value.width)
//        output.encodeIntElement(descriptor, 18, value.height)
//        output.encodeIntElement(descriptor, 20, value.videoStartFrame)
//        output.encodeFloatElement(descriptor, 22, value.translateX)
//        output.encodeFloatElement(descriptor, 24, value.translateY)
//        output.encodeFloatElement(descriptor, 26, value.scaleFactor)
//        output.encodeFloatElement(descriptor, 28, value.veTranslateLUX)
//        output.encodeFloatElement(descriptor, 30, value.veTranslateLUY)
//        output.encodeFloatElement(descriptor, 32, value.veTranslateRDX)
//        output.encodeFloatElement(descriptor, 34, value.veTranslateRDY)
//        output.encodeIntElement(descriptor, 36, value.editType)
//        output.encodeBooleanElement(descriptor, 38, value.isSubVideo)
//        output.encodeBooleanElement(descriptor, 40, value.isFromRecord)
//        output.encodeLongElement(descriptor, 42, value.totalDuration)
//        output.encodeStringElement(descriptor, 43, value.relationVideoGroup)
//        output.encodeBooleanElement(descriptor, 44, value.isCartoon)
//        output.encodeStringElement(descriptor, 45, value.sourcePath)
//        output.encodeFloatElement(descriptor, 46, value.volume)
//        output.encodeBooleanElement(descriptor, 47, value.hasKeyframe)
//        output.encodeStringElement(descriptor, 48, value.propsInfoJson)
//        output.endStructure(descriptor)
//    }
//
//    @Suppress("ComplexMethod")
//    override fun deserialize(decoder: Decoder): CutSameData {
//        val dec = decoder.beginStructure(descriptor)
//        var id = ""
//        var duration: Long = 0
//        var path = ""
//        var text = ""
//        var mediaType: Int = TYPE_IMAGE
//        var lock = false
//        var seted = false
//        var start = 0L
//        var width: Int = Int.MAX_VALUE
//        var height: Int = Int.MAX_VALUE
//        var videoStartFrame = 0
//        var translateX = 0f
//        var translateY = 0f
//        var scaleFactor = 1f
//        var veTranslateLUX = 0F
//        var veTranslateLUY = 0F
//        var veTranslateRDX = 1F
//        var veTranslateRDY = 1F
//        var editType = 0
//        var isSubVideo = false
//        var isFromRecord = false
//        var totalDuration = 0L
//        var relationVideoGroup = ""
//        var isCartoon = false
//        var sourcePath = ""
//        var volume = 0F
//        var hasKeyframe = false
//        var propsInfoJson = ""
//
//        loop@ while (true) {
//            when (val i = dec.decodeElementIndex(descriptor)) {
//                CompositeDecoder.DECODE_DONE -> break@loop
//                0, 1 -> id = dec.decodeStringElement(descriptor, i)
//                2, 3 -> duration = dec.decodeLongElement(descriptor, i)
//                4, 5 -> path = dec.decodeStringElement(descriptor, i)
//                6, 7 -> text = dec.decodeStringElement(descriptor, i)
//                8, 9 -> mediaType = dec.decodeIntElement(descriptor, i)
//                10, 11 -> lock = dec.decodeBooleanElement(descriptor, i)
//                12, 13 -> seted = dec.decodeBooleanElement(descriptor, i)
//                14, 15 -> start = dec.decodeLongElement(descriptor, i)
//                16, 17 -> width = dec.decodeIntElement(descriptor, i)
//                18, 19 -> height = dec.decodeIntElement(descriptor, i)
//                20, 21 -> videoStartFrame = dec.decodeIntElement(descriptor, i)
//                22, 23 -> translateX = dec.decodeFloatElement(descriptor, i)
//                24, 25 -> translateY = dec.decodeFloatElement(descriptor, i)
//                26, 27 -> scaleFactor = dec.decodeFloatElement(descriptor, i)
//                28, 29 -> veTranslateLUX = dec.decodeFloatElement(descriptor, i)
//                30, 31 -> veTranslateLUY = dec.decodeFloatElement(descriptor, i)
//                32, 33 -> veTranslateRDX = dec.decodeFloatElement(descriptor, i)
//                34, 35 -> veTranslateRDY = dec.decodeFloatElement(descriptor, i)
//                36, 37 -> editType = dec.decodeIntElement(descriptor, i)
//                38, 39 -> isSubVideo = dec.decodeBooleanElement(descriptor, i)
//                40, 41 -> isFromRecord = dec.decodeBooleanElement(descriptor, i)
//                42 -> totalDuration = dec.decodeLongElement(descriptor, i)
//                43 -> relationVideoGroup = dec.decodeStringElement(descriptor, i)
//                44 -> isCartoon = dec.decodeBooleanElement(descriptor, i)
//                45 -> sourcePath = dec.decodeStringElement(descriptor, i)
//                46 -> volume = dec.decodeFloatElement(descriptor, i)
//                47 -> hasKeyframe = dec.decodeBooleanElement(descriptor, i)
//                48 -> propsInfoJson = dec.decodeStringElement(descriptor, i)
//            }
//        }
//        dec.endStructure(descriptor)
//        return CutSameData(
//            id,
//            duration,
//            path,
//            text,
//            mediaType,
//            lock,
//            seted,
//            start,
//            width,
//            height,
//            videoStartFrame,
//            translateX,
//            translateY,
//            scaleFactor,
//            veTranslateLUX,
//            veTranslateLUY,
//            veTranslateRDX,
//            veTranslateRDY,
//            editType,
//            isSubVideo,
//            isFromRecord,
//            totalDuration,
//            relationVideoGroup,
//            isCartoon,
//            sourcePath,
//            volume = volume,
//            hasKeyframe = hasKeyframe,
//            propsInfoJson = propsInfoJson
//        )
//    }
//}
