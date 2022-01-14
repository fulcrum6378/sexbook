package ir.mahdiparastesh.sexbook.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Estimation
import ir.mahdiparastesh.sexbook.databinding.ItemGuessBinding

class GuessAdap(val c: Estimation) : RecyclerView.Adapter<GuessAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemGuessBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemGuessBinding.inflate(c.layoutInflater, parent, false)
        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (c.m.guesses.value == null) return
    }

    override fun getItemCount() = c.m.guesses.value!!.size
}
