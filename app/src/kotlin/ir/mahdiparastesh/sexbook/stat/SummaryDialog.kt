package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.list.SummaryAdap
import ir.mahdiparastesh.sexbook.page.Main
import ir.mahdiparastesh.sexbook.util.NumberUtils.show

class SummaryDialog : BaseDialog<Main>(), BaseDialog.SearchableStat {
    override var lookingFor: String? = null

    companion object : BaseDialogCompanion() {
        private const val TAG = "summary"

        fun create(c: BaseActivity) {
            if (isDuplicate()) return
            SummaryDialog().show(c.supportFragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        c.c.summary!!.classify(c.c)
        val b = SearchableStatBinding.inflate(c.layoutInflater)

        b.find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                lookingFor = s.toString()
                b.notFound.isInvisible = true
                (b.list.adapter as? SummaryAdap)?.also {
                    it.notifyDataSetChanged()

                    var firstGroup: Int? = null
                    if (!lookingFor.isNullOrEmpty()) for (group in it.list.indices) {
                        for (chip in it.list[group].value)
                            if (lookForIt(chip)) {
                                firstGroup = group
                                break; }
                        if (firstGroup != null) break
                    }
                    if (firstGroup != null) b.list.smoothScrollToPosition(firstGroup)
                    b.notFound.isInvisible =
                        firstGroup != null || lookingFor.isNullOrEmpty()
                }
            }
        })

        if (b.list.adapter == null) b.list.adapter =
            SummaryAdap(c, c.c.summary!!.classification!!.entries.toList(), this)
        b.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                b.listTopShadow.isInvisible = b.list.computeVerticalScrollOffset() == 0
            }
        })

        val pluses = LinearLayout(c).apply {
            id = R.id.pluses
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
            orientation = LinearLayout.VERTICAL
            val padH = resources.getDimension(R.dimen.ssPadH).toInt()
            setPadding(padH, (padH * 0.1f).toInt(), padH, (padH * 0.6f).toInt())
        }
        c.c.summary?.nExcluded?.also {
            if (it > 0f) pluses.addView(
                plus(c, getString(R.string.excStat, it.toString()))
            )
        }
        c.c.summary?.unknown?.also {
            if (it > 0f) pluses.addView(
                plus(c, getString(R.string.unknown, it.show()))
            )
        }
        c.c.summary?.nonCrush?.also {
            if (it > 0f) pluses.addView(
                plus(c, getString(R.string.nonCrush, it.show()))
            )
        }
        c.c.summary?.unsafe?.also {
            if (it > 0f) pluses.addView(
                plus(c, getString(R.string.plusUnsafe, it.show()))
            )
        }
        c.c.summary?.disappeared?.also {
            if (it > 0f) pluses.addView(
                plus(c, getString(R.string.plusDisappeared, it.show()))
            )
        }
        b.root.addView(pluses)
        pluses.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {  // like an onLoaded listener
                pluses.viewTreeObserver.removeOnGlobalLayoutListener(this)
                b.list.setPaddingRelative(
                    b.list.paddingStart, b.list.paddingTop, b.list.paddingEnd,
                    b.list.paddingBottom + pluses.height
                )
            }
        })

        isCancelable = true
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(
                "${getString(R.string.summary)} " +
                        "(${c.c.summary!!.apparent.show()} / ${c.c.reports.size()})"
            )
            setView(b.root)
        }.create()
    }

    private fun plus(c: BaseActivity, s: String) = TextView(c).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(0, c.dp(5), 0, c.dp(2))
        text = s
        setTextColor(c.themeColor(com.google.android.material.R.attr.colorOnSecondary))
        textSize = c.resources.getDimension(R.dimen.plusesFont) / c.dm.density
        alpha = .8f
        typeface = resources.getFont(R.font.normal)
    }
}
