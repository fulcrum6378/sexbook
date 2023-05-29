package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageLoveBinding
import ir.mahdiparastesh.sexbook.list.CrushAdap
import ir.mahdiparastesh.sexbook.more.MessageInbox

@SuppressLint("NotifyDataSetChanged")
class PageLove : Fragment() {
    val c: Main by lazy { activity as Main } // don't define it as a getter.
    private lateinit var b: PageLoveBinding
    private val messages = MessageInbox(PageSex.handler)
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
                        c.m.liefde.value = msg.obj as ArrayList<Crush>
                        prepareList()
                    }
                    Work.REPLACE_ALL -> Work(c, Work.C_VIEW_ALL).start()
                    Work.C_UPDATE_ONE -> /*if (b.rv.adapter != null)
                        c.m.liefde.value?.indexOfFirst { it.key == (msg.obj as Crush).key }?.also {
                            c.m.liefde.value?.set(it, msg.obj as Crush)
                            b.rv.adapter?.notifyItemChanged(it)
                        }*/ prepareList()
                    Work.C_DELETE_ONE -> if (b.rv.adapter != null)
                        c.m.liefde.value?.indexOfFirst { it.key == (msg.obj as Crush).key }?.also {
                            c.m.liefde.value?.removeAt(it)
                            b.rv.adapter?.notifyItemRemoved(it)
                            b.rv.adapter?.notifyItemRangeChanged(it, b.rv.adapter!!.itemCount - it)
                        }
                    // Work.ADMOB_LOADED -> loadAd()
                }
            }
        }
        messages.clear()

        if (c.m.liefde.value == null) Work(c, Work.C_VIEW_ALL).start()
        else prepareList()
    }

    override fun onResume() {
        super.onResume()
        c.summarize(true)
        if (changed) Work(c, Work.C_VIEW_ALL).start()
        else b.rv.adapter?.notifyDataSetChanged()
    }

    /**
     * Called whenever data is loaded, it sorts the data and then calls arrangeList().
     * The data doesn't need to be sorted again sometimes; that's why it was separated from arrangeList().
     */
    fun prepareList() {
        c.m.liefde.value?.sortWith(Crush.Sort(c.sp.getInt(Settings.spPageLoveSortBy, 0), c.m))
        if (!c.sp.getBoolean(Settings.spPageLoveSortAsc, true)) c.m.liefde.value?.reverse()
        arrangeList()
    }

    /** Arranges the list in the adapter, and creates the adapter if it doesn't exist. */
    private fun arrangeList() {
        if (b.empty.vis(c.m.liefde.value.isNullOrEmpty())) return
        if (b.rv.adapter == null) b.rv.adapter = CrushAdap(c)
        else b.rv.adapter?.notifyDataSetChanged()
    }

    /*private fun loadAd() {
        if (adBanner != null) return
        adBanner = Fun.adaptiveBanner(c, "ca-app-pub-9457309151954418/4204909055")
        b.root.addView(adBanner, Fun.adaptiveBannerLp())
        adBanner?.loadAd(AdRequest.Builder().build())
        b.rv.layoutParams = (b.rv.layoutParams as ConstraintLayout.LayoutParams)
            .apply { bottomToTop = R.id.adBanner }
    }*/
}
