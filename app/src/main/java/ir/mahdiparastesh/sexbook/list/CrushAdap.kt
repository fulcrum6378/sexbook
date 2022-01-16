package ir.mahdiparastesh.sexbook.list

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import ir.mahdiparastesh.sexbook.stat.Singular

class CrushAdap(val c: Main) : RecyclerView.Adapter<CrushAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemCrushBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemCrushBinding.inflate(c.layoutInflater, parent, false)

        // Fonts
        b.name.typeface = c.font1Bold

        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (c.m.liefde.value == null) return
        h.b.name.text = c.m.liefde.value!![i].visName()

        // Click
        h.b.root.setOnClickListener { v ->
            if (!Main.summarize(c.m)) return@setOnClickListener
            val ins = c.m.liefde.value!![h.layoutPosition].insta
            MaterialMenu(c, v, R.menu.crush, Act().apply {
                this[R.id.lcInstagram] = {
                    if (ins != null && ins != "") c.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(INSTA + c.m.liefde.value!![h.layoutPosition].insta)
                        )
                    )
                }
                this[R.id.lcStatistics] = {
                    c.m.crush = c.m.liefde.value!![i].key
                    c.startActivity(Intent(c, Singular::class.java))
                }
                this[R.id.lcDelete] = {
                    AlertDialog.Builder(c).apply {
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.lcDeleteCrushSure))
                        setPositiveButton(R.string.yes) { _, _ ->
                            Work(
                                c, Work.C_DELETE_ONE,
                                listOf(
                                    c.m.liefde.value!![h.layoutPosition],
                                    h.layoutPosition
                                )
                            ).start()
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                    }.create().apply {
                        show()
                        c.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                        c.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
                    }
                }
            }).apply { menu.findItem(R.id.lcInstagram).isEnabled = ins != null && ins != "" }
                .show()
        }
    }

    override fun getItemCount() = c.m.liefde.value!!.size

    companion object {
        const val INSTA = "https://www.instagram.com/"
    }
}
