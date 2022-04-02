package ir.mahdiparastesh.sexbook.more

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ir.mahdiparastesh.sexbook.R

class SpinnerAdap(val c: BaseActivity, items: List<String>) :
    ArrayAdapter<String>(c, R.layout.spinner, items) {
    init {
        setDropDownViewResource(R.layout.spinner_dd)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getView(position, convertView, parent) as TextView).apply {
            typeface = c.font1
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getDropDownView(position, convertView, parent) as TextView).apply {
            typeface = c.font1
        }
    }
}
