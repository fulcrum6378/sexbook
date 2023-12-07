package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.core.content.edit
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.Lister

class People : BaseActivity(), Toolbar.OnMenuItemClickListener, Lister {
    lateinit var b: PeopleBinding

    override var countBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)
        arrangeList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.sort)
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
        sp.edit {
            val value = Fun.sort(item.itemId)
            if (value is Int) putInt(Settings.spPeopleSortBy, value)
            else if (value is Boolean) putBoolean(Settings.spPeopleSortAsc, value)
        }
        arrangeList()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun arrangeList() {
        if (b.empty.vis(m.people.isNullOrEmpty())) return
        m.people?.sortWith(Crush.Sort(this, Settings.spPeopleSortBy))
        if (!sp.getBoolean(Settings.spPeopleSortAsc, true)) m.people?.reverse()
        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
        Delay(100L) { count(m.people?.size ?: 0) }
    }
}
