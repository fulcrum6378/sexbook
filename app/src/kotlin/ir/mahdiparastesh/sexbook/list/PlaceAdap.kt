package ir.mahdiparastesh.sexbook.list

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.annotation.MainThread
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.databinding.ItemPlaceBinding
import ir.mahdiparastesh.sexbook.databinding.MigratePlaceBinding
import ir.mahdiparastesh.sexbook.page.Main
import ir.mahdiparastesh.sexbook.page.Places
import ir.mahdiparastesh.sexbook.page.Settings.Companion.spDefPlace
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import ir.mahdiparastesh.sexbook.view.EasyPopupMenu
import ir.mahdiparastesh.sexbook.view.UiTools.dbValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Used in [Places] listing [Place]s */
class PlaceAdap(private val c: Places) :
    RecyclerView.Adapter<AnyViewHolder<ItemPlaceBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemPlaceBinding> =
        AnyViewHolder(ItemPlaceBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: AnyViewHolder<ItemPlaceBinding>, i: Int) {
        val p = c.c.places.getOrNull(i) ?: return

        // name
        h.b.name.setTextWatcher(null)
        h.b.name.setText(p.name)
        h.b.name.setTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.c.places[h.layoutPosition].apply {
                    val dbValue = h.b.name.dbValue()
                    if (name != dbValue) {
                        name = dbValue
                        update(h.layoutPosition)
                    }
                }
            }
        })

        // sum
        (p.sum >= 0L).apply {
            h.b.sum.isVisible = this
            h.b.sum.text = if (this) "{${p.sum}}" else ""
        }

        // click
        val longClick = View.OnLongClickListener { v ->
            if (c.c.places.size <= h.layoutPosition) return@OnLongClickListener true

            EasyPopupMenu(
                c, v, R.menu.place,
                R.id.plDefPlace to {
                    c.c.sp.edit().apply {
                        putLong(spDefPlace, c.c.places[h.layoutPosition].id)
                        apply()
                    }
                },
                R.id.plDelete to {
                    if (c.c.places[h.layoutPosition].sum > 0
                    ) MaterialAlertDialogBuilder(c).apply {
                        val bm = MigratePlaceBinding.inflate(c.layoutInflater)
                        bm.places.adapter = ArrayAdapter(
                            c, R.layout.spinner_white,
                            ArrayList(c.c.places.map { it.name }).apply {
                                add(0, "")
                                remove(c.c.places[h.layoutPosition].name)
                            }).apply { setDropDownViewResource(R.layout.spinner_dd) }
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.plDeletePlaceSure))
                        setView(bm.root)
                        setPositiveButton(R.string.yes) { _, _ ->
                            deleteWithProgress(
                                h.layoutPosition, bm.places.selectedItem as String
                            )
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                        c.shake()
                    }.show()
                    else delete(h.layoutPosition, -1L)
                }
            ).apply {
                if (c.c.sp.contains(spDefPlace) && c.c.sp.getLong(spDefPlace, -1L)
                    == c.c.places[h.layoutPosition].id
                ) menu.findItem(R.id.plDefPlace).isChecked = true
            }.show()
            true
        }
        h.b.root.setOnLongClickListener(longClick)
        h.b.name.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = c.c.places.size

    fun update(i: Int) {
        if (c.c.places.size <= i || i < 0) return
        CoroutineScope(Dispatchers.IO).launch {
            c.c.dao.pUpdate(c.c.places[i])
            Main.changed = true
        }
    }

    fun delete(i: Int, migrateToId: Long, @MainThread onFinished: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            c.c.dao.pDelete(c.c.places[i])
            val nAlteredReports: Int
            for (mig in c.c.dao.rGetByPlace(c.c.places[i].id)
                .also { nAlteredReports = it.size })
                c.c.dao.rUpdate(mig.apply { place = migrateToId })
            for (mig in c.c.dao.gGetByPlace(c.c.places[i].id))
                c.c.dao.gUpdate(mig.apply { place = migrateToId })
            c.c.places.removeAt(i)

            Main.changed = true
            withContext(Dispatchers.Main) {
                notifyItemRemoved(i)
                //notifyItemRangeChanged(i, itemCount - i)
                c.count(c.c.places.size)
                onFinished()

                // reorganise the list regarding the new sum for that Place, if needed
                if (migrateToId != -1L) {
                    val newPlaceIndex = c.c.places.indexOfFirst { it.id == migrateToId }
                    if (newPlaceIndex != -1) c.c.places.getOrNull(newPlaceIndex)?.apply {
                        val oldSum = sum
                        sum += nAlteredReports
                        if (sum != oldSum) {
                            notifyItemChanged(newPlaceIndex)
                            c.c.places.sortWith(Place.Sort(Place.Sort.SUM))
                            val newPlaceNewIndex = c.c.places.indexOfFirst { it.id == migrateToId }
                            if (newPlaceIndex != newPlaceNewIndex && newPlaceNewIndex != -1)
                                notifyItemMoved(newPlaceIndex, newPlaceNewIndex)
                        }
                    }
                }
            }
        }
    }

    fun deleteWithProgress(i: Int, name: String) {
        c.shake()
        val progressDialog = MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.delete)
            setMessage(R.string.reassigningReports)
            setView(ProgressBar(c).apply {
                isIndeterminate = true
                setPadding(0, c.dp(8), 0, c.dp(25))
            })
            setCancelable(false)
        }.show()
        delete(i, c.c.places.find { it.name == name }?.id ?: -1L) {
            progressDialog.dismiss()
        }
    }
}
