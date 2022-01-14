package ir.mahdiparastesh.sexbook.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.databinding.ItemPlaceBinding

class PlaceAdap(val c: Places) : RecyclerView.Adapter<PlaceAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemPlaceBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Fonts
        b.name.typeface = c.font1Bold

        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (c.m.places.value == null) return
        h.b.name.text = c.m.places.value!![i].name
    }

    override fun getItemCount() = c.m.places.value!!.size
}
