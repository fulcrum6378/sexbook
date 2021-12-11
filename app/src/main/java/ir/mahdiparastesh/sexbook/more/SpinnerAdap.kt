package ir.mahdiparastesh.sexbook.more

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.font1
import ir.mahdiparastesh.sexbook.R

class SpinnerAdap(items: List<String>) : ArrayAdapter<String>(c, R.layout.spinner_1, items) {
    init {
        setDropDownViewResource(R.layout.spinner_1_dd)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getView(position, convertView, parent) as TextView).apply {
            typeface = font1
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getDropDownView(position, convertView, parent) as TextView).apply {
            typeface = font1
        }
    }
}
