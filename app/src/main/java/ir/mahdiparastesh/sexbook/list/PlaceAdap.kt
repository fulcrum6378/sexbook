package ir.mahdiparastesh.sexbook.list

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings.Companion.spDefPlace
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemPlaceBinding
import ir.mahdiparastesh.sexbook.databinding.MigratePlaceBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.MaterialMenu

class PlaceAdap(val c: Places) : RecyclerView.Adapter<AnyViewHolder<ItemPlaceBinding>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AnyViewHolder<ItemPlaceBinding> =
        AnyViewHolder(ItemPlaceBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<ItemPlaceBinding>, i: Int) {
        if (c.m.places.value == null) return

        // Name
        h.b.name.setText(c.m.places.value!![i].name)
        h.b.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.places.value!![h.layoutPosition].apply {
                    if (name != h.b.name.text.toString()) {
                        name = h.b.name.text.toString()
                        update(h.layoutPosition, 0)
                    }
                }
            }
        })

        // Sum
        (c.m.places.value!![i].sum >= 0L).apply {
            h.b.sum.vis(this)
            h.b.sum.text = if (this) "{${c.m.places.value!![i].sum}}" else ""
        }

        // Click
        val longClick = View.OnLongClickListener { v ->
            if (c.m.places.value == null || c.m.places.value!!.size <= h.layoutPosition)
                return@OnLongClickListener true
            MaterialMenu(c, v, R.menu.place, Act().apply {
                this[R.id.plDefPlace] = {
                    c.sp.edit().apply {
                        putLong(spDefPlace, c.m.places.value!![h.layoutPosition].id)
                        apply()
                    }
                }
                this[R.id.plDelete] = {
                    MaterialAlertDialogBuilder(c).apply {
                        val bm = MigratePlaceBinding.inflate(c.layoutInflater)
                        bm.places.adapter = ArrayAdapter(c, R.layout.spinner,
                            ArrayList(c.m.places.value!!.map { it.name }).apply {
                                add(0, "")
                                remove(c.m.places.value!![h.layoutPosition].name)
                            }).apply { setDropDownViewResource(R.layout.spinner_dd) }
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.plDeletePlaceSure))
                        setView(bm.root)
                        setPositiveButton(R.string.yes) { _, _ ->
                            Work(
                                c, Work.P_DELETE_ONE, listOf(
                                    c.m.places.value!![h.layoutPosition],
                                    c.m.places.value!!.find { it.name == (bm.places.selectedItem as String) }
                                        ?.id ?: -1L,
                                    h.layoutPosition
                                )
                            ).start()
                            c.shake()
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                    }.show()
                    c.shake()
                }
            }).apply {
                if (c.sp.contains(spDefPlace) && c.sp.getLong(spDefPlace, -1L)
                    == c.m.places.value!![h.layoutPosition].id
                ) menu.findItem(R.id.plDefPlace).isChecked = true
            }.show()
            true
        }
        h.b.root.setOnLongClickListener(longClick)
        h.b.name.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = c.m.places.value?.size ?: 0

    fun update(i: Int, refresh: Int = 1) {
        if (c.m.places.value == null) return
        if (c.m.places.value!!.size <= i || i < 0) return
        Work(c, Work.P_UPDATE_ONE, listOf(c.m.places.value!![i], i, refresh)).start()
    }
}
