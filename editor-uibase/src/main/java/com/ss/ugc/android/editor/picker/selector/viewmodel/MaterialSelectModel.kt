package com.ss.ugc.android.editor.picker.selector.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.selector.validator.IConfirmValidator
import com.ss.ugc.android.editor.picker.selector.validator.IPostSelectValidator
import com.ss.ugc.android.editor.picker.selector.validator.IPreSelectValidator

/**
 * 提供素材选择操作
 */
class MaterialSelectModel : ViewModel() {
    private val selectedMap = hashMapOf<String, MaterialSelectedState>()
    private val _selectedList = mutableListOf<MediaItem>()
    val selectedList: List<MediaItem> get() = _selectedList

    private val _changeData = MutableLiveData<MediaItem>()
    val changeData: LiveData<MediaItem> get() = _changeData

    private val _enableSelect = MutableLiveData<Boolean>()
    val enableSelect: LiveData<Boolean> get() = _enableSelect

    private val _enableConfirm = MutableLiveData<Boolean>()
    val enableConfirm: LiveData<Boolean> get() = _enableConfirm

    private val _selectCount = MutableLiveData<Int>()
    val selectCount: LiveData<Int> get() = _selectCount

    private val selectPreValidators = mutableListOf<IPreSelectValidator>()
    private val selectPostValidators = mutableListOf<IPostSelectValidator>()
    private val confirmValidators = mutableListOf<IConfirmValidator>()

    init {
        _selectCount.value = 0
        _enableSelect.value = true
        _enableConfirm.value = false
    }

    /**
     * 选择素材或者取消选择素材
     */
    fun changeSelectState(mediaItem: MediaItem) {
        val currentState = getSelectState(mediaItem)
        val finalState = currentState.toggle()
        if (finalState.isSelected()) {
            select(mediaItem)
        } else {
            cancel(mediaItem)
        }
    }

    private fun select(mediaItem: MediaItem) {
        val preValidate = selectPreValidators.all {
            it.preCheck(mediaItem, selectedList)
        }

        if (preValidate) {
            updateSelectState(mediaItem, MaterialSelectedState.SELECTED)
            _changeData.value = mediaItem
            _selectedList.add(mediaItem)
            _selectCount.value = _selectCount.value!! + 1

            val postValidate = selectPostValidators.all {
                it.postCheck(mediaItem, selectedList)
            }
            if (postValidate && _enableSelect.value == true) {
                _enableSelect.value = false
            }

            val confirmValidate = confirmValidators.all {
                it.check(mediaItem, selectedList)
            }
            if (confirmValidate && _enableConfirm.value == false) {
                _enableConfirm.value = true
            }
        }
    }

    private fun cancel(mediaItem: MediaItem) {
        selectedMap.remove(mediaItem.path)
        _selectedList.remove(mediaItem)
        _changeData.value = mediaItem
        _selectCount.value = _selectCount.value!! - 1
        if (_enableSelect.value == false) {
            _enableSelect.value = true
        }

        val confirmValidate = confirmValidators.all {
            it.check(mediaItem, selectedList)
        }
        if (!confirmValidate && _enableConfirm.value == true) {
            _enableConfirm.value = false
        }
    }

    private fun updateSelectState(mediaItem: MediaItem, targetState: MaterialSelectedState) {
        selectedMap[mediaItem.path] = targetState
    }

    /**
     * 获取当前item的选择状态
     */
    fun getSelectState(mediaItem: MediaItem): MaterialSelectedState {
        return selectedMap[mediaItem.path] ?: MaterialSelectedState.NON_SELECTED
    }

    fun addPreSelectValidator(validator: IPreSelectValidator) {
        selectPreValidators.add(validator)
    }

    fun removePreSelectValidator(validator: IPreSelectValidator) {
        selectPreValidators.remove(validator)
    }

    fun addPostSelectValidator(validator: IPostSelectValidator) {
        selectPostValidators.add(validator)
    }

    fun removePostSelectValidator(validator: IPostSelectValidator) {
        selectPostValidators.remove(validator)
    }

    fun addConfirmValidator(validator: IConfirmValidator) {
        confirmValidators.add(validator)
    }

    fun removeConfirmValidator(validator: IConfirmValidator) {
        confirmValidators.add(validator)
    }

    override fun onCleared() {
        super.onCleared()
        selectedMap.clear()
        confirmValidators.clear()
        selectPostValidators.clear()
        selectPreValidators.clear()
    }
}

class SelectedState(val materialSelectedState: MaterialSelectedState, val enableSelect: Boolean)

enum class MaterialSelectedState {
    SELECTED, NON_SELECTED;

    fun isSelected(): Boolean = this === SELECTED

    fun toggle(): MaterialSelectedState {
        return when (this) {
            SELECTED -> NON_SELECTED
            NON_SELECTED -> SELECTED
        }
    }
}
