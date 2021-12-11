package ir.mahdiparastesh.sexbook.stat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.color
import ir.mahdiparastesh.sexbook.Fun.Companion.dp
import ir.mahdiparastesh.sexbook.Fun.Companion.font1
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumChipsBinding

class SumChips : Fragment() {
    private lateinit var b: SumChipsBinding
    private lateinit var m: Model

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = SumChipsBinding.inflate(layoutInflater, parent, false)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)

        b.find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val ss = s.toString()
                for (i in 1 until b.ll.childCount) {
				    if (b.ll[i] is TextView) continue
                    val cg = b.ll[i] as ChipGroup
                    for (y in 1 until cg.childCount) (cg[y] as Chip).apply {
                        chipBackgroundColor = c.getColorStateList(
                            if (ss != "" && text.toString().contains(ss, true))
                                R.color.chip_search else R.color.chip_normal
                        )
                    }
                }
            }
        })
        b.find.typeface = font1
        for (r in m.summary.value!!.results().calculations) b.ll.addView(
            ChipGroup(ContextThemeWrapper(c, R.style.AppTheme), null, 0).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(TextView(c).apply {
                    layoutParams = ChipGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, dp(12), 0, 0)
                    text = (if (r.key % 1 > 0) r.key.toString()
                    else r.key.toInt().toString()).plus(": ")
                    setTextColor(color(R.color.recency))
                    textSize = Fun.dm.density * 5
                    typeface = font1
                })
                for (crush in r.value) addView(
                    Chip(ContextThemeWrapper(c, R.style.AppTheme), null, 0).apply {
                        layoutParams = ChipGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = crush
                        setTextColor(color(R.color.chipText))
                        chipBackgroundColor = c.getColorStateList(R.color.chip_normal)
                        setOnClickListener {
                            m.crush.value = crush
                            startActivity(Intent(c, Singular::class.java))
                        }
                        typeface = font1
                    })
            })
        if (m.summary.value!!.unknown > 0f) b.ll.addView(TextView(c).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, dp(7), 0, 0)
            text = getString(R.string.unknown, m.summary.value!!.unknown.toString())
            setTextColor(color(R.color.searchHint))
        })

        return b.root
    }
}
