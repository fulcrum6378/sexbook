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
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.Lister
import ir.mahdiparastesh.sexbook.stat.CrushesStat

class People : BaseActivity(), Toolbar.OnMenuItemClickListener, Lister {
    lateinit var b: PeopleBinding
    val mm: MyModel by viewModels()

    override var countBadge: BadgeDrawable? = null

    class MyModel : ViewModel() {
        lateinit var visPeople: ArrayList<Crush>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)
        arrangeList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.crush_list)
        b.toolbar.setOnMenuItemClickListener(this)
        return true
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
            R.id.chart -> CrushesStat().show(supportFragmentManager, CrushesStat.TAG)

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
        b.empty.isVisible = m.people.isNullOrEmpty()
        if (m.people.isNullOrEmpty()) return

        mm.visPeople = ArrayList(m.people!!.sortedWith(Crush.Sort(this, Settings.spPeopleSortBy)))
        if (!sp.getBoolean(Settings.spPeopleSortAsc, true)) mm.visPeople.reverse()
        // TODO filter | search

        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
        Delay(100L) { count(mm.visPeople.size) }
    }
}
