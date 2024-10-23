package ir.mahdiparastesh.sexbook.misc

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.SexType
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.databinding.TypeSelectorBinding
import ir.mahdiparastesh.sexbook.databinding.TypeSelectorDdBinding

/**
 * An ArrayAdapter that wraps arround an Array of SexType instances.
 * @see Fun.SexType
 */
class TypeAdap(
    private val c: BaseActivity,
    private val types: Array<SexType> = Fun.sexTypes(c),
) : ArrayAdapter<SexType>(c, 0, types) {
    private val li = LayoutInflater.from(c)

    @SuppressLint("ViewHolder")
    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        TypeSelectorBinding.inflate(li, parent, false).apply {
            root.setImageResource(types[i].icon)
            root.colorFilter = c.themePdcf()
        }.root

    override fun getDropDownView(i: Int, convertView: View?, parent: ViewGroup): View =
        TypeSelectorDdBinding.inflate(li, parent, false).apply {
            icon.setImageResource(types[i].icon)
            icon.colorFilter = c.themePdcf(com.google.android.material.R.attr.colorOnSecondary)
            name.text = types[i].name
        }.root
}
