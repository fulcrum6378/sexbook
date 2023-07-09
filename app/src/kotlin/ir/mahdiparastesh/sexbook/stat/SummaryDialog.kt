package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.sexbook.Fun.onLoad
import ir.mahdiparastesh.sexbook.Fun.tripleRound
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.databinding.SumPieBinding
import ir.mahdiparastesh.sexbook.list.StatSumAdap
import ir.mahdiparastesh.sexbook.more.BaseDialog
import ir.mahdiparastesh.sexbook.more.BaseFragment

class SummaryDialog : BaseDialog() {
    private var dialogue: AlertDialog? = null
    private var pager: ViewPager2? = null

    companion object {
        const val TAG = "summary"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        pager = ViewPager2(c).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            adapter = SumAdapter(c)
        }
        dialogue = MaterialAlertDialogBuilder(c).apply {
            setTitle("${getString(R.string.summary)} (${c.m.summary!!.actual} / ${c.m.onani.value!!.size})")
            setView(ConstraintLayout(c).apply {
                layoutParams = ViewGroup.LayoutParams(-1, -1)
                addView(pager)
                // The EditText below improves the EditText focus issue when you put
                // a Fragment inside a Dialog with a ViewPager in the middle!
                addView(EditText(c).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, -2)
                    visibility = View.GONE
                })
            })
            setPositiveButton(android.R.string.ok, null)
            setNeutralButton(R.string.chart, null)
            setCancelable(true)
        }.create()
        return dialogue!!
    }

    override fun onResume() {
        super.onResume()
        dialogue?.getButton(AlertDialog.BUTTON_NEUTRAL)
            ?.setOnClickListener { pager?.currentItem = 1 }
    }

    private inner class SumAdapter(c: FragmentActivity) : FragmentStateAdapter(c) {
        override fun getItemCount(): Int = 2
        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> SumPie()
            else -> SumChips()
        }
    }

    class SumChips : BaseFragment(), SearchableStat {
        private lateinit var b: SearchableStatBinding
        override var lookingFor: String? = null

        override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
            SearchableStatBinding.inflate(inf, parent, false).apply { b = this }.root

        @SuppressLint("NotifyDataSetChanged")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            b.find.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    lookingFor = s.toString()
                    b.notFound.isInvisible = true
                    (b.list.adapter as? StatSumAdap)?.also {
                        it.notifyDataSetChanged()

                        var firstGroup: Int? = null
                        if (!lookingFor.isNullOrEmpty()) for (group in it.arr.indices) {
                            for (chip in it.arr[group].value)
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
            lookingFor?.also { b.find.setText(it) }

            if (c.m.summary == null) return
            b.list.adapter = StatSumAdap(
                c, c.m.summary!!.results().calculations.entries.toList(), this@SumChips
            )

            val pluses = LinearLayout(c).apply {
                id = R.id.pluses
                layoutParams = ConstraintLayout.LayoutParams(-1, -2)
                    .apply { bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID }
                orientation = LinearLayout.VERTICAL
                resources.getDimension(R.dimen.ssPadH).toInt().also { setPadding(it, 0, it, 0) }
            }
            c.m.summary?.nExcluded?.also {
                if (it > 0f) pluses.addView(
                    plus(b.root.context, getString(R.string.excStat, it.toString()))
                )
            }
            c.m.summary?.unknown?.also {
                if (it > 0f) pluses.addView(
                    plus(b.root.context, getString(R.string.unknown, it.toString()))
                )
            }
            b.root.addView(pluses)
            pluses.onLoad {
                b.list.setPaddingRelative(
                    b.list.paddingStart, b.list.paddingTop, b.list.paddingEnd,
                    b.list.paddingBottom + pluses.height
                )
            }
        }

        fun plus(c: Context, s: String) = AppCompatTextView(c).apply {
            layoutParams = LinearLayout.LayoutParams(-2, -2)
            setPadding(0, this@SumChips.c.dp(5), 0, this@SumChips.c.dp(2))
            text = s
            textSize = c.resources.getDimension(R.dimen.plusesFont) / this@SumChips.c.dm.density
            alpha = .8f
            typeface = ResourcesCompat.getFont(c, R.font.normal)!!
        }
    }

    class SumPie : BaseFragment() {
        private lateinit var b: SumPieBinding

        override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
            SumPieBinding.inflate(layoutInflater, parent, false).apply { b = this }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val data = arrayListOf<SliceValue>()
            c.m.summary?.scores?.entries?.sortedBy {
                it.value.sumOf { s -> s.value.toDouble() }.toFloat()
            }?.forEach {
                val score = it.value.sumOf { s -> s.value.toDouble() }.toFloat()
                data.add(
                    SliceValue(score, c.color(R.color.CPV_LIGHT))
                        .apply { setLabel("${it.key} {${score.tripleRound()}}") })
            }
            b.root.pieChartData = PieChartData(data).apply {
                setHasLabelsOnlyForSelected(true) // setHasLabels(true)
            }
        }
    }
}
