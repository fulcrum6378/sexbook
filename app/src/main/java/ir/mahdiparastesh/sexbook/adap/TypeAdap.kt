package ir.mahdiparastesh.sexbook.adap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.pdcf
import ir.mahdiparastesh.sexbook.R

class TypeAdap : ArrayAdapter<TypeAdap.Type>(c, 0, types) {
    val inf: LayoutInflater = LayoutInflater.from(context)
    companion object {
        val names: Array<String> = c.resources.getStringArray(R.array.types)
        val types = arrayOf(
            Type(names[0], R.drawable.wet_dream),
            Type(names[1], R.drawable.masturbation),
            Type(names[2], R.drawable.oral_sex),
            Type(names[3], R.drawable.anal_sex),
            Type(names[4], R.drawable.vaginal_sex),
        )
    }

    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        (inf.inflate(R.layout.type_selector, parent, false) as ImageView).apply {
            setImageResource(types[i].icon)
            colorFilter = pdcf(c, R.color.mrvNotes)
        }

    override fun getDropDownView(i: Int, convertView: View?, parent: ViewGroup): View =
        (inf.inflate(R.layout.type_selector_dd, parent, false) as LinearLayout).apply {
            val icon = this[0] as ImageView
            val name = this[1] as TextView

            icon.setImageResource(types[i].icon)
            icon.colorFilter = pdcf(c, R.color.mrvNotes)
            name.text = types[i].name
        }

    data class Type(val name: String, val icon: Int)
}
