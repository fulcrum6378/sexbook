package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.SumChipGroupBinding
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.stat.Singular

class StatSumAdap(
    private val c: BaseActivity,
    val arr: List<MutableMap.MutableEntry<Float, ArrayList<String>>>,
    private val searchable: BaseDialog.SearchableStat,
) : RecyclerView.Adapter<AnyViewHolder<SumChipGroupBinding>>() {
    private val statOnlyCrushes = c.sp.getBoolean(Settings.spStatOnlyCrushes, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<SumChipGroupBinding> =
        AnyViewHolder(SumChipGroupBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<SumChipGroupBinding>, i: Int) {
        h.b.count.text = arr[i].key.show().plus(": ")

        for (crush in arr[i].value.indices) (if (h.b.root.childCount < crush + 2) Chip(c).apply {
            layoutParams = ChipGroup.LayoutParams(-2, -2)
            typeface = ResourcesCompat.getFont(c, R.font.normal)!!
            h.b.root.addView(this)
        } else h.b.root.getChildAt(crush + 1) as Chip
                ).apply {
                val crushKey = arr[i].value[crush]
                text = crushKey +
                        (if (!statOnlyCrushes && crushKey in c.m.liefde) "*" else "")
                val bb = searchable.lookForIt(text.toString())
                setOnClickListener {
                    c.goTo(Singular::class) {
                        putExtra(Singular.EXTRA_CRUSH_KEY, arr[h.layoutPosition].value[crush])
                    }
                }
                isActivated = bb
                isVisible = true
            }
        for (hide in arr[i].value.size + 1 until h.b.root.childCount)
            h.b.root.getChildAt(hide).isVisible = false
    }

    override fun getItemCount() = arr.size
}
