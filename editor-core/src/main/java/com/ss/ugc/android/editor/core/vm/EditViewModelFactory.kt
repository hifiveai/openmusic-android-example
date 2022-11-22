package com.ss.ugc.android.editor.core.vm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.InvocationTargetException

class EditViewModelFactory(val activity: FragmentActivity) : ViewModelProvider.AndroidViewModelFactory(activity.application) {
    companion object {
        private var sInstance: EditViewModelFactory? = null
        private fun getInstance(activity: FragmentActivity): EditViewModelFactory {
            if (sInstance == null) {
                sInstance = EditViewModelFactory(activity)
            }
            return sInstance ?: EditViewModelFactory(activity)
        }

        fun viewModelProvider(fragment: Fragment): ViewModelProvider {
            val activity = fragment.activity ?: throw NullPointerException("non null activity is allowed")
            return ViewModelProvider(activity, EditViewModelFactory(activity))
        }
        @JvmStatic
        fun viewModelProvider(activity: FragmentActivity):ViewModelProvider{
            return ViewModelProvider(activity, EditViewModelFactory(activity))
        }
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (BaseViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                modelClass.getConstructor(FragmentActivity::class.java).newInstance(activity)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        } else super.create(modelClass)
    }


}