package ir.mahdiparastesh.sexbook.list

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.more.CustomTypefaceSpan
import ir.mahdiparastesh.sexbook.stat.Singular

class CrushAdap(val c: Main) : RecyclerView.Adapter<CrushAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemCrushBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemCrushBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Fonts
        b.name.typeface = Fun.font1Bold

        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (c.m.liefde.value == null) return
        h.b.name.text = c.m.liefde.value!![i].visName()

        // Click
        h.b.root.setOnClickListener { v ->
            if (!Main.summarize(c.m)) return@setOnClickListener
            val ins = c.m.liefde.value!![h.layoutPosition].insta
            PopupMenu(ContextThemeWrapper(Fun.c, R.style.AppTheme), v).apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.lcInstagram -> {
                            if (ins != null && ins != "") c.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(INSTA + c.m.liefde.value!![h.layoutPosition].insta)
                                )
                            )
                            true
                        }
                        R.id.lcStatistics -> {
                            c.m.crush.value = c.m.liefde.value!![i].key
                            c.startActivity(Intent(c, Singular::class.java))
                            true
                        }
                        R.id.lcDelete -> {
                            AlertDialog.Builder(c).apply {
                                setTitle(c.resources.getString(R.string.lcDelete))
                                setMessage(c.resources.getString(R.string.lcDeleteCrushSure))
                                setPositiveButton(R.string.yes) { _, _ ->
                                    Work(
                                        Work.C_DELETE_ONE,
                                        listOf(c.m.liefde.value!![h.layoutPosition], h.layoutPosition)
                                    ).start()
                                }
                                setNegativeButton(R.string.no, null)
                                setCancelable(true)
                            }.create().apply {
                                show()
                                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                                Fun.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
                            }
                            true
                        }
                        else -> false
                    }
                }
                inflate(R.menu.crush)
                show()
                menu.forEach {
                    val mNewTitle = SpannableString(it.title)
                    mNewTitle.setSpan(
                        CustomTypefaceSpan("", Fun.font1, Fun.dm.density * 16f), 0,
                        mNewTitle.length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    it.title = mNewTitle
                }
                menu.findItem(R.id.lcInstagram).isEnabled = ins != null && ins != ""
            }
        }
    }

    override fun getItemCount() = c.m.liefde.value!!.size

    companion object {
        const val INSTA = "https://www.instagram.com/"
    }
}
