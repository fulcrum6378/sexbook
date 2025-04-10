package ir.mahdiparastesh.sexbook.list

import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import ir.mahdiparastesh.sexbook.view.EasyPopupMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.or

class CrushAdap(private val c: Main) :
    RecyclerView.Adapter<AnyViewHolder<ItemCrushBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemCrushBinding> =
        AnyViewHolder(ItemCrushBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<ItemCrushBinding>, i: Int) {
        val cr = c.c.people[c.c.liefde[i]] ?: return

        // texts
        h.b.name.text = cr.visName()
        h.b.sum.text = cr.getSum(c.c).let { if (it != 0f) "{${it.show()}}" else "" }

        // clicks
        h.b.root.setOnClickListener { v ->
            if (!c.summarize(true)) return@setOnClickListener
            val crk = c.c.liefde.getOrNull(h.layoutPosition) ?: return@setOnClickListener
            val crc = c.c.people[crk] ?: return@setOnClickListener

            EasyPopupMenu(
                c, v, R.menu.crush,
                R.id.lcIdentify to { Identify.create<Main>(c, crk) },
                R.id.lcStatistics to {
                    c.goTo(Singular::class) { putExtra(Singular.EXTRA_CRUSH_KEY, crk) }
                },
                R.id.lcInstagram to {
                    if (crc.insta != null && crc.insta != "") try {
                        c.startActivity(
                            Intent(Intent.ACTION_VIEW, (Crush.INSTA + crc.insta).toUri())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: ActivityNotFoundException) {
                    }
                },
                R.id.lcDeactivate to {
                    crc.status = crc.status or Crush.STAT_INACTIVE
                    CoroutineScope(Dispatchers.IO).launch {
                        c.c.dao.cUpdate(crc)
                        withContext(Dispatchers.Main) { c.c.onCrushChanged(c, crk, 1) }
                    }
                    c.shake()
                }
            ).apply {
                menu.findItem(R.id.lcInstagram).isVisible = crc.insta != null && crc.insta != ""
                menu.findItem(R.id.lcStatistics).isVisible = cr.getSum(c.c) > 0.0
            }.show()
        }
        h.b.root.setOnLongClickListener {
            c.c.liefde.getOrNull(h.layoutPosition)?.also { Identify.create<Main>(c, it) }; true
        }
    }

    override fun getItemCount() = c.c.liefde.size
}
