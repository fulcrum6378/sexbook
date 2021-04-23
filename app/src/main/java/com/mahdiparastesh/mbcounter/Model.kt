package com.mahdiparastesh.mbcounter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mahdiparastesh.mbcounter.data.Report

class Model : ViewModel() {
    val onani: MutableLiveData<ArrayList<Report>?> by lazy { MutableLiveData<ArrayList<Report>?>() }
    val crush: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }
    val summary: MutableLiveData<Summary.Result?> by lazy { MutableLiveData<Summary.Result?>() }


    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(Model::class.java)) {
                val key = "Model"
                return if (hashMapViewModel.containsKey(key)) getViewModel(key) as T
                else {
                    addViewModel(key, Model())
                    getViewModel(key) as T
                }
            }
            throw IllegalArgumentException("Unknown Model class")
        }

        companion object {
            val hashMapViewModel = HashMap<String, ViewModel>()

            fun addViewModel(key: String, viewModel: ViewModel) =
                hashMapViewModel.put(key, viewModel)

            fun getViewModel(key: String): ViewModel? = hashMapViewModel[key]
        }
    }
}
