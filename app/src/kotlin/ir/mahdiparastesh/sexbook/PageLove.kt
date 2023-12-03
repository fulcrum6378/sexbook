package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageLoveBinding
import ir.mahdiparastesh.sexbook.list.CrushAdap
import ir.mahdiparastesh.sexbook.more.BasePage
import java.util.concurrent.CopyOnWriteArrayList

@SuppressLint("NotifyDataSetChanged")
class PageLove : BasePage() {
    private lateinit var b: PageLoveBinding
    private var wasListEverPrepared = false
    // private var adBanner: AdView? = null

    companion object {
        var handler = MutableLiveData<Handler?>(null)
        var changed = false
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageLoveBinding.inflate(layoutInflater, parent, false).apply { b = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // if (BaseActivity.adsInitStatus?.isReady() == true) loadAd()

        handler.value = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ALL -> {
                        c.m.liefde = CopyOnWriteArrayList(msg.obj as List<Crush>)
                        prepareList()
                        c.count(c.m.liefde?.size ?: 0)
                    }
                    Work.REPLACE_ALL -> Work(c, Work.C_VIEW_ALL).start()
                    Work.C_UPDATE_ONE -> if (b.rv.adapter != null)
                        c.m.liefde?.indexOfFirst { it.key == (msg.obj as Crush).key }?.also {
                            val newCrs = msg.obj as Crush
                            if (newCrs.active()) {
                                c.m.liefde?.set(it, newCrs)
                                // b.rv.adapter?.notifyItemChanged(it)
                                prepareList()
                            } else {
                                c.m.liefde?.removeAt(it)
                                b.rv.adapter?.notifyItemRemoved(it)
                                b.rv.adapter?.notifyItemRangeChanged(
                                    it, b.rv.adapter!!.itemCount - it
                                )
                                c.count(c.m.liefde?.size ?: 0)
                            }
                        }
                    Work.C_DELETE_ONE -> if (b.rv.adapter != null) {
                        c.m.liefde?.indexOfFirst { it.key == (msg.obj as Crush).key }?.also {
                            c.m.liefde?.removeAt(it)
                            b.rv.adapter?.notifyItemRemoved(it)
                            b.rv.adapter?.notifyItemRangeChanged(it, b.rv.adapter!!.itemCount - it)
                        }
                        c.count(c.m.liefde?.size ?: 0)
                    }
                    // Work.ADMOB_LOADED -> loadAd()
                }
            }
        }

        if (c.m.liefde == null) Work(c, Work.C_VIEW_ALL).start()
        else if (!loadingNeedsSummary()) prepareList()
    }

    override fun onResume() {
        super.onResume()
        c.summarize(true)
        if (!wasListEverPrepared || loadingNeedsSummary()) prepareList()
        if (changed) {
            Work(c, Work.C_VIEW_ALL).start()
            changed = false
        } else b.rv.adapter?.notifyDataSetChanged()
    }

    /**
     * Called whenever data is loaded, it sorts the data and then calls arrangeList().
     * The data doesn't need to be sorted again sometimes; that's why it was separated from arrangeList().
     */
    override fun prepareList() {
        wasListEverPrepared = true
        c.m.liefde?.sortWith(Crush.Sort(c))
        if (!c.sp.getBoolean(Settings.spPageLoveSortAsc, true)) c.m.liefde?.reverse()
        arrangeList()
    }

    /** Arranges the list in the adapter, and creates the adapter if it doesn't exist. */
    private fun arrangeList() {
        if (b.empty.vis(c.m.liefde.isNullOrEmpty())) return
        if (b.rv.adapter == null) b.rv.adapter = CrushAdap(c)
        else b.rv.adapter?.notifyDataSetChanged()
    }

    private fun loadingNeedsSummary() =
        c.sp.getInt(Settings.spPageLoveSortBy, 0) in arrayOf(Fun.SORT_BY_SUM, Fun.SORT_BY_LAST)

    /*private fun loadAd() {
        if (adBanner != null) return
        adBanner = Fun.adaptiveBanner(c, "ca-app-pub-9457309151954418/4204909055")
        b.root.addView(adBanner, Fun.adaptiveBannerLp())
        adBanner?.loadAd(AdRequest.Builder().build())
        b.rv.layoutParams = (b.rv.layoutParams as ConstraintLayout.LayoutParams)
            .apply { bottomToTop = R.id.adBanner }
    }*/
}
