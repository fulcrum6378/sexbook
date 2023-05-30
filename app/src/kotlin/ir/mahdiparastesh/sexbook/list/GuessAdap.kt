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
import ir.mahdiparastesh.sexbook.Estimation
import ir.mahdiparastesh.sexbook.Fun.defCalendar
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.now
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemGuessBinding
import ir.mahdiparastesh.sexbook.more.*
import kotlin.collections.set

class GuessAdap(val c: Estimation) : RecyclerView.Adapter<AnyViewHolder<ItemGuessBinding>>() {
    val places = c.m.places.value?.sortedWith(Place.Sort(Place.Sort.NAME))

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AnyViewHolder<ItemGuessBinding> {
        val b = ItemGuessBinding.inflate(c.layoutInflater, parent, false)
        /* As soon as I applied this null safety feature, IndexOutOfBoundsException:
        "Inconsistency detected" exception appeared for the first time in the history of Sexbook! */

        // Labels
        b.sincLabel.text = b.sincLabel.text.toString() + "*"
        b.tillLabel.text = b.tillLabel.text.toString() + "*"
        b.freqLabel.text = b.freqLabel.text.toString() + "*"

        // Type
        b.type.adapter = TypeAdap(c)

        // Place
        places?.also { l ->
            b.place.adapter = ArrayAdapter(c, R.layout.spinner,
                ArrayList(l.map { it.name }).apply { add(0, "") })
                .apply { setDropDownViewResource(R.layout.spinner_dd) }
        }

        return AnyViewHolder(b)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(h: AnyViewHolder<ItemGuessBinding>, i: Int) {
        val g = c.m.guesses.value?.getOrNull(i) ?: return
        if (g.able) h.b.root.alpha = 1f
        else Delay(350) { h.b.root.alpha = .7f }

        // Crush
        h.b.crsh.setText(g.crsh)
        h.b.crsh.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return
                if (gu.crsh != h.b.crsh.text.toString()) {
                    gu.crsh = h.b.crsh.text.toString()
                    update(h.layoutPosition)
                }
            }
        })

        // Since
        if (g.sinc > -1L)
            h.b.sinc.text = g.sinc.defCalendar(c).fullDate()
        else h.b.sinc.setText(R.string.etDateHint)
        h.b.sinc.setOnClickListener {
            val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return@setOnClickListener
            val oldTime = gu.sinc
            var oldSinc = (if (oldTime > -1L) oldTime else now()).defCalendar(c)
            DatePickerDialog.newInstance({ _, year, month, day ->
                oldSinc.set(Calendar.YEAR, year)
                oldSinc.set(Calendar.MONTH, month)
                oldSinc.set(Calendar.DAY_OF_MONTH, day)
                oldSinc = McdtpUtils.trimToMidnight(oldSinc)
                h.b.sinc.text = oldSinc.fullDate()
                if (gu.sinc != oldSinc.timeInMillis) {
                    gu.sinc = oldSinc.timeInMillis
                    update(h.layoutPosition)
                }
            }, oldSinc).defaultOptions(c).show(c.supportFragmentManager, "sinc")
        }

        // Until
        if (g.till > -1L)
            h.b.till.text = g.till.defCalendar(c).fullDate()
        else h.b.till.setText(R.string.etDateHint)
        h.b.till.setOnClickListener {
            val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return@setOnClickListener
            val oldTime = gu.till
            var oldTill = (if (oldTime > -1L) oldTime else now()).defCalendar(c)
            DatePickerDialog.newInstance({ _, year, month, day ->
                oldTill.set(Calendar.YEAR, year)
                oldTill.set(Calendar.MONTH, month)
                oldTill.set(Calendar.DAY_OF_MONTH, day)
                oldTill = McdtpUtils.trimToMidnight(oldTill)
                h.b.till.text = oldTill.fullDate()
                if (gu.till != oldTill.timeInMillis) {
                    gu.till = oldTill.timeInMillis
                    update(h.layoutPosition)
                }
            }, oldTill).defaultOptions(c).show(c.supportFragmentManager, "till")
        }

        // Frequency
        h.b.freq.setText(g.freq.toString())
        h.b.freq.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return
                try {
                    if (gu.freq != h.b.freq.text.toString().toFloat()) {
                        gu.freq = h.b.freq.text.toString().toFloat()
                        update(h.layoutPosition)
                    }
                } catch (ignored: NumberFormatException) {
                }
            }
        })

        // Type
        h.b.type.setSelection(g.type.toInt(), true)
        h.b.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return
                if (gu.type != i.toByte()) {
                    gu.type = i.toByte()
                    update(h.layoutPosition)
                }
            }
        }

        // Descriptions
        h.b.desc.setText(g.desc)
        h.b.desc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return
                if (gu.desc != h.b.desc.text.toString()) {
                    gu.desc = h.b.desc.text.toString()
                    update(h.layoutPosition)
                }
            }
        })

        // Place
        var placeTouched = false
        h.b.place.setOnTouchListener { _, _ -> placeTouched = true; false }
        h.b.place.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (places == null || !placeTouched) return
                val id = if (i == 0) -1L else places[i - 1].id
                val gu = c.m.guesses.value?.getOrNull(h.layoutPosition) ?: return
                if (gu.plac != id) {
                    gu.plac = id
                    update(h.layoutPosition)
                }
            }
        }
        h.b.place.setSelection(
            if (g.plac == -1L || places == null) 0
            else ReportAdap.placePos(g.plac, places) + 1,
            true
        )

        // Long Click
        val longClick = View.OnLongClickListener { v ->
            val gu = c.m.guesses.value?.getOrNull(h.layoutPosition)
                ?: return@OnLongClickListener false
            MaterialMenu(c, v, R.menu.guess, Act().apply {
                this[R.id.glSuspend] = {
                    c.m.guesses.value?.getOrNull(h.layoutPosition)?.apply {
                        able = !able
                        Work(c, Work.G_UPDATE_ONE, listOf(this, h.layoutPosition, 1)).start()
                    }
                }
                this[R.id.glDelete] = {
                    MaterialAlertDialogBuilder(c).apply {
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.etDeleteGuessSure))
                        setPositiveButton(R.string.yes) { _, _ ->
                            Work(
                                c, Work.G_DELETE_ONE,
                                listOf(gu, h.layoutPosition)
                            ).start()
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                    }.show()
                }
            }).apply {
                if (!gu.able) menu.findItem(R.id.glSuspend)?.isChecked = true
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

    override fun getItemCount() = c.m.guesses.value?.size ?: 0

    fun update(i: Int, refresh: Int = 0) {
        c.m.guesses.value?.also { list ->
            if (list.size <= i || i < 0) return
            Work(c, Work.G_UPDATE_ONE, listOf(list[i], i, refresh)).start()
        }
    }

    // Don't migrate to Java!
    class Sort : Comparator<Guess> {
        override fun compare(a: Guess, b: Guess) = a.sinc.compareTo(b.sinc)
    }
}
