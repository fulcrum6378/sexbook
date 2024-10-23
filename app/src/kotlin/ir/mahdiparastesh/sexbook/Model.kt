package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Dao
import ir.mahdiparastesh.sexbook.data.Database
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.misc.CalendarManager
import ir.mahdiparastesh.sexbook.stat.Summary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/** Static ViewModel available for all BaseActivity instances. */
class Model : ViewModel() {
    lateinit var db: Database
    lateinit var dao: Dao
    var dbLoaded = false

    /* --- Database Models --- */
    val reports = hashMapOf<Long, Report>()
    val people = hashMapOf<String, Crush>()
    val places = arrayListOf<Place>()
    val guesses = arrayListOf<Guess>()

    /** Main data structure for most statistical analyses. */
    var summary: Summary? = null

    /** A list of active Crushes. */
    var liefde = CopyOnWriteArrayList<String>()

    /** A list of Crushes marked as "unsafe". */
    var unsafe = CopyOnWriteArraySet<String>()


    fun resetData() {
        reports.clear()
        people.clear()
        places.clear()
        guesses.clear()
        dbLoaded = false

        summary = null
        liefde.clear()
        unsafe.clear()
    }

    /** @param changeType 0=>insert, 1=>update, 2=>delete */
    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    fun onCrushChanged(c: BaseActivity, crush: String, changeType: Int) {
        val pageLove = if (c is Main) c.pageLove() else null
        val cr = people[crush]
        val lfPos = liefde.indexOf(crush)
        if (changeType != 2) { // insert, update
            if (cr!!.active()) {
                if (lfPos == -1) liefde.add(crush)
                pageLove?.prepareList()
            } else if (lfPos != -1) { // deactivated
                liefde.remove(crush)
                pageLove?.apply {
                    b.rv.adapter?.notifyItemRemoved(lfPos)
                    if (liefde.isNotEmpty()) b.rv.adapter?.notifyItemRangeChanged(
                        lfPos, pageLove.b.rv.adapter!!.itemCount - lfPos
                    )
                    b.empty.isVisible = liefde.isEmpty()
                }
            }
            if (c is People && crush in c.mm.visPeople) c.arrangeList()
            if (c is Settings) {
                c.mm.sortBNtfCrushes(c)
                c.bNtfCrushAdap?.notifyDataSetChanged()
            }
            if (cr.unsafe())
                unsafe.add(crush)
            else
                unsafe.remove(crush)
        } else { // delete
            if (lfPos != -1) {
                liefde.removeAt(lfPos)
                pageLove?.apply {
                    b.rv.adapter?.notifyItemRemoved(lfPos)
                    if (liefde.isNotEmpty()) b.rv.adapter?.notifyItemRangeChanged(
                        lfPos, pageLove.b.rv.adapter!!.itemCount - lfPos
                    )
                    b.empty.isVisible = liefde.isEmpty()
                }
            }
            if (c is People && crush in c.mm.visPeople) {
                val vpPos = c.mm.visPeople.indexOf(crush)
                c.mm.visPeople.remove(crush)
                c.b.list.adapter?.notifyItemRemoved(vpPos)
                if (people.isNotEmpty()) c.b.list.adapter?.notifyItemRangeChanged(
                    vpPos, c.b.list.adapter!!.itemCount - vpPos
                )
                c.b.empty.isVisible = people.isEmpty()
            }
            if (c is Settings) {
                val bnPos = c.mm.bNtfCrushes.indexOf(crush)
                c.mm.bNtfCrushes.remove(crush)
                c.bNtfCrushAdap?.notifyItemRemoved(bnPos)
                if (c.mm.bNtfCrushes.isNotEmpty()) c.bNtfCrushAdap?.notifyItemRangeChanged(
                    bnPos, c.bNtfCrushAdap!!.itemCount - bnPos
                )
            }
            unsafe.remove(crush)
        }
        if (c is Main)
            c.count(liefde.size)
        else {
            PageLove.changed = true
            if (c is People) c.count(c.mm.visPeople.size)
        }
        if (CalendarManager.id != null)
            CoroutineScope(Dispatchers.IO).launch { CalendarManager.update(c) }
    }

    fun summaryCrushes(c: BaseActivity): ArrayList<String> =
        summary?.let { summary ->
            var all = ArrayList(summary.scores.keys)
            if (c.sp.getBoolean(Settings.spHideUnsafePeople, true) && unsafe.isNotEmpty())
                ArrayList(all.filter { it !in unsafe })
            else all
        } ?: arrayListOf()
    // it must necessarily return a mutable collection, otherwise CrushSuggester::update() will error!


    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(Model::class.java))
                throw IllegalArgumentException("Unknown Model class")
            val key = "Model"
            return if (hashMapViewModel.containsKey(key)) getViewModel(key) as T
            else {
                addViewModel(key, Model())
                getViewModel(key) as T
            }
        }

        companion object {
            val hashMapViewModel = HashMap<String, ViewModel>()

            fun addViewModel(key: String, viewModel: ViewModel) =
                hashMapViewModel.put(key, viewModel)

            fun getViewModel(key: String): ViewModel? = hashMapViewModel[key]
        }
    }
}
