package ir.mahdiparastesh.sexbook.adap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush

class CrushAdap(val list: List<Crush>, val that: AppCompatActivity, val m: Model) :
    RecyclerView.Adapter<CrushAdap.MyViewHolder>() {
    class MyViewHolder(val l: ConstraintLayout) : RecyclerView.ViewHolder(l)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var l = LayoutInflater.from(parent.context)
            .inflate(R.layout.crush, parent, false) as ConstraintLayout
        return MyViewHolder(l)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {

    }

    override fun getItemCount() = list.size
}
