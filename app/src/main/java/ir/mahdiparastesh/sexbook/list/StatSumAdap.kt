package ir.mahdiparastesh.sexbook.list

import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumChipGroupBinding
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.stat.Singular

class StatSumAdap(
    private val c: BaseActivity,
    private val arr: List<MutableMap.MutableEntry<Float, ArrayList<String>>>
) : RecyclerView.Adapter<AnyViewHolder<SumChipGroupBinding>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AnyViewHolder<SumChipGroupBinding> =
        AnyViewHolder(SumChipGroupBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<SumChipGroupBinding>, i: Int) {
        h.b.count.text =
            (if (arr[i].key % 1 > 0) arr[i].key.toString()
            else arr[i].key.toInt().toString()).plus(": ")

        while (h.b.root.childCount != 1) h.b.root.removeViewAt(h.b.root.childCount - 1)
        for (crush in arr[i].value) h.b.root.addView(
            Chip(ContextThemeWrapper(c.c, R.style.Theme_MaterialComponents)).apply {
                layoutParams = ChipGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = crush
                val bb = c.m.lookingFor
                    ?.let { it != "" && text.toString().contains(it, true) } ?: false
                chipBackgroundColor = AppCompatResources.getColorStateList(
                    c, if (!bb) R.color.chip_normal else R.color.chip_search
                )
                setTextColor(c.color(if (!bb) R.color.chipText else R.color.chipTextSearch))
                setOnClickListener {
                    c.m.crush = crush
                    c.startActivity(Intent(c, Singular::class.java))
                }
                typeface = ResourcesCompat.getFont(c, R.font.normal)!!
            })
    }

    override fun getItemCount() = arr.size
}
