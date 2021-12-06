package ir.mahdiparastesh.sexbook.adap

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.stat.Singular

class CrushAdap(val list: List<Crush>, val that: Main) :
    RecyclerView.Adapter<CrushAdap.MyViewHolder>() {
    class MyViewHolder(val l: ConstraintLayout) : RecyclerView.ViewHolder(l)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var l = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crush, parent, false) as ConstraintLayout
        val name = l[namePos] as TextView

        // Fonts
        if (that.dateFont != null) name.setTypeface(that.dateFont, Typeface.BOLD)

        return MyViewHolder(l)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val name = h.l[namePos] as TextView

        name.text = list[i].visName()

        h.l.setOnClickListener {
            if (!Main.summarize(that.m)) return@setOnClickListener
            that.m.crush.value = list[i].key
            that.startActivity(Intent(that, Singular::class.java))
        }
    }

    override fun getItemCount() = list.size


    companion object {
        const val namePos = 0
    }
}
