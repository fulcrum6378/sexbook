package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import ir.mahdiparastesh.sexbook.base.BasePage
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.PageLoveBinding
import ir.mahdiparastesh.sexbook.list.CrushAdap

class PageLove : BasePage() {
    lateinit var b: PageLoveBinding

    companion object {
        var changed = false
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageLoveBinding.inflate(layoutInflater, parent, false).also { b = it }.root

    override fun onResume() {
        super.onResume()
        if (c.c.dbLoaded) {
            c.summarize(true)
            prepareList()
            if (changed) changed = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun prepareList() {
        super.prepareList()
        c.c.liefde.sortWith(Crush.Sort(c.c, Settings.spPageLoveSortBy, Settings.spPageLoveSortAsc))
        c.count(c.c.liefde.size)
        b.empty.isVisible = c.c.liefde.isEmpty()
        if (b.rv.adapter == null) b.rv.adapter = CrushAdap(c)
        else b.rv.adapter?.notifyDataSetChanged()
    }
}
