package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.People
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ItemPersonBinding
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import ir.mahdiparastesh.sexbook.view.MaterialMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.or
import kotlin.experimental.xor

class PersonAdap(private val c: People) :
    RecyclerView.Adapter<AnyViewHolder<ItemPersonBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPersonBinding> =
        AnyViewHolder(ItemPersonBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<ItemPersonBinding>, i: Int) {
        val p = c.mm.visPeople.getOrNull(i)?.let { c.c.people[it] } ?: return

        // is active?
        h.b.active.setOnCheckedChangeListener(null)
        h.b.active.isChecked = p.active()
        h.b.active.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == p.active()) return@setOnCheckedChangeListener
            CoroutineScope(Dispatchers.IO).launch {
                p.status =
                    if (isChecked) (p.status xor Crush.STAT_INACTIVE)
                    else (p.status or Crush.STAT_INACTIVE)
                c.c.dao.cUpdate(p)
                withContext(Dispatchers.Main) { c.c.onCrushChanged(c, p.key, 1) }
            }
        }

        // name
        h.b.name.text = "${i + 1}. ${p.visName()}"
        h.b.sum.text = p.getSum(c.c).let { if (it != 0f) "{${it.show()}}" else "" }

        // clicks
        h.b.root.setOnClickListener {
            c.mm.visPeople.getOrNull(h.layoutPosition)?.also { Identify.create<People>(c, it) }
        }
        h.b.root.setOnLongClickListener { v ->
            val pk = c.mm.visPeople.getOrNull(h.layoutPosition)
                ?: return@setOnLongClickListener false
            val pc = c.c.people[pk] ?: return@setOnLongClickListener false

            // REMOVE THIS if you wanna add more options
            if (pc.insta.isNullOrBlank() && p.getSum(c.c) == 0f)
                return@setOnLongClickListener false

            MaterialMenu(
                c, v, R.menu.person,
                R.id.lcStatistics to {
                    c.goTo(Singular::class) { putExtra(Singular.EXTRA_CRUSH_KEY, pk) }
                },
                R.id.lcInstagram to {
                    if (pc.insta != null && pc.insta != "") try {
                        c.startActivity(
                            Intent(Intent.ACTION_VIEW, (Fun.INSTA + pc.insta).toUri())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: ActivityNotFoundException) {
                    }
                }
            ).apply {
                menu.findItem(R.id.lcInstagram).isVisible = !pc.insta.isNullOrBlank()
                menu.findItem(R.id.lcStatistics).isVisible = p.getSum(c.c) > 0f
            }.show()
            true
        }
    }

    override fun getItemCount() = c.mm.visPeople.size
}