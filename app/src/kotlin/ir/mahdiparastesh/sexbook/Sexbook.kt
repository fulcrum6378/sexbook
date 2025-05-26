package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.icu.util.GregorianCalendar
import android.icu.util.IndianCalendar
import android.util.LongSparseArray
import androidx.annotation.MainThread
import androidx.core.view.isVisible
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.ctrl.CalendarManager
import ir.mahdiparastesh.sexbook.ctrl.Dao
import ir.mahdiparastesh.sexbook.ctrl.Database
import ir.mahdiparastesh.sexbook.ctrl.Screening
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.stat.Summary
import ir.mahdiparastesh.sexbook.util.HumanistIranianCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * The [Application] subclass of this app
 * It is in charge of maintaining necessary data,
 */
class Sexbook : Application() {

    /**
     * One database to rule them all!
     * We better never close this database.
     */
    val db: Database by lazy { Database.Builder(this).build() }
    val dao: Dao by lazy { db.dao() }
    var dbLoaded = false

    /** User preferences */
    val sp: SharedPreferences by lazy {
        getSharedPreferences(Settings.spName, MODE_PRIVATE)
    }

    /* --- Database Models --- */
    val reports: LongSparseArray<Report> = LongSparseArray<Report>()
    val people = hashMapOf<String, Crush>()
    val places = arrayListOf<Place>()
    val guesses = arrayListOf<Guess>()

    /** Main data structure for most statistical analyses. */
    var summary: Summary? = null

    /** A list of active Crushes. */
    var liefde = CopyOnWriteArrayList<String>()

    /** A list of [Crush]es marked as "unsafe". */
    var unsafe = CopyOnWriteArraySet<String>()

    /** A list of filters which will be applied to [People]. */
    var screening: Screening.Filters? = null


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

    /**
     * @return the chosen calendar type, if no choice has benn made, chooses one of them according
     * to the default [Locale] of this device
     */
    fun calType() = arrayOf(
        GregorianCalendar::class.java,
        HumanistIranianCalendar::class.java,
        IndianCalendar::class.java
    )[sp.getInt(
        Settings.spCalType, when (Locale.getDefault().country) {
            "IR" -> 1
            "IN" -> 2
            else -> 0
        }
    )]

    /**
     * Performs some complex necessary action after a [Crush] data model is altered.
     *
     * @param changeType 0=>insert, 1=>update, 2=>delete
     */
    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    fun onCrushChanged(c: BaseActivity, crush: String, changeType: Int) {
        val pageLove = if (c is Main) c.pageLove() else null
        val cr = people[crush]
        val lfPos = liefde.indexOf(crush)
        if (changeType != 2) {  // insert, update
            if (cr!!.active()) {
                if (lfPos == -1) liefde.add(crush)
                pageLove?.prepareList()
            } else if (lfPos != -1) {  // deactivated
                liefde.remove(crush)
                pageLove?.apply {
                    b.rv.adapter?.notifyItemRemoved(lfPos)
                    if (liefde.isNotEmpty()) b.rv.adapter?.notifyItemRangeChanged(
                        lfPos, pageLove.b.rv.adapter!!.itemCount - lfPos
                    )
                    b.empty.isVisible = liefde.isEmpty()
                }
            }
            if (c is People && crush in c.vm.visPeople) c.arrangeList()
            if (c is Settings) {
                c.vm.sortBNtfCrushes(this)
                c.bNtfCrushAdap?.notifyDataSetChanged()
            }
            if (cr.unsafe())
                unsafe.add(crush)
            else
                unsafe.remove(crush)
        } else {  // delete
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
            if (c is People && crush in c.vm.visPeople) {
                val vpPos = c.vm.visPeople.indexOf(crush)
                c.vm.visPeople.remove(crush)
                c.b.list.adapter?.notifyItemRemoved(vpPos)
                if (people.isNotEmpty()) c.b.list.adapter?.notifyItemRangeChanged(
                    vpPos, c.b.list.adapter!!.itemCount - vpPos
                )
                c.b.empty.isVisible = people.isEmpty()
            }
            if (c is Settings) {
                val bnPos = c.vm.bNtfCrushes.indexOf(crush)
                c.vm.bNtfCrushes.remove(crush)
                c.bNtfCrushAdap?.notifyItemRemoved(bnPos)
                if (c.vm.bNtfCrushes.isNotEmpty()) c.bNtfCrushAdap?.notifyItemRangeChanged(
                    bnPos, c.bNtfCrushAdap!!.itemCount - bnPos
                )
            }
            unsafe.remove(crush)
        }
        if (c is Main)
            c.count(liefde.size)
        else {
            PageLove.changed = true
            if (c is People) c.count(c.vm.visPeople.size)
        }
        if (CalendarManager.id != null)
            CoroutineScope(Dispatchers.IO).launch { CalendarManager.update(this@Sexbook) }
    }

    /**
     * Prepares a list of people mentioned in [Summary] regardless of their presence in the
     * [Crush] table in the [Database].
     *
     * It must necessarily return a mutable collection;
     * otherwise CrushSuggester::update() will error!
     */
    fun summaryCrushes(): ArrayList<String> =
        summary?.let { summary ->
            var all = ArrayList(summary.scores.keys)
            if (hideUnsafe()) ArrayList(all.filter { it !in unsafe }) else all
        } ?: arrayListOf()

    /** Should unsafe people be not shown? */
    fun hideUnsafe() =
        sp.getBoolean(Settings.spHideUnsafePeople, true) && unsafe.isNotEmpty()
}
