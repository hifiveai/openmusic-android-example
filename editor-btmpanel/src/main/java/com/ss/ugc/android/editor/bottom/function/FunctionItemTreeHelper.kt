package com.ss.ugc.android.editor.bottom.function

import android.text.TextUtils
import android.util.Log
import com.ss.ugc.android.editor.base.functions.FunctionItem
import java.util.*
import kotlin.collections.ArrayList

/**
 * @date: 2021/3/31
 */

const val ROOT_ITEM_TYPE = "root_item"

class FunctionItemTreeHelper(val rootFunctionItemList: ArrayList<FunctionItem>) {

    private val rootItem = FunctionItem
        .Builder()
        .type(ROOT_ITEM_TYPE)
        .children(rootFunctionItemList)
        .build()

    fun addFuncItemToRoot(index: Int, funcItem: FunctionItem): Boolean {
        return if (index >= 0 && index <= rootFunctionItemList.size) {
            rootFunctionItemList.add(index, funcItem)
            true
        } else {
            Log.e("DavinciEditor", "index = $index is out of index, rootFunctionItemList size is ${rootFunctionItemList.size}")
            false
        }
    }

    fun removeFuncItemFromRootAt(index: Int): Boolean {
        return if (index >= 0 && index < rootFunctionItemList.size) {
            rootFunctionItemList.removeAt(index)
            true
        } else {
            false
        }
    }

    fun removeFuncItemFromRootByType(childType: String): Boolean {
        val findChildItem = rootFunctionItemList.firstOrNull {
            it.type === childType
        }
        if (findChildItem == null) {
            Log.e("DEditor", "removeFuncItemFromRootByType:: findChildItem is null")
            return false
        }
        return rootFunctionItemList.remove(findChildItem)
    }

    fun addFuncItemToParent(parentItem: FunctionItem, childItem: FunctionItem, index: Int): Boolean {
        //有几种情况
        val childList = parentItem.getChildList()
        if (childList.isNotEmpty()) {
            //1.如果之前存在children，需要检查index，防止数组越界
            return if (index >= 0 && index <= childList.size) {
                childList.add(index, childItem)
                true
            } else {
                Log.e("DavinciEditor", "index = $index is out of index, childList size is ${childList.size}")
                false
            }
        } else {
//            //2.如果之前不不存在children，则需要将parentItem添加到第一个位置（clone出一个不带children的FunctionItem）
//            childList.add(
//                FunctionItem.Builder()
//                    .title(parentItem.title)
//                    .icon(parentItem.icon)
//                    .type(parentItem.type)
//                    .build()
//            )
            return if (index >= 0 && index <= childList.size) {
                childList.add(index, childItem)
                true
            } else {
                Log.e("DEditor", "removeFuncItemFromParent::index = $index is out of index, childList size is ${childList.size}")
                false
            }
        }
    }

    fun removeFuncItemFromParent(parentItem: FunctionItem, index: Int): Boolean {
        val childList = parentItem.getChildList()
        if (childList.isEmpty()) {
            Log.e("DEditor", "removeFuncItemFromParent:: childList is empty")
            return false
        }
        return if (index >= 0 && index < childList.size) {
            childList.removeAt(index)
            true
        } else {
            Log.e("DEditor", "removeFuncItemFromParent::index = $index is out of index, childList size is ${childList.size}")
            false
        }
    }

    fun removeFuncItemFromParent(parentItem: FunctionItem, childType: String): Boolean {
        val childList = parentItem.getChildList()
        if (childList.isEmpty()) {
            Log.e("DEditor", "removeFuncItemFromParent:: childList is empty")
            return false
        }
        val findChildItem = childList.firstOrNull {
            it.type === childType
        }
        if (findChildItem == null) {
            Log.e("DEditor", "removeFuncItemFromParent:: findChildItem is null")
            return false
        }
        return childList.remove(findChildItem)
    }

    fun findFunctionItemByType(type: String): FunctionItem? {
        var target: FunctionItem? = null
        val queue: Queue<FunctionItem> = LinkedList()
        queue.offer(rootItem)
        while (queue.isNotEmpty()) {
            val item = queue.poll()
            if (TextUtils.equals(type, item.type)) {
                target = item
                break
            }
            for (child in item.getChildList()) {
                queue.offer(child)
            }
        }
        return target
    }

    fun findParentByType(type: String): FunctionItem?{
        val funcItem = findFunctionItemByType(type) ?: return null
        return findParent(funcItem)
    }

    fun findParent(item: FunctionItem): FunctionItem? {
        return findParentByChild(rootItem, item)
    }

    private fun findParentByChild(parentItem: FunctionItem, targetItem: FunctionItem): FunctionItem? {
        if (parentItem.getChildList().contains(targetItem)) {
            return parentItem
        } else {
            parentItem.getChildList().forEach {
                val find = findParentByChild(it, targetItem)
                if (find != null) {
                    return find
                }
            }
            return null
        }
    }

    fun isRootItem(item: FunctionItem): Boolean{
        return findParent(item)?.type == ROOT_ITEM_TYPE
    }

    fun containsItem(funcItem: FunctionItem): Boolean {
        if (rootFunctionItemList.isEmpty()) {
            return false
        }
        for (item in rootFunctionItemList){
            if (TextUtils.equals(item.type, funcItem.type)) {
                return true
            }
        }
        return false
    }

    fun replace(oriItem: FunctionItem, newItem: FunctionItem): Boolean {
        val findParent = findParent(oriItem)
        return if (findParent == null) {
            false
        }else{
            var result = findParent.getChildList().remove(oriItem)
            if (result) {
                result = findParent.getChildList().add(newItem)
            }
            result
        }
    }


}