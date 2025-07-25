package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.databinding.ItemGuessBinding
import ir.mahdiparastesh.sexbook.page.Estimation
import ir.mahdiparastesh.sexbook.page.Main
import ir.mahdiparastesh.sexbook.util.Delay
import ir.mahdiparastesh.sexbook.util.NumberUtils
import ir.mahdiparastesh.sexbook.util.NumberUtils.defCalendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.fullDate
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import ir.mahdiparastesh.sexbook.view.CustomSpinnerTouchListener
import ir.mahdiparastesh.sexbook.view.EasyPopupMenu
import ir.mahdiparastesh.sexbook.view.SexType
import ir.mahdiparastesh.sexbook.view.SpinnerTouchListener
import ir.mahdiparastesh.sexbook.view.UiTools.dbValue
import ir.mahdiparastesh.sexbook.view.UiTools.defaultOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Used in [Estimation] listing [Guess]es */
class GuessAdap(private val c: Estimation) :
    RecyclerView.Adapter<AnyViewHolder<ItemGuessBinding>>() {

    val places = c.c.places.sortedWith(Place.Sort(Place.Sort.NAME))

    @SuppressLint("SetTextI18n")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemGuessBinding> {
        val b = ItemGuessBinding.inflate(c.layoutInflater, parent, false)
        /* As soon as I applied this null safety feature, IndexOutOfBoundsException:
        "Inconsistency detected" exception appeared for the first time in the history of Sexbook! */

        // labels
        b.sincLabel.text = b.sincLabel.text.toString() + "*"
        b.tillLabel.text = b.tillLabel.text.toString() + "*"
        b.freqLabel.text = b.freqLabel.text.toString() + "*"

        // SexType
        b.type.adapter = SexType.Adapter(c)
        b.type.setOnTouchListener(SpinnerTouchListener())

        // Place
        b.place.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow,
            ArrayList(places.map { it.name }).apply { add(0, "") })
            .apply { setDropDownViewResource(R.layout.spinner_dd) }

        return AnyViewHolder(b)
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<ItemGuessBinding>, i: Int) {
        val g = c.c.guesses.getOrNull(i) ?: return
        if (g.active) h.b.root.alpha = 1f
        else Delay(350) { h.b.root.alpha = .7f }

        // Crush
        h.b.name.setTextWatcher(null)
        h.b.name.setText(g.name)
        h.b.name.setTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return
                val dbValue = h.b.name.dbValue()
                if (gu.name != dbValue) {
                    gu.name = dbValue
                    update(h.layoutPosition)
                }
            }
        })

        // since
        if (g.since > -1L)
            h.b.sinc.text = g.since.defCalendar(c.c).fullDate()
        else h.b.sinc.setText(R.string.etDateHint)
        h.b.sinc.setOnClickListener {
            val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return@setOnClickListener
            val oldTime = gu.since
            var oldSinc = (if (oldTime > -1L) oldTime else NumberUtils.now()).defCalendar(c.c)
            DatePickerDialog.newInstance({ _, year, month, day ->
                oldSinc.set(Calendar.YEAR, year)
                oldSinc.set(Calendar.MONTH, month)
                oldSinc.set(Calendar.DAY_OF_MONTH, day)
                oldSinc = McdtpUtils.trimToMidnight(oldSinc)
                h.b.sinc.text = oldSinc.fullDate()
                if (gu.since != oldSinc.timeInMillis) {
                    gu.since = oldSinc.timeInMillis
                    update(h.layoutPosition)
                }
            }, oldSinc).defaultOptions().show(c.supportFragmentManager, "sinc")
        }

        // until
        if (g.until > -1L)
            h.b.till.text = g.until.defCalendar(c.c).fullDate()
        else h.b.till.setText(R.string.etDateHint)
        h.b.till.setOnClickListener {
            val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return@setOnClickListener
            val oldTime = gu.until
            var oldTill = (if (oldTime > -1L) oldTime else NumberUtils.now()).defCalendar(c.c)
            DatePickerDialog.newInstance({ _, year, month, day ->
                oldTill.set(Calendar.YEAR, year)
                oldTill.set(Calendar.MONTH, month)
                oldTill.set(Calendar.DAY_OF_MONTH, day)
                oldTill = McdtpUtils.trimToMidnight(oldTill)
                h.b.till.text = oldTill.fullDate()
                if (gu.until != oldTill.timeInMillis) {
                    gu.until = oldTill.timeInMillis
                    update(h.layoutPosition)
                }
            }, oldTill).defaultOptions().show(c.supportFragmentManager, "till")
        }

        // frequency
        h.b.freq.setTextWatcher(null)
        h.b.freq.setText(g.frequency.toString())
        h.b.freq.setTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return
                try {
                    if (gu.frequency != h.b.freq.text.toString().toFloat()) {
                        gu.frequency = h.b.freq.text.toString().toFloat()
                        update(h.layoutPosition)
                    }
                } catch (_: NumberFormatException) {
                }
            }
        })

        // SexType
        h.b.type.setSelection(g.type.toInt(), true)
        h.b.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return
                if (gu.type != i.toByte()) {
                    gu.type = i.toByte()
                    update(h.layoutPosition)
                }
            }
        }

        // descriptions
        h.b.desc.setTextWatcher(null)
        h.b.desc.setText(g.description)
        h.b.desc.setTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return
                val dbValue = h.b.desc.dbValue()
                if (gu.description != dbValue) {
                    gu.description = dbValue
                    update(h.layoutPosition)
                }
            }
        })

        // Place
        var placeTouched = false
        h.b.place.setOnTouchListener(CustomSpinnerTouchListener { placeTouched = true })
        h.b.place.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (!placeTouched) return
                val id = if (i == 0) -1L else places[i - 1].id
                val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return
                if (gu.place != id) {
                    gu.place = id
                    update(h.layoutPosition)
                }
            }
        }
        h.b.place.setSelection(
            if (g.place == -1L) 0
            else ReportAdap.placePos(g.place, places) + 1,
            true
        )

        // long click
        val longClick = View.OnLongClickListener { v ->
            val gu = c.c.guesses.getOrNull(h.layoutPosition) ?: return@OnLongClickListener false

            EasyPopupMenu(
                c, v, R.menu.guess,
                R.id.glSuspend to {
                    c.c.guesses.getOrNull(h.layoutPosition)?.apply {
                        active = !active
                        CoroutineScope(Dispatchers.IO).launch {
                            @Suppress("LABEL_NAME_CLASH") c.c.dao.gUpdate(this@apply)
                            Main.changed = true
                            withContext(Dispatchers.Main) { notifyItemChanged(h.layoutPosition) }
                        }
                    }
                },
                R.id.glDelete to {
                    MaterialAlertDialogBuilder(c).apply {
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.etDeleteGuessSure))
                        setPositiveButton(R.string.yes) { _, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                c.c.dao.gDelete(gu)
                                c.c.guesses.removeAt(h.layoutPosition)
                                Main.changed = true
                                withContext(Dispatchers.Main) {
                                    val ii = h.layoutPosition
                                    notifyItemRemoved(ii)
                                    notifyItemRangeChanged(ii, itemCount - ii)
                                    c.count(c.c.guesses.size)
                                }
                            }
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                    }.show()
                }
            ).apply {
                if (!gu.active) menu.findItem(R.id.glSuspend)?.isChecked = true
            }.show()
            true
        }
        h.b.root.setOnLongClickListener(longClick)
        // h.b.sinc.setOnLongClickListener(longClick)
        // h.b.till.setOnLongClickListener(longClick)
        h.b.freq.setOnLongClickListener(longClick)
        h.b.type.setOnLongClickListener(longClick)
        h.b.desc.setOnLongClickListener(longClick)
        h.b.place.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = c.c.guesses.size

    fun update(i: Int) {
        if (c.c.guesses.size <= i || i < 0) return
        CoroutineScope(Dispatchers.IO).launch {
            c.c.dao.gUpdate(c.c.guesses[i])
            Main.changed = true
        }
    }
}
