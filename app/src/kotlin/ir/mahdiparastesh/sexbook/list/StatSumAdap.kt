package ir.mahdiparastesh.sexbook.list

import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumChipGroupBinding
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.BaseDialog
import ir.mahdiparastesh.sexbook.stat.Singular

class StatSumAdap(
    private val c: BaseActivity,
    val arr: List<MutableMap.MutableEntry<Float, ArrayList<String>>>,
    private val searchable: BaseDialog.SearchableStat,
) : RecyclerView.Adapter<AnyViewHolder<SumChipGroupBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<SumChipGroupBinding> =
        AnyViewHolder(SumChipGroupBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<SumChipGroupBinding>, i: Int) {
        h.b.count.text = Fun.decimalCount(arr[i].key).plus(": ")

        for (crush in arr[i].value.indices) (if (h.b.root.childCount < crush + 2) Chip(c).apply {
            layoutParams = ChipGroup.LayoutParams(-2, -2)
            typeface = ResourcesCompat.getFont(c, R.font.normal)!!
            h.b.root.addView(this)
        } else h.b.root.getChildAt(crush + 1) as Chip).apply {
            text = arr[i].value[crush]
            val bb = searchable.lookForIt(text.toString())
            setOnClickListener {
                c.goTo(Singular::class) {
                    putExtra(Singular.EXTRA_CRUSH_KEY, arr[h.layoutPosition].value[crush])
                }
            }
            isActivated = bb
            vis(true)
        }
        for (hide in arr[i].value.size + 1 until h.b.root.childCount)
            h.b.root.getChildAt(hide).vis(false)
    }

    override fun getItemCount() = arr.size
}
