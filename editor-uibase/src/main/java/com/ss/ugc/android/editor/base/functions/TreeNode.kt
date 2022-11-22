package com.ss.ugc.android.editor.base.functions

/**
 * @date: 2021/3/26
 */
open class TreeNode {

    var children = mutableListOf<TreeNode>()

    fun addChild(node: TreeNode): Boolean {
        if (!children.contains(node)) {
            return children.add(node)
        }
        return false
    }

    fun removeChild(node: TreeNode): Boolean {
        if (children.contains(node)) {
            return children.remove(node)
        }
        return false
    }

    fun addChildList(childList: List<TreeNode>) {
        children.addAll(childList)
    }

    fun removeChildList(childList: List<TreeNode>) {
        children.removeAll(childList)
    }

    fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }



}