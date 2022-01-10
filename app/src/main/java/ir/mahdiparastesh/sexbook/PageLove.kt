package ir.mahdiparastesh.sexbook

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import ir.mahdiparastesh.sexbook.Fun.Companion.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageLoveBinding
import ir.mahdiparastesh.sexbook.list.CrushAdap
import ir.mahdiparastesh.sexbook.more.MessageInbox

class PageLove(val c: Main) : Fragment() {
    private lateinit var b: PageLoveBinding

    companion object {
        var handler = MutableLiveData<Handler?>(null)
        val messages = MessageInbox(PageSex.handler)
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = PageLoveBinding.inflate(layoutInflater, parent, false)


        // Handler
        handler.value = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ALL -> arrangeList(msg.obj as ArrayList<Crush>)
                    Work.REPLACE_ALL -> Work(Work.C_VIEW_ALL).start()
                    Work.C_DELETE_ONE -> {
                        c.m.liefde.value?.removeAt(msg.arg1)
                        b.rv.adapter?.notifyItemRemoved(msg.arg1)
                        b.rv.adapter?.notifyItemRangeChanged(msg.arg1, b.rv.adapter!!.itemCount)
                    }
                }
            }
        }
        messages.clear()

        return b.root
    }

    override fun onResume() {
        super.onResume()
        Work(Work.C_VIEW_ALL).start()
    }

    fun arrangeList(list: ArrayList<Crush>?) {
        c.m.liefde.value = list
        if (vis(b.empty, list.isNullOrEmpty())) return
        c.m.liefde.value!!.sortWith(Crush.Sort())
        b.rv.adapter = CrushAdap(c)
    }
}
