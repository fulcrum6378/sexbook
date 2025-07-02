package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.Lister
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.databinding.PlacesBinding
import ir.mahdiparastesh.sexbook.list.PlaceAdap
import ir.mahdiparastesh.sexbook.util.Delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This Activity lists and controls the [Place] table in the database.
 */
class Places : BaseActivity(), Lister {
    private lateinit var b: PlacesBinding

    override var countBadge: BadgeDrawable? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!c.dbLoaded) {
            @Suppress("DEPRECATION")
            onBackPressed()
            return; }

        super.onCreate(savedInstanceState)
        b = PlacesBinding.inflate(layoutInflater)
        setContentView(b.root)
        configureToolbar(b.toolbar, R.string.places)

        // list
        if (b.list.adapter == null) b.list.adapter = PlaceAdap(this)
        else b.list.adapter?.notifyDataSetChanged()
        b.empty.isVisible = c.places.isEmpty()
        Delay(100L) { count(c.places.size) }

        // "Add" button
        if (night) b.addIV.colorFilter = themePdcf()
        b.add.setOnClickListener { add() }
    }

    fun add() {
        CoroutineScope(Dispatchers.IO).launch {
            val newPlace = Place()
            newPlace.id = c.dao.pInsert(newPlace)
            c.places.add(newPlace)
            Main.changed = true

            withContext(Dispatchers.Main) {
                b.list.adapter!!.notifyItemInserted(c.places.size - 1)
                explosionEffect(b.add)
                count(c.places.size)
                b.empty.isVisible = false
            }
        }
        shake()
    }
}
