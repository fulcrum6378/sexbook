package ir.mahdiparastesh.sexbook.stat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
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

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        SumChipsBinding.inflate(inf, parent, false).apply { b = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val ss = s.toString()
                for (i in 0 until b.ll.childCount) if (b.ll[i] is ChipGroup) {
                    val cg = b.ll[i] as ChipGroup
                    for (y in 1 until cg.childCount) (cg[y] as Chip).updateSearch(ss)
                }
                c.m.lookingFor = ss
            }
        })
        val lookingFor = c.m.lookingFor
        lookingFor?.also { b.find.setText(it) }

        if (c.m.summary == null) return
        for (r in c.m.summary!!.results().calculations) b.ll.addView(
            ChipGroup(b.ll.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(AppCompatTextView(context).apply {
                    layoutParams = ChipGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, c.dp(12), 0, 0)
                    text = (if (r.key % 1 > 0) r.key.toString()
                    else r.key.toInt().toString()).plus(": ")
                    textSize = c.dm.density * 5
                    typeface = ResourcesCompat.getFont(c, R.font.normal)!!
                })
                for (crush in r.value) addView(
                    Chip(ContextThemeWrapper(c.c, R.style.Theme_MaterialComponents)).apply {
                        layoutParams = ChipGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = crush
                        updateSearch(lookingFor)
                        setOnClickListener {
                            c.m.crush = crush
                            startActivity(Intent(c, Singular::class.java))
                        }
                        typeface = ResourcesCompat.getFont(c, R.font.normal)!!
                    })
            })
        c.m.summary?.nExcluded?.also {
            if (it > 0f) b.ll.addView(
                plus(b.ll.context, getString(R.string.excStat, it.toString()))
            )
        }
        c.m.summary?.unknown?.also {
            if (it > 0f) b.ll.addView(
                plus(b.ll.context, getString(R.string.unknown, it.toString()))
            )
        }
    }

    fun plus(c: Context, s: String) = AppCompatTextView(c).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(0, this@SumChips.c.dp(7), 0, 0)
        text = s
        alpha = .8f
        typeface = ResourcesCompat.getFont(c, R.font.normal)!!
    }

    private fun Chip.updateSearch(ss: String?) {
        val bb = ss != null && ss != "" && text.toString().contains(ss, true)
        chipBackgroundColor = AppCompatResources.getColorStateList(
            c, if (!bb) R.color.chip_normal else R.color.chip_search
        )
        setTextColor(c.color(if (!bb) R.color.chipText else R.color.chipTextSearch))
    }
}
