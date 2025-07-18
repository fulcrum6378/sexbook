package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.databinding.RecencyBinding
import ir.mahdiparastesh.sexbook.stat.OtherPartners
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.view.AnyViewHolder

/** Used in [OtherPartners] */
class OtherPartnersAdap(private val c: Singular) :
    RecyclerView.Adapter<AnyViewHolder<RecencyBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<RecencyBinding> =
        AnyViewHolder(RecencyBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<RecencyBinding>, i: Int) {
        val p = c.vm.otherPartners[i]
        h.b.name.text = "${i + 1}. ${p.name}"
        h.b.date.text = when (p.times) {  // TODO translate
            1 -> "Once"
            2 -> "Twice"
            3 -> "Thrice"
            else -> "${p.times} times"
        }
        h.b.sep.isVisible = i != c.vm.otherPartners.size - 1
        h.b.root.setOnClickListener {
            c.goTo(Singular::class) {
                putExtra(Singular.EXTRA_CRUSH_KEY, p.name)
            }
        }
    }

    override fun getItemCount() = c.vm.otherPartners.size
}
