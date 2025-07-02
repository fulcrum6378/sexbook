package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.Lister
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.databinding.EstimationBinding
import ir.mahdiparastesh.sexbook.list.GuessAdap
import ir.mahdiparastesh.sexbook.util.Delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This Activity lists and controls the [Guess] table in the database.
 */
class Estimation : BaseActivity(), Lister {
    private lateinit var b: EstimationBinding

    override var countBadge: BadgeDrawable? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!c.dbLoaded) {
            @Suppress("DEPRECATION")
            onBackPressed()
            return; }

        super.onCreate(savedInstanceState)
        b = EstimationBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.estimation)

        // list
        if (b.list.adapter == null) b.list.adapter = GuessAdap(this)
        else b.list.adapter?.notifyDataSetChanged()
        b.empty.isVisible = c.guesses.isEmpty()
        Delay(100L) { count(c.guesses.size) }

        // "Add" button
        if (night) b.addIV.colorFilter = themePdcf()
        b.add.setOnClickListener { add() }
    }

    private fun add() {
        CoroutineScope(Dispatchers.IO).launch {
            val newGuess = Guess()
            newGuess.id = c.dao.gInsert(newGuess)
            c.guesses.add(newGuess)
            Main.changed = true

            withContext(Dispatchers.Main) {
                b.list.adapter!!.notifyItemInserted(c.guesses.size - 1)
                explosionEffect(b.add)
                count(c.places.size)
                b.empty.isVisible = false
            }
        }
        shake()
    }
}
