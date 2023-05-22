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
    val onani = MutableLiveData<ArrayList<Report>?>(null) // static unsorted indices
    val liefde = MutableLiveData<ArrayList<Crush>?>(null)
    val places = MutableLiveData<ArrayList<Place>?>(null)
    val guesses = MutableLiveData<ArrayList<Guess>?>(null)

    var loaded = false
    var crush: String? = null
    val visOnani = arrayListOf<Report>()
    var listFilter = -1
    var summary: Summary? = null
    var recency: Recency? = null
    var navOpen = false
    var showingSummary = false
    var showingRecency = false
    var lookingFor: String? = null

    fun lookForIt(text: String) =
        lookingFor?.let { it != "" && text.contains(it, true) } ?: false

    fun findGlobalIndexOfReport(id: Long) =
        onani.value!!.indexOfFirst { it.id == id }

    fun resetData() {
        onani.value = null
        liefde.value = null
        places.value = null
        guesses.value = null
        visOnani.clear()
        listFilter = -1
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
