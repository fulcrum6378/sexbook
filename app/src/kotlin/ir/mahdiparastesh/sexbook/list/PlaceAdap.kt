package ir.mahdiparastesh.sexbook.list

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings.Companion.spDefPlace
import ir.mahdiparastesh.sexbook.databinding.ItemPlaceBinding
import ir.mahdiparastesh.sexbook.databinding.MigratePlaceBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaceAdap(val c: Places) : RecyclerView.Adapter<AnyViewHolder<ItemPlaceBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPlaceBinding> =
        AnyViewHolder(ItemPlaceBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<ItemPlaceBinding>, i: Int) {

        // Name
        h.b.name.setTextWatcher(null)
        h.b.name.setText(c.m.places[i].name)
        h.b.name.setTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.places[h.layoutPosition].apply {
                    if (name != h.b.name.text.toString()) {
                        name = h.b.name.text.toString()
                        update(h.layoutPosition)
                    }
                }
            }
        })

        // Sum
        (c.m.places[i].sum >= 0L).apply {
            h.b.sum.isVisible = this
            h.b.sum.text = if (this) "{${c.m.places[i].sum}}" else ""
        }

        // Click
        val longClick = View.OnLongClickListener { v ->
            if (c.m.places.size <= h.layoutPosition)
                return@OnLongClickListener true
            MaterialMenu(c, v, R.menu.place, Act().apply {
                this[R.id.plDefPlace] = {
                    c.sp.edit().apply {
                        putLong(spDefPlace, c.m.places[h.layoutPosition].id)
                        apply()
                    }
                }
                this[R.id.plDelete] = {
                    if (c.m.places[h.layoutPosition].sum > 0
                    ) MaterialAlertDialogBuilder(c).apply {
                        val bm = MigratePlaceBinding.inflate(c.layoutInflater)
                        bm.places.adapter = ArrayAdapter(c, R.layout.spinner_white,
                            ArrayList(c.m.places.map { it.name }).apply {
                                add(0, "")
                                remove(c.m.places[h.layoutPosition].name)
                            }).apply { setDropDownViewResource(R.layout.spinner_dd) }
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.plDeletePlaceSure))
                        setView(bm.root)
                        setPositiveButton(R.string.yes) { _, _ ->
                            delete(
                                h.layoutPosition, c.m.places
                                    .find { it.name == (bm.places.selectedItem as String) }?.id
                                    ?: -1L
                            )
                            c.shake()
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                        c.shake()
                    }.show()
                    else delete(h.layoutPosition, -1L)
                }
            }).apply {
                if (c.sp.contains(spDefPlace) && c.sp.getLong(spDefPlace, -1L)
                    == c.m.places[h.layoutPosition].id
                ) menu.findItem(R.id.plDefPlace).isChecked = true
            }.show()
            true
        }
        h.b.root.setOnLongClickListener(longClick)
        h.b.name.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = c.m.places.size

    fun update(i: Int) {
        if (c.m.places.size <= i || i < 0) return
        CoroutineScope(Dispatchers.IO).launch {
            c.m.dao.pUpdate(c.m.places[i])
            Main.changed = true
        }
    }

    fun delete(i: Int, migrateToId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            c.m.dao.pDelete(c.m.places[i])
            for (mig in c.m.dao.rGetByPlace(c.m.places[i].id))
                c.m.dao.rUpdate(mig.apply { plac = migrateToId })
            c.m.places.removeAt(i)
            Main.changed = true
            withContext(Dispatchers.Main) {
                notifyItemRemoved(i)
                notifyItemRangeChanged(i, itemCount - i)
                c.count(c.m.places.size)
            }
        }
    }
}
