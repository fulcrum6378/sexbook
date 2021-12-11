package ir.mahdiparastesh.sexbook

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun.Companion.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageLoveBinding
import ir.mahdiparastesh.sexbook.list.CrushAdap

class PageLove(val that: Main) : Fragment() {
    private lateinit var b: PageLoveBinding
    private lateinit var m: Model
    var adapter: CrushAdap? = null

    companion object {
        lateinit var handler: Handler

        fun handling() = ::handler.isInitialized
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = PageLoveBinding.inflate(layoutInflater, parent, false)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)


        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ALL -> arrangeList(msg.obj as ArrayList<Crush>)
                    Work.REPLACE_ALL -> Work(Work.C_VIEW_ALL).start()
                }
            }
        }

        Work(Work.C_VIEW_ALL).start()
        return b.root
    }

    fun arrangeList(list: ArrayList<Crush>?) {
        m.liefde.value = list
        if (vis(b.empty, list.isNullOrEmpty())) return
        m.liefde.value!!.sortWith(Crush.Sort())
        adapter = CrushAdap(list!!.toList(), that)
        b.rv.adapter = adapter
    }
}
