package ir.mahdiparastesh.sexbook.adap

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.stat.Singular

class CrushAdap(val list: List<Crush>, val that: Main) :
    RecyclerView.Adapter<CrushAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemCrushBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemCrushBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Fonts
        b.name.typeface = Fun.font1Bold

        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        h.b.name.text = list[i].visName()

        h.b.root.setOnClickListener {
            if (!Main.summarize(that.m)) return@setOnClickListener
            that.m.crush.value = list[i].key
            that.startActivity(Intent(that, Singular::class.java))
        }
    }

    override fun getItemCount() = list.size
}
