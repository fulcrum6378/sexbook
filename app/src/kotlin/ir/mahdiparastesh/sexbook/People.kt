package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.ctrl.Screening
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Crush.Companion.STAT_FICTION
import ir.mahdiparastesh.sexbook.data.Crush.Companion.STAT_UNSAFE_PERSON
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.misc.Delay
import ir.mahdiparastesh.sexbook.stat.CrushesStat
import ir.mahdiparastesh.sexbook.view.Lister
import kotlin.experimental.and

class People : BaseActivity(), Toolbar.OnMenuItemClickListener, Lister {
    lateinit var b: PeopleBinding
    val mm: MyModel by viewModels()

    override var countBadge: BadgeDrawable? = null

    class MyModel : ViewModel() {
        lateinit var visPeople: ArrayList<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)
    }

    override fun onResume() {
        super.onResume()
        arrangeList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.people)
        b.toolbar.setOnMenuItemClickListener(this)
        updateFilterIcon()
        return true
    }

    fun updateFilterIcon() {
        b.toolbar.menu.findItem(R.id.filter)?.setIcon(
            if (m.screening?.any() == true) R.drawable.filtered else R.drawable.filter
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(Fun.findSortMenuItemId(sp.getInt(Settings.spPeopleSortBy, 0)))
            ?.isChecked = true
        menu?.findItem(
            if (sp.getBoolean(Settings.spPeopleSortAsc, true))
                R.id.sortAsc else R.id.sortDsc
        )?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chart -> if (m.people.isNotEmpty()) CrushesStat().apply {
                arguments = Bundle().apply { putInt(CrushesStat.BUNDLE_WHICH_LIST, 0) }
                show(supportFragmentManager, CrushesStat.TAG)
            }
            R.id.filter -> if (m.people.isNotEmpty())
                Screening().show(supportFragmentManager, "screening")
            else -> {
                Fun.sort(item.itemId)?.also { value ->
                    item.isChecked = true
                    sp.edit {
                        if (value is Int) putInt(Settings.spPeopleSortBy, value)
                        else if (value is Boolean) putBoolean(Settings.spPeopleSortAsc, value)
                    }
                    arrangeList()
                }
            }
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun arrangeList() {
        val hideUnsafe = sp.getBoolean(Settings.spHideUnsafePeople, true) && m.unsafe.isNotEmpty()
        val filters = m.screening
        mm.visPeople = ArrayList(when {
            filters?.any() == true -> m.people.filter { p ->
                if (filters.gender != 0 &&
                    filters.gender != (p.value.status and Crush.STAT_GENDER).toInt()
                ) return@filter false

                if (filters.fiction != 0 &&
                    (filters.fiction - 1) != (p.value.status and STAT_FICTION).toInt() shr 3
                ) return@filter false

                if (filters.safety != 0 &&
                    (filters.safety - 1) != (p.value.status and STAT_UNSAFE_PERSON).toInt() shr 5
                ) return@filter false

                return@filter true
            }
            hideUnsafe -> m.people.filter { p -> !p.value.unsafe() }
            else -> m.people
        }.keys)
        mm.visPeople.sortWith(Crush.Sort(this, Settings.spPeopleSortBy, Settings.spPeopleSortAsc))

        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
        b.empty.isVisible = mm.visPeople.isEmpty()
        Delay(100L) { count(mm.visPeople.size) }
    }
}
