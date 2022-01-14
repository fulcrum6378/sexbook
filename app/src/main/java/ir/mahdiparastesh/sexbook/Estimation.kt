package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.EstimationBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class Estimation : BaseActivity() {
    private lateinit var b: EstimationBinding

    companion object {
        var handler: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = EstimationBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.etTitle)

        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.G_VIEW_ALL ->
                        m.guesses.value = (msg.obj as ArrayList<Guess>)
                            .apply { sortWith(Guess.Sort()) }
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
        }

        Work(c, Work.G_VIEW_ALL).start()
    }

    override fun onDestroy() {
        Places.handler = null
        super.onDestroy()
    }
}
