package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isInvisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.list.StatRecAdap
import ir.mahdiparastesh.sexbook.more.BaseDialog

class Recency : BaseDialog() {
    companion object {
        const val TAG = "recency"
    }

    /** Uses the Summary to compute recency of crushes. */
    private fun compute() {
        c.m.summary!!.scores.forEach { (name, erections) ->
            if (Summary.isUnknown(name)) return@forEach
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            c.m.recency.add(Item(name, mostRecent))
        }
        c.m.recency.sortByDescending { it.time }
    }

    data class Item(val name: String, val time: Long)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        compute()
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(resources.getString(R.string.recency))
            setView(draw())
            setPositiveButton(android.R.string.ok, null)
            setCancelable(true)
            setOnDismissListener {
                c.m.lookingFor = null // TODO
            }
        }.show()
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    private fun draw() = SearchableStatBinding.inflate(c.layoutInflater).apply {
        find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.lookingFor = s.toString()

                notFound.isInvisible = true
                if (list.adapter == null) return
                list.adapter?.notifyDataSetChanged()
                val firstOccur = if (!c.m.lookingFor.isNullOrEmpty())
                    c.m.recency.indexOfFirst { c.m.lookForIt(it.name) }
                        .let { if (it != -1) it else null }
                else null
                if (firstOccur != null) list.smoothScrollToPosition(firstOccur)
                notFound.isInvisible = firstOccur != null || c.m.lookingFor.isNullOrEmpty()
            }
        })
        c.m.lookingFor?.also { find.setText(it) }
        list.adapter = StatRecAdap(c as Main)
        list.clipToPadding = false
    }.root
}
