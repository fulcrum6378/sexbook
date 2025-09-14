package ir.mahdiparastesh.sexbook.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.DrawableRes
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.databinding.TypeSelectorBinding
import ir.mahdiparastesh.sexbook.databinding.TypeSelectorDdBinding
import ir.mahdiparastesh.sexbook.page.Settings

/**
 * Data class that indicates a sex type.
 * @param name visible name
 * @param icon visible icon from drawable resources
 */
data class SexType(
    val name: String,
    @DrawableRes val icon: Int
) {

    companion object {

        /** The number of all the available sex types. */
        const val count = 5

        /** @return an Array of all the available sex types */
        fun all(c: Context): Array<SexType> {
            val names = c.resources.getStringArray(R.array.sexTypes)
            return arrayOf(
                SexType(names[0], R.drawable.sex_type_wet_dream),
                SexType(names[1], R.drawable.sex_type_masturbation),
                SexType(names[2], R.drawable.sex_type_oral_sex),
                SexType(names[3], R.drawable.sex_type_anal_sex),
                SexType(names[4], R.drawable.sex_type_vaginal_sex),
            )
        }

        /** @return an ArrayList of the IDs of the allowed sex types based on shared preferences */
        fun allowedOnes(sp: SharedPreferences) = arrayListOf<Byte>().apply {
            for (s in 0 until count)
                if (sp.getBoolean(Settings.spStatInclude + s, true))
                    add(s.toByte())
            if (isEmpty()) addAll((0 until count).map { it.toByte() })
        }
    }

    /** An [ArrayAdapter] that wraps arround an Array of [SexType] instances. */
    class Adapter(
        private val c: BaseActivity,
        private val types: Array<SexType> = all(c),
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
}
