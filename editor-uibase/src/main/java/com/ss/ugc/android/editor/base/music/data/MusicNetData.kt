package com.ss.ugc.android.editor.base.music.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.ss.ugc.android.editor.base.network.BaseResponse
import java.io.Serializable

@Keep
data class MusicListRequest(
    val id: String,
    val pageIndex: Int,
    val pageSize: Int
)

@Keep
open class MusicListResponse : BaseResponse<MusicListModel>()

@Keep
open class MusicCollectionsResponse : BaseResponse<MusicCollectionModel>()

@Keep
data class MusicListModel(
    @SerializedName("has_more")
    val hasMore: Boolean = true,
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 0,
    val list: List<MusicItem> = emptyList()
) : Serializable

@Keep
data class MusicCollectionModel(
    val collections: List<MusicCollection> = emptyList()
) : Serializable

@Keep
data class MusicCollection(
    val id: String,
    val name: String,
    @SerializedName("name_en")
    val nameEn: String
) : Serializable