package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SearchableStatBinding
import ir.mahdiparastesh.sexbook.list.StatSumAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity

class SumChips : Fragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }
    private lateinit var b: SearchableStatBinding

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        SearchableStatBinding.inflate(inf, parent, false).apply { b = this }.root

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.lookingFor = s.toString()
                b.list.adapter?.notifyDataSetChanged()
            }
        })
        c.m.lookingFor?.also { b.find.setText(it) }

        if (c.m.summary == null) return
        b.list.adapter = StatSumAdap(c, c.m.summary!!.results().calculations.entries.toList())

        val pluses = LinearLayout(c).apply {
            id = R.id.pluses
            layoutParams = ConstraintLayout.LayoutParams(-1, -2)
                .apply { bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID }
            orientation = LinearLayout.VERTICAL
            setPadding(c.dp(20), 0, c.dp(20), 0)
        }
        (b.list.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            bottomToTop = R.id.pluses
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
    }

    fun plus(c: Context, s: String) = AppCompatTextView(c).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(0, this@SumChips.c.dp(5), 0, this@SumChips.c.dp(2))
        text = s
        alpha = .8f
        typeface = ResourcesCompat.getFont(c, R.font.normal)!!
    }
}
