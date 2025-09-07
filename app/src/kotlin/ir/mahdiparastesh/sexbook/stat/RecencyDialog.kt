package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.list.RecencyAdap
import ir.mahdiparastesh.sexbook.page.Main

class RecencyDialog : BaseDialog<Main>(), BaseDialog.SearchableStat {
    val items: ArrayList<Item> = ArrayList()
    override var lookingFor: String? = null

    data class Item(val name: String, val time: Long)

    companion object : BaseDialogCompanion() {
        private const val TAG = "recency"

        fun create(c: BaseActivity) {
            if (isDuplicate()) return
            RecencyDialog().show(c.supportFragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = true
        if (items.isEmpty()) compute()
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(resources.getString(R.string.recency))
            setView(draw())
        }.create()
    }

    /** Uses [Summary] to compute recency of crushes. */
    private fun compute() {
        val hideUnsafe = c.c.hideUnsafe()
        val hideDisappeared = c.c.hideDisappeared()

        for ((key, score) in c.c.summary!!.scores) {
            if (hideUnsafe && key in c.c.unsafe) continue
            if (hideDisappeared && key in c.c.disappeared) continue

            var mostRecent = 0L
            for (e in score.orgasms) if (e.time > mostRecent) mostRecent = e.time
            items.add(Item(key, mostRecent))
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
        if (list.adapter == null) list.adapter = RecencyAdap(this@RecencyDialog)
        list.clipToPadding = false
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                listTopShadow.isInvisible = list.computeVerticalScrollOffset() == 0
            }
        })
    }.root
}
