package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.Fun.explode
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.databinding.PlacesBinding
import ir.mahdiparastesh.sexbook.list.PlaceAdap
import ir.mahdiparastesh.sexbook.misc.Delay
import ir.mahdiparastesh.sexbook.misc.Lister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Places : BaseActivity(), Lister {
    private lateinit var b: PlacesBinding

    override var countBadge: BadgeDrawable? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PlacesBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.places)

        // List
        if (b.list.adapter == null) b.list.adapter = PlaceAdap(this)
        else b.list.adapter?.notifyDataSetChanged()
        b.empty.isVisible = m.places.isEmpty()
        Delay(100L) { count(m.places.size) }

        // "Add" button
        if (night()) b.addIV.colorFilter = themePdcf()
        b.add.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val newPlace = Place()
                newPlace.id = m.dao.pInsert(newPlace)
                m.places.add(newPlace)
                Main.changed = true

                withContext(Dispatchers.Main) {
                    b.list.adapter!!.notifyItemInserted(m.places.size - 1)
                    b.add.explode(this@Places)
                    count(m.places.size)
                    b.empty.isVisible = false
                }
            }
            c.shake()
        }
    }
}
