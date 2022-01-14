package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
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
    private var adding = false

    companion object {
        lateinit var handler: Handler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PlacesBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.plTitle)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.P_VIEW_ALL ->
                        m.places.value = (msg.obj as ArrayList<Place>)
                            .apply { sortWith(Place.Sort()) }
                    Work.P_INSERT_ONE -> if (msg.obj != null)
                        Work(c, Work.P_VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)).start()
                    Work.P_UPDATE_ONE -> {
                    }
                }
            }
        }

        // List
        m.places.observe(this) {
            if (it == null) return@observe
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
}
