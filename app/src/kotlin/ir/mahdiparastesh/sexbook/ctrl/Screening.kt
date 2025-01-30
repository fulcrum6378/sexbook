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

class Screening : BaseDialog<People>() {
    private lateinit var b: ScreeningBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = ScreeningBinding.inflate(c.layoutInflater)

        prepareSpinner(b.gender, c.resources.getStringArray(R.array.genders)) {
            //b.bodyBreasts.isVisible = i == 1 || i == 3
            //b.bodyPenis.isVisible = i == 2 || i == 3
        }
        prepareSpinner(
            b.fiction, arrayOf(
                getString(R.string.isFictional),
                getString(R.string.realPerson),
                getString(R.string.fictionalCharacter),
            )
        )
        prepareSpinner(
            b.safety, arrayOf(
                getString(R.string.isUnsafePerson),
                getString(R.string.safePerson),
                getString(R.string.unsafePerson),
            )
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
            }
            setNeutralButton(R.string.clear) { _, _ ->
                c.m.screening = null
                c.arrangeList()
            }
        }.create()
    }

    private fun prepareSpinner(
        spinner: Spinner, array: Array<String>, onItemSelected: ((i: Int) -> Unit)? = null
    ) {
        spinner.adapter = ArrayAdapter(c, R.layout.spinner_white, array)
            .apply { setDropDownViewResource(R.layout.spinner_dd) }
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
    )
}
