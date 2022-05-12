package ir.mahdiparastesh.sexbook.list

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import ir.mahdiparastesh.sexbook.stat.Singular

class CrushAdap(val c: Main) : RecyclerView.Adapter<AnyViewHolder<ItemCrushBinding>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AnyViewHolder<ItemCrushBinding> {
        val b = ItemCrushBinding.inflate(c.layoutInflater, parent, false)
        b.name.typeface = c.font1Bold
        return AnyViewHolder(b)
    }

    override fun onBindViewHolder(h: AnyViewHolder<ItemCrushBinding>, i: Int) {
        if (c.m.liefde.value == null) return

        // Name
        h.b.name.text = c.m.liefde.value!![i].visName()

        // Sum
        h.b.sum.text = c.m.summary.value?.scores?.get(c.m.liefde.value!![i].key)
            ?.sumOf { it.value.toDouble() }?.let { "{$it}" } ?: ""

        // Click
        h.b.root.setOnClickListener { v ->
            if (!c.summarize()) return@setOnClickListener
            val cr = c.m.liefde.value?.get(h.layoutPosition)
            val ins = cr?.insta
            MaterialMenu(c, v, R.menu.crush, Act().apply {
                this[R.id.lcInstagram] = {
                    if (ins != null && ins != "") c.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(INSTA + c.m.liefde.value!![h.layoutPosition].insta)
                        )
                    )
                }
                this[R.id.lcIdentify] = {
                    if (cr != null) Singular.identify(c, cr, PageLove.handler.value)
                }
                this[R.id.lcStatistics] = {
                    c.m.crush = c.m.liefde.value!![i].key
                    c.startActivity(Intent(c, Singular::class.java))
                }
            }).apply {
                menu.findItem(R.id.lcInstagram).isEnabled = ins != null && ins != ""
                val sum = c.m.summary.value?.scores?.get(c.m.liefde.value!![i].key)
                    ?.sumOf { it.value.toDouble() }
                menu.findItem(R.id.lcStatistics).isEnabled = sum != null && sum > 0.0
            }.show()
        }
    }

    override fun getItemCount() = c.m.liefde.value?.size ?: 0

    companion object {
        const val INSTA = "https://www.instagram.com/"
    }
}
