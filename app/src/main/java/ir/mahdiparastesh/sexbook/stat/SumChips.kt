package ir.mahdiparastesh.sexbook.stat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumChipsBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class SumChips : Fragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }
    private lateinit var b: SumChipsBinding

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = SumChipsBinding.inflate(layoutInflater, parent, false)

        b.find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val ss = s.toString()
                for (i in 0 until b.ll.childCount) {
                    if (b.ll[i] !is ChipGroup) continue
                    val cg = b.ll[i] as ChipGroup
                    for (y in 1 until cg.childCount) (cg[y] as Chip).apply {
                        chipBackgroundColor = AppCompatResources.getColorStateList(
                            c.c, if (ss != "" && text.toString().contains(ss, true))
                                R.color.chip_search else R.color.chip_normal
                        )
                    }
                }
            }
        })
        b.find.typeface = c.font1
        for (r in c.m.summary.value!!.results().calculations) b.ll.addView(
            ChipGroup(c, null, 0).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(TextView(c).apply {
                    layoutParams = ChipGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, c.dp(12), 0, 0)
                    text = (if (r.key % 1 > 0) r.key.toString()
                    else r.key.toInt().toString()).plus(": ")
                    setTextColor(c.color(R.color.recency))
                    textSize = c.dm.density * 5
                    typeface = c.font1
                })
                for (crush in r.value) addView(
                    Chip(c, null, 0).apply {
                        layoutParams = ChipGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = crush
                        setTextColor(c.color(R.color.chipText))
                        chipBackgroundColor =
                            AppCompatResources.getColorStateList(c, R.color.chip_normal)
                        setOnClickListener {
                            c.m.crush = crush
                            startActivity(Intent(c, Singular::class.java))
                        }
                        typeface = c.font1
                    })
            })
        c.m.summary.value!!.nExcluded.also {
            if (it > 0f) b.ll.addView(plus(getString(R.string.excStat, it.toString())))
        }
        c.m.summary.value!!.unknown.also {
            if (it > 0f) b.ll.addView(plus(getString(R.string.unknown, it.toString())))
        }
        c.m.summary.value!!.nEstimated.also {
            if (it > 0f) b.ll.addView(plus(getString(R.string.estimated, it.toString())))
        }

        return b.root
    }

    fun plus(s: String) = TextView(c).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(0, c.dp(7), 0, 0)
        text = s
        setTextColor(c.color(R.color.searchHint))
        typeface = c.font1
    }
}
