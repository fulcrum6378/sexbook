package ir.mahdiparastesh.sexbook

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.stat.Recency
import ir.mahdiparastesh.sexbook.stat.Summary

class Model : ViewModel() {
    val onani = MutableLiveData<ArrayList<Report>?>(null)
    val liefde = MutableLiveData<ArrayList<Crush>?>(null)
    val places = MutableLiveData<ArrayList<Place>?>(null)
    val guesses = MutableLiveData<ArrayList<Guess>?>(null)

    var loaded = false
    var crush: String? = null
    val visOnani = MutableLiveData<ArrayList<Report>>(arrayListOf())
    val summary = MutableLiveData<Summary?>()
    val recency = MutableLiveData<Recency?>()
    var showingSummary = false
    var showingRecency = false

    fun reset() {
        onani.value = null
        visOnani.value?.clear()
        liefde.value = null
        places.value = null
        guesses.value = null
    }


    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
