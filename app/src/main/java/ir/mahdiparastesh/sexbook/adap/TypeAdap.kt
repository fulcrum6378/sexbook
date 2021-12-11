package ir.mahdiparastesh.sexbook.adap

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.pdcf
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.TypeSelectorBinding
import ir.mahdiparastesh.sexbook.databinding.TypeSelectorDdBinding

class TypeAdap : ArrayAdapter<TypeAdap.Type>(c, 0, types) {

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

    @SuppressLint("ViewHolder")
    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        TypeSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            root.setImageResource(types[i].icon)
            root.colorFilter = pdcf(R.color.mrvNotes)
        }.root

    override fun getDropDownView(i: Int, convertView: View?, parent: ViewGroup): View =
        TypeSelectorDdBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            icon.setImageResource(types[i].icon)
            icon.colorFilter = pdcf(R.color.mrvNotes)
            name.text = types[i].name
            name.typeface = Fun.font1
        }.root

    data class Type(val name: String, val icon: Int)
}
