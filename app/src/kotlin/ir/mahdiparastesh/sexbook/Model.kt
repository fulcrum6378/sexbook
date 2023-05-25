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

/** Static ViewModel available for all BaseActivity instances. */
class Model : ViewModel() {
    /** Holds all sex records with static unsorted indices. */
    val onani = MutableLiveData<ArrayList<Report>?>(null)

    /** Holds all crushes. */
    val liefde = MutableLiveData<ArrayList<Crush>?>(null)

    /** Holds all places. */
    val places = MutableLiveData<ArrayList<Place>?>(null)

    /** Holds all estimations. */
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
