package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.explode
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.EstimationBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.Lister

class Estimation : BaseActivity(), Lister {
    private lateinit var b: EstimationBinding
    private var changed = false
    private var adding = false

    override var countBadge: BadgeDrawable? = null

    companion object {
        var handler: Handler? = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = EstimationBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.estimation)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                if (msg.what in arrayOf(
                        Work.G_VIEW_ONE, Work.G_INSERT_ONE, Work.G_UPDATE_ONE, Work.G_DELETE_ONE
                    )
                ) changed = true

                when (msg.what) {
                    Work.G_VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (m.guesses.value == null) m.guesses.value = ArrayList()
                            m.guesses.value!!.add(msg.obj as Guess)
                            b.list.adapter!!.notifyItemInserted(m.guesses.value!!.size - 1)
                            adding = false
                            b.add.explode(this@Estimation)
                            count(m.guesses.value?.size ?: 0)
                        }
                    }
                    Work.G_VIEW_ALL -> m.guesses.value = (msg.obj as ArrayList<Guess>)
                        .apply { sortWith(Guess.Sort()) }
                    Work.G_INSERT_ONE -> if (msg.obj != null)
                        Work(c, Work.G_VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)).start()
                    Work.G_UPDATE_ONE ->
                        if (msg.arg2 == 1) b.list.adapter?.notifyItemChanged(msg.arg1)
                    Work.G_DELETE_ONE -> {
                        m.guesses.value?.removeAt(msg.arg1)
                        b.list.adapter?.notifyItemRemoved(msg.arg1)
                        b.list.adapter?.notifyItemRangeChanged(
                            msg.arg1, b.list.adapter!!.itemCount - msg.arg1
                        )
                        count(m.guesses.value?.size ?: 0)
                    }
                }
            }
        }

        // List
        m.guesses.observe(this) {
            if (it == null) {
                b.list.adapter = null
                return@observe
            }
            if (b.list.adapter == null) b.list.adapter = GuessAdap(this)
            else b.list.adapter?.notifyDataSetChanged()
            count(m.guesses.value?.size ?: 0)
        }
        b.add.setOnClickListener {
            if (adding) return@setOnClickListener
            adding = true
            Work(c, Work.G_INSERT_ONE, listOf(Guess())).start()
            Delay { adding = false }
            c.shake()
        }

        // Miscellaneous
        if (night()) b.addIV.colorFilter = themePdcf()

        Work(c, Work.G_VIEW_ALL).start()
    }

    override fun onDestroy() {
        Places.handler = null
        super.onDestroy()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (changed) goTo(Main::class, true) { action = Main.Action.RELOAD.s }
        else super.onBackPressed()
    }
}
