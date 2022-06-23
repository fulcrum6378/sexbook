package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
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
import ir.mahdiparastesh.sexbook.mdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import ir.mahdiparastesh.sexbook.more.TypeAdap
import kotlin.collections.set

class GuessAdap(val c: Estimation) : RecyclerView.Adapter<AnyViewHolder<ItemGuessBinding>>() {
    val places = c.m.places.value?.sortedWith(Place.Sort(Place.Sort.NAME))

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AnyViewHolder<ItemGuessBinding> {
        val b = ItemGuessBinding.inflate(c.layoutInflater, parent, false)

        // Type
        b.type.adapter = TypeAdap(c)

        // Place
        b.placeMark.setColorFilter(c.color(R.color.spnFilterMark))
        places?.let { l ->
            b.place.adapter = ArrayAdapter(c, R.layout.spinner,
                ArrayList(l.map { it.name }).apply { add(0, "") })
                .apply { setDropDownViewResource(R.layout.spinner_dd) }
        }

        return AnyViewHolder(b)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(h: AnyViewHolder<ItemGuessBinding>, i: Int) {
        if (c.m.guesses.value == null) return

        // Since
        if (c.m.guesses.value!![i].sinc > -1L)
            h.b.sinc.text = c.m.guesses.value!![i].sinc.defCalendar(c).fullDate()
        else h.b.sinc.setText(R.string.etDateHint)
        h.b.sinc.setOnClickListener {
            val oldTime = c.m.guesses.value!![h.layoutPosition].sinc
            val oldSinc = (if (oldTime > -1L) oldTime else now()).defCalendar(c)
            DatePickerDialog.newInstance({ _, time ->
                h.b.sinc.text = time.defCalendar(c).fullDate()
                c.m.guesses.value!![h.layoutPosition].apply {
                    if (sinc != time) {
                        sinc = time
                        update(h.layoutPosition)
                    }
                }
            }, oldSinc).defaultOptions(c).show(c.supportFragmentManager, "sinc")
        }

        // Until
        if (c.m.guesses.value!![i].till > -1L)
            h.b.till.text = c.m.guesses.value!![i].till.defCalendar(c).fullDate()
        else h.b.till.setText(R.string.etDateHint)
        h.b.till.setOnClickListener {
            val oldTime = c.m.guesses.value!![h.layoutPosition].till
            val oldTill = (if (oldTime > -1L) oldTime else now()).defCalendar(c)
            DatePickerDialog.newInstance({ _, time ->
                h.b.till.text = time.defCalendar(c).fullDate()
                c.m.guesses.value!![h.layoutPosition].apply {
                    if (till != time) {
                        till = time
                        update(h.layoutPosition)
                    }
                }
            }, oldTill).defaultOptions(c).show(c.supportFragmentManager, "till")
        }

        // Frequency
        h.b.freq.setText(c.m.guesses.value!![i].freq.toString())
        h.b.freq.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    c.m.guesses.value!![h.layoutPosition].apply {
                        if (freq != h.b.freq.text.toString().toFloat()) {
                            freq = h.b.freq.text.toString().toFloat()
                            update(h.layoutPosition)
                        }
                    }
                } catch (ignored: NumberFormatException) {
                }
            }
        })

        // Type
        h.b.type.setSelection(c.m.guesses.value!![i].type.toInt(), true)
        h.b.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                c.m.guesses.value!![h.layoutPosition].apply {
                    if (type != i.toByte()) {
                        type = i.toByte()
                        update(h.layoutPosition)
                    }
                }
            }
        }

        // Descriptions
        h.b.desc.setText(c.m.guesses.value!![i].desc)
        h.b.desc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.guesses.value!![h.layoutPosition].apply {
                    if (desc != h.b.desc.text.toString()) {
                        desc = h.b.desc.text.toString()
                        update(h.layoutPosition)
                    }
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
                c.m.guesses.value!![h.layoutPosition].apply {
                    if (plac != id) {
                        plac = id
                        update(h.layoutPosition)
                    }
                }
            }
        }
        h.b.place.setSelection(
            if (c.m.guesses.value!![i].plac == -1L || places == null) 0
            else ReportAdap.placePos(c.m.guesses.value!![i].plac, places) + 1,
            true
        )

        // Long Click
        val longClick = View.OnLongClickListener { v ->
            MaterialMenu(c, v, R.menu.guess, Act().apply {
                this[R.id.glDelete] = {
                    AlertDialog.Builder(c).apply {
                        setTitle(c.resources.getString(R.string.delete))
                        setMessage(c.resources.getString(R.string.etDeleteGuessSure))
                        setPositiveButton(R.string.yes) { _, _ ->
                            Work(
                                c, Work.G_DELETE_ONE,
                                listOf(c.m.guesses.value!![h.layoutPosition], h.layoutPosition)
                            ).start()
                        }
                        setNegativeButton(R.string.no, null)
                        setCancelable(true)
                    }.show()
                }
            }).show()
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
        if (c.m.guesses.value == null) return
        if (c.m.guesses.value!!.size <= i || i < 0) return
        Work(c, Work.G_UPDATE_ONE, listOf(c.m.guesses.value!![i], i, refresh)).start()
    }

    // Don't migrate to Java!
    class Sort : Comparator<Guess> {
        override fun compare(a: Guess, b: Guess) = a.sinc.compareTo(b.sinc)
    }
}
