package ir.mahdiparastesh.sexbook.ctrl

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.ArrayRes
import androidx.core.view.isVisible
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

        prepareSpinner(b.gender, R.array.genders, c.c.screening?.gender) { i ->
            val bb = i == 1 || i == 3
            b.bodyBreasts.isVisible = bb
            b.bodyPenis.isVisible = bb
            if (!bb) {
                b.bodyBreasts.setSelection(0)
                b.bodyPenis.setSelection(0)
            }
        }
        prepareSpinner(
            b.fiction, arrayOf(
                getString(R.string.isFictional),
                getString(R.string.realPerson),
                getString(R.string.fictionalCharacter),
            ), c.c.screening?.fiction
        )
        prepareSpinner(
            b.safety, arrayOf(
                getString(R.string.isUnsafePerson),
                getString(R.string.safePerson),
                getString(R.string.unsafePerson),
            ), c.c.screening?.safety
        )
        prepareSpinner(b.bodySkinColour, R.array.bodySkinColour, c.c.screening?.bodySkinColour)
        prepareSpinner(b.bodyHairColour, R.array.bodyHairColour, c.c.screening?.bodyHairColour)
        prepareSpinner(b.bodyEyeColour, R.array.bodyEyeColour, c.c.screening?.bodyEyeColour)
        prepareSpinner(b.bodyEyeShape, R.array.bodyEyeShape, c.c.screening?.bodyEyeShape)
        prepareSpinner(b.bodyFaceShape, R.array.bodyFaceShape, c.c.screening?.bodyFaceShape)
        prepareSpinner(b.bodyFat, R.array.bodyFat, c.c.screening?.bodyFat)
        prepareSpinner(b.bodyBreasts, R.array.bodyBreasts, c.c.screening?.bodyBreasts)
        prepareSpinner(b.bodyPenis, R.array.bodyPenis, c.c.screening?.bodyPenis)
        prepareSpinner(b.bodyMuscle, R.array.bodyMuscle, c.c.screening?.bodyMuscle)
        prepareSpinner(b.bodySexuality, R.array.bodySexuality, c.c.screening?.bodySexuality)

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.filter)
            setView(b.root)
            setPositiveButton(R.string.apply) { _, _ ->
                c.c.screening = Filters(
                    b.gender.selectedItemPosition,
                    b.fiction.selectedItemPosition,
                    b.safety.selectedItemPosition,
                    b.bodySkinColour.selectedItemPosition, b.bodyHairColour.selectedItemPosition,
                    b.bodyEyeColour.selectedItemPosition, b.bodyEyeShape.selectedItemPosition,
                    b.bodyFaceShape.selectedItemPosition, b.bodyFat.selectedItemPosition,
                    b.bodyBreasts.selectedItemPosition, b.bodyPenis.selectedItemPosition,
                    b.bodyMuscle.selectedItemPosition, b.bodySexuality.selectedItemPosition,
                )
                c.arrangeList()
                c.updateFilterIcon()
            }
            setNeutralButton(R.string.clear) { _, _ ->
                c.c.screening = null
                c.arrangeList()
                c.updateFilterIcon()
            }
        }.create()
    }

    private fun prepareSpinner(
        spinner: Spinner,
        @ArrayRes arrayRes: Int,
        defaultPos: Int?,
        onItemSelected: ((i: Int) -> Unit)? = null
    ) {
        prepareSpinner(
            spinner, spinner.context.resources.getStringArray(arrayRes), defaultPos, onItemSelected
        )
    }

    private fun prepareSpinner(
        spinner: Spinner,
        array: Array<String>,
        defaultPos: Int?,
        onItemSelected: ((i: Int) -> Unit)? = null
    ) {
        spinner.adapter = ArrayAdapter(c, R.layout.spinner_white, array)
            .apply { setDropDownViewResource(R.layout.spinner_dd) }
        spinner.setSelection(defaultPos ?: 0)
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
        val bodySkinColour: Int, val bodyHairColour: Int,
        val bodyEyeColour: Int, val bodyEyeShape: Int,
        val bodyFaceShape: Int, val bodyFat: Int,
        val bodyBreasts: Int, val bodyPenis: Int,
        val bodyMuscle: Int, val bodySexuality: Int,
    ) {
        fun any() = gender != 0 || fiction != 0 || safety != 0 ||
                bodySkinColour != 0 || bodyHairColour != 0 ||
                bodyEyeColour != 0 || bodyEyeShape != 0 ||
                bodyFaceShape != 0 || bodyFat != 0 ||
                bodyBreasts != 0 || bodyPenis != 0 ||
                bodyMuscle != 0 || bodySexuality != 0
    }
}
