package com.ss.ugc.android.editor.picker.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import com.ss.ugc.android.editor.picker.data.model.LocalMediaCategory
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.data.model.MediaType
import com.ss.ugc.android.editor.picker.data.model.QueryParam

class MaterialDataRepository(
    private val imageQueryParams: QueryParam,
    private val videoQueryParams: QueryParam
) {
    fun getMaterial(
        context: Context,
        type: LocalMediaCategory.Type,
    ): List<MediaItem> {
        return when (type) {
            LocalMediaCategory.ALL -> getAllMaterial(context, imageQueryParams, videoQueryParams)
            LocalMediaCategory.IMAGE -> parseCursor(
                getMaterialCursor(context, imageQueryParams),
                imageQueryParams.mediaType
            )
            LocalMediaCategory.VIDEO -> parseCursor(
                getMaterialCursor(context, videoQueryParams),
                videoQueryParams.mediaType
            )
            else -> getAllMaterial(context, imageQueryParams, videoQueryParams)
        }
    }

    private fun getAllMaterial(
        context: Context,
        imageQueryParams: QueryParam,
        videoQueryParams: QueryParam
    ): List<MediaItem> {
        val resultList = mutableListOf<MediaItem>()
        resultList.addAll(
            parseCursor(
                getMaterialCursor(context, imageQueryParams),
                imageQueryParams.mediaType
            )
        )
        resultList.addAll(
            parseCursor(
                getMaterialCursor(context, videoQueryParams),
                videoQueryParams.mediaType
            )
        )
        return resultList
    }

    private fun parseCursor(cursor: Cursor, mediaType: MediaType): List<MediaItem> = cursor.run {
        val resultList = mutableListOf<MediaItem>()
        val idIndex = getColumnIndex(MediaStore.MediaColumns._ID)
        val pathIndex = getColumnIndex(MediaStore.MediaColumns.DATA)
        val mimeTypeIndex = getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
        val sizeIndex = getColumnIndex(MediaStore.MediaColumns.SIZE)
        val dateAddIndex = getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
        val widthIndex = getColumnIndex(MediaStore.MediaColumns.WIDTH)
        val heightIndex = getColumnIndex(MediaStore.MediaColumns.HEIGHT)
        val durationIndex = getColumnIndex(MediaStore.Video.Media.DURATION)

        if (idIndex == -1 || mimeTypeIndex == -1) {
            // 必要字段
            emptyList()
        } else {
            while (cursor.moveToNext()) {
                val id = getInt(idIndex)
                val path = getString(pathIndex)
                val mimeType = getString(mimeTypeIndex)
                val size = getLong(sizeIndex)
                val dateAdd = getLong(dateAddIndex)
                val width = getInt(widthIndex)
                val height = getInt(heightIndex)
                val duration = if (durationIndex == -1) 0 else getLong(durationIndex)

                val uri = ContentUris.withAppendedId(
                    when (mediaType) {
                        MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    },
                    id.toLong()
                )

                val mediaItem = MediaItem(
                    type = mediaType,
                    mimeType = mimeType,
                    path = path,
                    uri = uri,
                    dateAdd = dateAdd,
                    displayName = id.toString(),
                    id = id.toLong(),
                    width = width,
                    height = height,
                    size = size,
                    duration = duration
                )

                //过滤掉无效的数据
                if (size != 0L) {
                    resultList.add(
                        mediaItem
                    )
                }
            }
            cursor.close()
            resultList
        }
    }

    private fun getMaterialCursor(
        context: Context,
        queryParam: QueryParam,
        sortOrder: String? = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
    ): Cursor {
        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
        ).apply {
            if (queryParam.mediaType == MediaType.VIDEO) {
                add(MediaStore.Video.Media.DURATION)
            }
        }

        return ContentResolverCompat.query(
            context.contentResolver,
            queryParam.mediaType.contentUri,
            projection.toTypedArray(),
            queryParam.selectionString(),
            queryParam.mimeTypeSelectionArgs,
            sortOrder,
            null
        )
    }
}