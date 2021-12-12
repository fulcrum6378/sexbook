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
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemCrushBinding
import ir.mahdiparastesh.sexbook.more.CustomTypefaceSpan
import ir.mahdiparastesh.sexbook.stat.Singular

class CrushAdap(val list: List<Crush>, val that: Main) :
    RecyclerView.Adapter<CrushAdap.MyViewHolder>() {
    class MyViewHolder(val b: ItemCrushBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemCrushBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Fonts
        b.name.typeface = Fun.font1Bold

        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        h.b.name.text = list[i].visName()

        // Click
        h.b.root.setOnClickListener { v ->
            if (!Main.summarize(that.m)) return@setOnClickListener
            PopupMenu(ContextThemeWrapper(Fun.c, R.style.AppTheme), v).apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.lcInstagram -> {
                            val ins = list[h.layoutPosition].insta
                            if (ins != null && ins != "") that.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(INSTA + list[h.layoutPosition].insta)
                                )
                            ) else it.isEnabled = false
                            true
                        }
                        R.id.lcStatistics -> {
                            that.m.crush.value = list[i].key
                            that.startActivity(Intent(that, Singular::class.java))
                            true
                        }
                        R.id.lcDelete -> {
                            AlertDialog.Builder(that).apply {
                                setTitle(that.resources.getString(R.string.lcDelete))
                                setMessage(that.resources.getString(R.string.lcDeleteCrushSure))
                                setPositiveButton(R.string.yes) { _, _ ->
                                    Work(
                                        Work.C_DELETE_ONE,
                                        listOf(list[h.layoutPosition], h.layoutPosition)
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
            }
        }
    }

    override fun getItemCount() = list.size

    companion object {
        const val INSTA = "https://www.instagram.com/"
    }
}
