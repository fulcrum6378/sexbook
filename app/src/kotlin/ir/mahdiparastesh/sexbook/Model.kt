package ir.mahdiparastesh.sexbook

import androidx.annotation.MainThread
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Dao
import ir.mahdiparastesh.sexbook.data.Database
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.CalendarManager
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.stat.Summary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.experimental.and

/** Static ViewModel available for all BaseActivity instances. */
class Model : ViewModel() {
    lateinit var db: Database
    lateinit var dao: Dao

    /** Holds all sex records with static unsorted indices. */
    var onani: ArrayList<Report>? = null
    val visOnani = arrayListOf<Report>()

    /** Holds all crushes (liefde is a subset of people). */
    var liefde: CopyOnWriteArrayList<Crush>? = null
    var people: ArrayList<Crush>? = null

    /** Holds all places. */
    var places: ArrayList<Place>? = null

    /** Holds all estimations. */
    var guesses: ArrayList<Guess>? = null

    /** Main data structure for most statistical analyses. */
    var summary: Summary? = null

    /** Interface for controlling this app's calendar events in the system calendar. */
    var calManager: CalendarManager? = null

    var loaded = false
    var currentPage = 0
    var listFilter = -1
    var navOpen = false

    fun findGlobalIndexOfReport(id: Long) =
        onani!!.indexOfFirst { it.id == id }

    fun getCrushes() = people?.let { ppl ->
        CopyOnWriteArrayList(ppl.filter {
            (it.status and Crush.STAT_INACTIVE) == 0.toByte()
        })
    }

    /** @param changeType 0=>insert, 1=>update, 2=>delete */
    @MainThread
    fun onCrushChanged(c: BaseActivity, crush: Crush, changeType: Int) {
        val pageLove = if (c is Main) c.pageLove() else null
        val aPos = people?.indexOfFirst { it.key == crush.key }
        val pos = liefde?.indexOfFirst { it.key == crush.key }
        if (changeType != 2) { // insert, update
            if (crush.active()) {
                if (pos != null && pos != -1) liefde?.set(pos, crush)
                else liefde?.add(crush)
                pageLove?.apply {
                    // b.rv.adapter?.notifyItemChanged(it)
                    prepareList() // so they can be sorted
                }
            } else if (pos != null && pos != -1) { // deactivated
                liefde?.removeAt(pos)
                pageLove?.apply {
                    b.rv.adapter?.notifyItemRemoved(pos)
                    b.rv.adapter?.notifyItemRangeChanged(
                        pos, pageLove.b.rv.adapter!!.itemCount - pos
                    )
                    b.empty.isVisible = liefde.isNullOrEmpty()
                }
            }
            if (aPos != null && aPos != -1) people?.set(aPos, crush)
            else people?.add(crush)
            if (c is People && people != null) // c.b.list.adapter?.notifyItemChanged(it)
                c.arrangeList() // so they can be sorted
        } else { // delete
            if (crush.active() && pos != -1) pos?.also {
                liefde?.removeAt(it)
                pageLove?.apply {
                    b.rv.adapter?.notifyItemRemoved(it)
                    b.rv.adapter?.notifyItemRangeChanged(
                        it, pageLove.b.rv.adapter!!.itemCount - it
                    )
                    b.empty.isVisible = liefde.isNullOrEmpty()
                }
            }
            if (aPos != -1) aPos?.also {
                people?.removeAt(it)
                if (c is People) {
                    c.b.list.adapter?.notifyItemRemoved(it)
                    c.b.list.adapter?.notifyItemRangeChanged(
                        it, c.b.list.adapter!!.itemCount - it
                    )
                }
            }
        }
        if (c is Main)
            c.count(liefde?.size ?: 0)
        else {
            PageLove.changed = true
            if (c is Singular) c.crush = crush
        }
        calManager?.apply { CoroutineScope(Dispatchers.IO).launch { replaceEvents(liefde) } }
    }

    fun summaryCrushes() = summary?.let { ArrayList(it.scores.keys) } ?: arrayListOf<String>()

    fun resetData() {
        onani = null
        visOnani.clear()
        people = null
        liefde = null
        places = null
        guesses = null
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
