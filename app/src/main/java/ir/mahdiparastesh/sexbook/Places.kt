package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PlacesBinding
import ir.mahdiparastesh.sexbook.list.PlaceAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class Places : BaseActivity() {
    private lateinit var b: PlacesBinding
    private var changed = false
    private var adding = false

    companion object {
        var handler: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PlacesBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.plTitle)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                if (msg.what in arrayOf(Work.P_INSERT_ONE, Work.P_UPDATE_ONE, Work.P_DELETE_ONE))
                    changed = true

                when (msg.what) {
                    Work.P_VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (m.places.value == null) m.places.value = ArrayList()
                            m.places.value!!.add(msg.obj as Place)
                            b.list.adapter!!.notifyItemInserted(m.places.value!!.size - 1)
                            adding = false
                            Fun.explode(c, b.add)
                        }
                    }
                    Work.P_VIEW_ALL -> m.places.value = (msg.obj as ArrayList<Place>).apply {
                        if (m.onani.value != null) for (p in indices) {
                            var sum = 0L
                            for (r in m.onani.value!!)
                                if (r.plac == this[p].id)
                                    sum++
                            this[p].sum = sum
                        }
                        sortWith(Place.Sort(Place.Sort.NAME))
                        if (m.onani.value != null) sortWith(Place.Sort(Place.Sort.SUM))
                    }
                    Work.P_INSERT_ONE -> if (msg.obj != null)
                        Work(c, Work.P_VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)).start()
                    Work.P_UPDATE_ONE ->
                        if (msg.arg2 == 1) b.list.adapter?.notifyItemChanged(msg.arg1)
                    Work.P_DELETE_ONE -> {
                        m.places.value?.removeAt(msg.arg1)
                        b.list.adapter?.notifyItemRemoved(msg.arg1)
                        b.list.adapter?.notifyItemRangeChanged(
                            msg.arg1, b.list.adapter!!.itemCount - msg.arg1
                        )
                    }
                }
            }
        }

        // List
        m.places.observe(this) {
            if (it == null) {
                b.list.adapter = null
                return@observe
            }
            if (b.list.adapter == null) b.list.adapter = PlaceAdap(this)
            else b.list.adapter?.notifyDataSetChanged()
        }
        b.add.setOnClickListener {
            if (adding) return@setOnClickListener
            adding = true
            Work(c, Work.P_INSERT_ONE, listOf(Place("", -1.0, -1.0))).start()
            object : CountDownTimer(Work.TIMEOUT, Work.TIMEOUT) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    adding = false; }
            }.start()
            Fun.shake(c)
        }

        Work(c, Work.P_VIEW_ALL).start()
    }

    override fun onDestroy() {
        handler = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (changed) {
            finish()
            startActivity(Intent(this, Main::class.java).setAction(Main.Action.RELOAD.s))
        } else super.onBackPressed()
    }
}
