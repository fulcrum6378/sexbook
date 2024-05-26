package ir.mahdiparastesh.sexbook.list

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.Fun.sumOf
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Identify
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import ir.mahdiparastesh.sexbook.stat.Singular
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.or

class CrushAdap(val c: Main) : RecyclerView.Adapter<AnyViewHolder<ItemCrushBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemCrushBinding> =
        AnyViewHolder(ItemCrushBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<ItemCrushBinding>, i: Int) {
        if (c.m.liefde == null) return

        // Texts
        h.b.name.text = c.m.liefde!![i].visName()
        h.b.sum.text = c.m.liefde!![i].getSum(c.m)
            .let { if (it != 0f) "{${it.show()}}" else "" }

        // Clicks
        h.b.root.setOnClickListener { v ->
            if (!c.summarize(true)) return@setOnClickListener
            val cr = c.m.liefde?.getOrNull(h.layoutPosition)
            val ins = cr?.insta
            MaterialMenu(c, v, R.menu.crush, Act().apply {
                this[R.id.lcInstagram] = {
                    if (ins != null && ins != "") try {
                        c.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(Fun.INSTA + c.m.liefde!![h.layoutPosition].insta)
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: ActivityNotFoundException) {
                    }
                }
                this[R.id.lcIdentify] = {
                    if (cr != null) identify(cr)
                }
                this[R.id.lcStatistics] = {
                    if (cr != null) c.goTo(Singular::class) {
                        putExtra(Singular.EXTRA_CRUSH_KEY, cr.key)
                    }
                }
                this[R.id.lcDeactivate] = {
                    if (cr != null) {
                        cr.status = cr.status or Crush.STAT_INACTIVE
                        CoroutineScope(Dispatchers.IO).launch {
                            c.m.dao.cUpdate(cr)
                            withContext(Dispatchers.Main) { c.m.onCrushChanged(c, cr, 1) }
                        }
                        c.shake()
                    }
                }
            }).apply {
                menu.findItem(R.id.lcInstagram).isVisible = ins != null && ins != ""
                val sum = cr?.key?.let { c.m.summary?.scores?.get(it) }
                    ?.sumOf { it.value }?.toDouble()
                menu.findItem(R.id.lcStatistics).isVisible = sum != null && sum > 0.0
            }.show()
        }
        h.b.root.setOnLongClickListener {
            c.m.liefde?.getOrNull(h.layoutPosition)?.also { identify(it) }; true
        }
    }

    override fun getItemCount() = c.m.liefde?.size ?: 0

    private fun identify(crush: Crush) {
        Identify(c, crush).apply {
            arguments = Bundle().apply { putString(Identify.BUNDLE_CRUSH_KEY, crush.key) }
            show(c.supportFragmentManager, Identify.TAG)
        }
    }
}
