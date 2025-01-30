package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.explode
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.databinding.EstimationBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.misc.Delay
import ir.mahdiparastesh.sexbook.view.Lister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Estimation : BaseActivity(), Lister {
    private lateinit var b: EstimationBinding

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
        b.empty.isVisible = m.guesses.isEmpty()
        Delay(100L) { count(m.guesses.size) }

        // "Add" button
        if (night()) b.addIV.colorFilter = themePdcf()
        b.add.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val newGuess = Guess()
                newGuess.id = m.dao.gInsert(newGuess)
                m.guesses.add(newGuess)
                Main.changed = true

                withContext(Dispatchers.Main) {
                    b.list.adapter!!.notifyItemInserted(m.guesses.size - 1)
                    b.add.explode(this@Estimation)
                    count(m.places.size)
                    b.empty.isVisible = false
                }
            }
            c.shake()
        }
    }
}
