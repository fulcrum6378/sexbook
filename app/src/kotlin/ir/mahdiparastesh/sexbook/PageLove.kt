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
    // private var adBanner: AdView? = null

    companion object {
        var changed = false
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageLoveBinding.inflate(layoutInflater, parent, false).also { b = it }.root

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BaseActivity.adsInitStatus?.isReady() == true) loadAd()
    }*/

    override fun onResume() {
        super.onResume()
        if (c.m.dbLoaded) {
            c.summarize(true)
            prepareList()
            if (changed) changed = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun prepareList() {
        super.prepareList()
        c.m.liefde.sortWith(Crush.Sort(c, Settings.spPageLoveSortBy, Settings.spPageLoveSortAsc))
        c.count(c.m.liefde.size)
        b.empty.isVisible = c.m.liefde.isEmpty()
        if (b.rv.adapter == null) b.rv.adapter = CrushAdap(c)
        else b.rv.adapter?.notifyDataSetChanged()
    }

    /*fun loadAd() {
        if (adBanner != null) return
        adBanner = Fun.adaptiveBanner(c, "ca-app-pub-9457309151954418/4204909055")
        b.root.addView(adBanner, Fun.adaptiveBannerLp())
        adBanner?.loadAd(AdRequest.Builder().build())
        b.rv.layoutParams = (b.rv.layoutParams as ConstraintLayout.LayoutParams)
            .apply { bottomToTop = R.id.adBanner }
    }*/
}
