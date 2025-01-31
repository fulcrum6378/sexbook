package ir.mahdiparastesh.sexbook.ctrl

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.People
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.ScreeningBinding

/** An advanced interface for filtering people. */
class Screening : BaseDialog<People>() {
    private lateinit var b: ScreeningBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = ScreeningBinding.inflate(c.layoutInflater)

        prepareSpinner(
            b.gender, c.resources.getStringArray(R.array.genders),
            c.m.screening?.gender ?: 0
        ) {
            //b.bodyBreasts.isVisible = i == 1 || i == 3
            //b.bodyPenis.isVisible = i == 2 || i == 3
        }
        prepareSpinner(
            b.fiction, arrayOf(
                getString(R.string.isFictional),
                getString(R.string.realPerson),
                getString(R.string.fictionalCharacter),
            ), c.m.screening?.fiction ?: 0
        )
        prepareSpinner(
            b.safety, arrayOf(
                getString(R.string.isUnsafePerson),
                getString(R.string.safePerson),
                getString(R.string.unsafePerson),
            ), c.m.screening?.safety ?: 0
        )

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.filter)
            setView(b.root)
            setPositiveButton(R.string.apply) { _, _ ->
                c.m.screening = Filters(
                    b.gender.selectedItemPosition,
                    b.fiction.selectedItemPosition,
                    b.safety.selectedItemPosition,
                    0
                )
                c.arrangeList()
                c.updateFilterIcon()
            }
            setNeutralButton(R.string.clear) { _, _ ->
                c.m.screening = null
                c.arrangeList()
                c.updateFilterIcon()
            }
        }.create()
    }

    private fun prepareSpinner(
        spinner: Spinner,
        array: Array<String>,
        defaultPos: Int,
        onItemSelected: ((i: Int) -> Unit)? = null
    ) {
        spinner.adapter = ArrayAdapter(c, R.layout.spinner_white, array)
            .apply { setDropDownViewResource(R.layout.spinner_dd) }
        spinner.setSelection(defaultPos)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                spinner.alpha = if (i == 0) Fun.DISABLED_ALPHA else 1f
                onItemSelected?.let { it(i) }
            }
        }
    }

    data class Filters(
        val gender: Int,
        val fiction: Int,
        val safety: Int,
        val body: Int,
    ) {
        fun any() = gender != 0 || fiction != 0 || safety != 0 || body != 0
    }
}
