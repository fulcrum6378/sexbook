package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ItemPersonBinding
import ir.mahdiparastesh.sexbook.page.Settings
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.or
import kotlin.experimental.xor

/** Used in [Settings] listing [Crush]es */
class BNtfCrushAdap(private val c: Settings) :
    RecyclerView.Adapter<AnyViewHolder<ItemPersonBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPersonBinding> =
        AnyViewHolder(ItemPersonBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<ItemPersonBinding>, i: Int) {
        val p = c.c.people[c.vm.bNtfCrushes[i]] ?: return

        // is active?
        h.b.active.setOnCheckedChangeListener(null)
        h.b.active.isChecked = p.notifyBirth()
        h.b.active.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == p.notifyBirth()) return@setOnCheckedChangeListener
            CoroutineScope(Dispatchers.IO).launch {
                p.apply {
                    status = if (isChecked) (status or Crush.STAT_NOTIFY_BIRTH)
                    else (status xor Crush.STAT_NOTIFY_BIRTH)
                }
                c.c.dao.cUpdate(p)
                withContext(Dispatchers.Main) {
                    c.c.onCrushChanged(c, p.key, 1)
                }
            }
        }

        // name
        h.b.name.text = "${i + 1}. ${p.visName()}"
        h.b.sum.text = p.getSum(c.c).let { if (it != 0f) "{${it.show()}}" else "" }

        // clicks
        h.b.root.setOnClickListener {
            c.vm.bNtfCrushes.getOrNull(h.layoutPosition)
                ?.also { Identify.create<Settings>(c, it) }
        }
        h.b.root.setOnLongClickListener {
            c.vm.bNtfCrushes.getOrNull(h.layoutPosition)?.also {
                c.goTo(Singular::class) { putExtra(Singular.EXTRA_CRUSH_KEY, it) }
            }; true
        }
    }

    override fun getItemCount() = c.vm.bNtfCrushes.size
}