package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun.Companion.vis
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings.Companion.spDefPlace
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemPlaceBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.sp
import ir.mahdiparastesh.sexbook.more.MaterialMenu

class PlaceAdap(val c: Places) : RecyclerView.Adapter<PlaceAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemPlaceBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemPlaceBinding.inflate(c.layoutInflater, parent, false)

        // Fonts
        b.name.typeface = c.font1Bold

        return MyViewHolder(b)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
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
            vis(h.b.sum, this)
            h.b.sum.text = if (this) "{${c.m.places.value!![i].sum}}" else ""
        }

        // Click
        val longClick = View.OnLongClickListener { v ->
            MaterialMenu(c, v, R.menu.place, Act().apply {
                this[R.id.plDefPlace] = {
                    sp.edit().apply {
                        putLong(spDefPlace, c.m.places.value!![h.layoutPosition].id)
                        apply()
                    }
                }
                this[R.id.plDelete] = {
                    AlertDialog.Builder(c).apply {
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.plDeletePlaceSure))
                        setPositiveButton(R.string.yes) { _, _ ->
                            // TODO: REPORTS WHICH WERE RELATED TO THIS PLACE?!?
                            Work(
                                c, Work.P_DELETE_ONE,
                                listOf(c.m.places.value!![h.layoutPosition], h.layoutPosition)
                            ).start()
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        c.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                        c.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
                    }
                }
            }).apply {
                if (sp.contains(spDefPlace) && sp.getLong(spDefPlace, -1L)
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
