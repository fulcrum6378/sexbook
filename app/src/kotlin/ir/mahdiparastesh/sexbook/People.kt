package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Lister

class People : BaseActivity(), Lister {
    private lateinit var b: PeopleBinding
    val mm: MyModel by viewModels()

    override var countBadge: BadgeDrawable? = null

    companion object {
        var handler: Handler? = null
    }

    class MyModel : ViewModel() {
        var people: ArrayList<Crush>? = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ALL_PEOPLE -> {
                        mm.people = ArrayList(msg.obj as List<Crush>)
                        mm.people?.sortWith(Crush.Sort(this@People))
                        if (!sp.getBoolean(Settings.spPageLoveSortAsc, true)) mm.people?.reverse()
                        arrangeList()
                        count(mm.people?.size ?: 0)
                    }
                    Work.C_UPDATE_ONE -> {
                        if (b.list.adapter != null)
                            mm.people?.indexOfFirst { it.key == (msg.obj as Crush).key }?.also {
                                mm.people?.set(it, msg.obj as Crush)
                                b.list.adapter?.notifyItemChanged(it)
                            }
                        PageLove.changed = true
                    }
                    Work.C_DELETE_ONE -> {
                        if (b.list.adapter != null) {
                            mm.people?.indexOfFirst { it.key == (msg.obj as Crush).key }?.also {
                                mm.people?.removeAt(it)
                                b.list.adapter?.notifyItemRemoved(it)
                                b.list.adapter?.notifyItemRangeChanged(
                                    it, b.list.adapter!!.itemCount - it
                                )
                            }
                            count(mm.people?.size ?: 0)
                        }
                        PageLove.changed = true
                    }
                }
            }
        }

        Work(c, Work.C_VIEW_ALL_PEOPLE, null, handler).start()
    }

    override fun onDestroy() {
        handler = null
        super.onDestroy()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun arrangeList() {
        if (b.empty.vis(mm.people.isNullOrEmpty())) return
        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
    }
}
