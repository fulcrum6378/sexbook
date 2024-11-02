package ir.mahdiparastesh.sexbook.list

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import ir.mahdiparastesh.sexbook.misc.Act
import ir.mahdiparastesh.sexbook.misc.AnyViewHolder
import ir.mahdiparastesh.sexbook.misc.MaterialMenu
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
        val cr = c.m.people[c.m.liefde[i]] ?: return

        // Texts
        h.b.name.text = cr.visName()
        h.b.sum.text = cr.getSum(c.m).let { if (it != 0f) "{${it.show()}}" else "" }

        // Clicks
        h.b.root.setOnClickListener { v ->
            if (!c.summarize(true)) return@setOnClickListener
            val crk = c.m.liefde.getOrNull(h.layoutPosition) ?: return@setOnClickListener
            val crc = c.m.people[crk] ?: return@setOnClickListener

            MaterialMenu(c, v, R.menu.crush, Act().apply {
                this[R.id.lcInstagram] = {
                    if (crc.insta != null && crc.insta != "") try {
                        c.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(Fun.INSTA + crc.insta)
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: ActivityNotFoundException) {
                    }
                }
                this[R.id.lcIdentify] = {
                    Identify.display(c, crk)
                }
                this[R.id.lcStatistics] = {
                    c.goTo(Singular::class) {
                        putExtra(Singular.EXTRA_CRUSH_KEY, crk)
                    }
                }
                this[R.id.lcDeactivate] = {
                    crc.status = crc.status or Crush.STAT_INACTIVE
                    CoroutineScope(Dispatchers.IO).launch {
                        c.m.dao.cUpdate(crc)
                        withContext(Dispatchers.Main) { c.m.onCrushChanged(c, crk, 1) }
                    }
                    c.shake()
                }
            }).apply {
                menu.findItem(R.id.lcInstagram).isVisible = crc.insta != null && crc.insta != ""
                val sum = c.m.summary?.scores?.get(crk)
                    ?.sumOf { it.value }?.toDouble()
                menu.findItem(R.id.lcStatistics).isVisible = sum != null && sum > 0.0
            }.show()
        }
        h.b.root.setOnLongClickListener {
            c.m.liefde.getOrNull(h.layoutPosition)?.also { Identify.display(c, it) }; true
        }
    }

    override fun getItemCount() = c.m.liefde.size
}
