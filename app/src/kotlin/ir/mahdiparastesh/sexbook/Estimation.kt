package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.explode
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.databinding.EstimationBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Lister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Estimation : BaseActivity(), Lister {
    private lateinit var b: EstimationBinding
    var changed = false
    private var adding = false

    override var countBadge: BadgeDrawable? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = EstimationBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.estimation)

        // List
        if (b.list.adapter == null) b.list.adapter = GuessAdap(this)
        else b.list.adapter?.notifyDataSetChanged()
        count(m.guesses?.size ?: 0)

        // "Add" button
        if (night()) b.addIV.colorFilter = themePdcf()
        b.add.setOnClickListener {
            if (adding) return@setOnClickListener
            adding = true
            CoroutineScope(Dispatchers.IO).launch {
                val newGuess = Guess()
                newGuess.id = m.dao.gInsert(newGuess)
                if (m.guesses == null) m.guesses = ArrayList()
                m.guesses!!.add(newGuess)
                changed = true
                adding = false
                withContext(Dispatchers.Main) {
                    b.list.adapter!!.notifyItemInserted(m.guesses!!.size - 1)
                    b.add.explode(this@Estimation)
                    count(m.places?.size ?: 0)
                }
            }
            c.shake()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (changed) goTo(Main::class, true) { action = Main.Action.RELOAD.s }
        else @Suppress("DEPRECATION") super.onBackPressed()
    }
}
