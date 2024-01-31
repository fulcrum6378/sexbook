package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isInvisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.list.StatRecAdap
import ir.mahdiparastesh.sexbook.more.BaseDialog

class Recency : BaseDialog(), BaseDialog.SearchableStat {
    val items: ArrayList<Item> = ArrayList()
    override var lookingFor: String? = null

    data class Item(val name: String, val time: Long)

    companion object {
        const val TAG = "recency"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = true
        if (items.isEmpty()) compute()
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(resources.getString(R.string.recency))
            setView(draw())
            setPositiveButton(android.R.string.ok, null)
        }.create()
    }

    /** Uses Summary to compute recency of crushes. */
    private fun compute() {
        c.m.summary!!.scores.forEach { (name, erections) ->
            if (Summary.isUnknown(name)) return@forEach
            var mostRecent = 0L
            for (e in erections) if (e.time > mostRecent) mostRecent = e.time
            items.add(Item(name, mostRecent))
        }
        items.sortByDescending { it.time }
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    private fun draw() = SearchableStatBinding.inflate(c.layoutInflater).apply {
        find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                lookingFor = s.toString()

                notFound.isInvisible = true
                if (list.adapter == null) return
                list.adapter?.notifyDataSetChanged()
                val firstOccur = if (!lookingFor.isNullOrEmpty())
                    items.indexOfFirst { lookForIt(it.name) }
                        .let { if (it != -1) it else null }
                else null
                if (firstOccur != null) list.smoothScrollToPosition(firstOccur)
                notFound.isInvisible = firstOccur != null || lookingFor.isNullOrEmpty()
            }
        })
        list.adapter = StatRecAdap(this@Recency)
        list.clipToPadding = false
    }.root
}
