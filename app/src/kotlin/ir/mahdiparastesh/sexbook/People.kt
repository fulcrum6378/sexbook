package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Lister

class People : BaseActivity(), Lister {
    lateinit var b: PeopleBinding

    override var countBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)
        arrangeList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun arrangeList() {
        if (b.empty.vis(m.people.isNullOrEmpty())) return
        m.people?.sortWith(Crush.Sort(this))
        if (!sp.getBoolean(Settings.spPageLoveSortAsc, true)) m.people?.reverse()
        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
        count(m.people?.size ?: 0)
    }
}
