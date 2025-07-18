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
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.ScreeningBinding
import ir.mahdiparastesh.sexbook.page.People
import ir.mahdiparastesh.sexbook.util.NumberUtils.DISABLED_ALPHA
import ir.mahdiparastesh.sexbook.view.SpinnerTouchListener

/** An advanced interface for filtering people as a dialog box */
class Screening : BaseDialog<People>() {
    private lateinit var b: ScreeningBinding

    companion object : BaseDialogCompanion() {
        private const val TAG = "screening"

        fun create(c: BaseActivity) {
            if (isDuplicate()) return
            Screening().show(c.supportFragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = ScreeningBinding.inflate(c.layoutInflater)

        if (b.search.text.isEmpty()) b.search.setText(c.c.screening?.search)

        // status
        prepareSpinner(
            b.presence, R.array.presence, c.c.screening?.presence
        )
        prepareSpinner(
            b.gender, R.array.genders, c.c.screening?.gender
        ) { i ->
            val bb = i == 1 || i == 3
            b.bodyBreasts.isVisible = bb
            if (!bb) b.bodyBreasts.setSelection(0)

            val bp = i == 2 || i == 3
            b.bodyPenis.isVisible = bp
            if (!bp) b.bodyPenis.setSelection(0)
        }
        prepareSpinner(
            b.safety, R.array.unsafeness, c.c.screening?.safety
        )

        // reports
        b.minSum.setText(c.c.screening?.minSum?.toString())

        // body attributes
        prepareSpinner(
            b.bodySkinColour, R.array.bodySkinColour,
            c.c.screening?.bodySkinColour
        )
        prepareSpinner(
            b.bodyHairColour, R.array.bodyHairColour,
            c.c.screening?.bodyHairColour
        )
        prepareSpinner(
            b.bodyEyeColour, R.array.bodyEyeColour,
            c.c.screening?.bodyEyeColour
        )
        prepareSpinner(
            b.bodyEyeShape, R.array.bodyEyeShape,
            c.c.screening?.bodyEyeShape
        )
        prepareSpinner(
            b.bodyFaceShape, R.array.bodyFaceShape,
            c.c.screening?.bodyFaceShape
        )
        prepareSpinner(
            b.bodyFat, R.array.bodyFat,
            c.c.screening?.bodyFat
        )
        prepareSpinner(
            b.bodyBreasts, R.array.bodyBreasts,
            c.c.screening?.bodyBreasts
        )
        prepareSpinner(
            b.bodyPenis, R.array.bodyPenis,
            c.c.screening?.bodyPenis
        )
        prepareSpinner(
            b.bodyMuscle, R.array.bodyMuscle,
            c.c.screening?.bodyMuscle
        )

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.filter)
            setView(b.root)
            setPositiveButton(R.string.apply) { _, _ ->
                c.c.screening = Filters(
                    b.search.text.toString(),
                    b.presence.selectedItemPosition,
                    b.gender.selectedItemPosition,
                    b.safety.selectedItemPosition,
                    b.minSum.text.toString().toIntOrNull() ?: 0,
                    b.bodySkinColour.selectedItemPosition,
                    b.bodyHairColour.selectedItemPosition,
                    b.bodyEyeColour.selectedItemPosition,
                    b.bodyEyeShape.selectedItemPosition,
                    b.bodyFaceShape.selectedItemPosition,
                    b.bodyFat.selectedItemPosition,
                    b.bodyBreasts.selectedItemPosition,
                    b.bodyPenis.selectedItemPosition,
                    b.bodyMuscle.selectedItemPosition,
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
        spinner.adapter = ArrayAdapter(
            c, R.layout.spinner_white,
            spinner.context.resources.getStringArray(arrayRes)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        spinner.setSelection(defaultPos ?: 0)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                spinner.alpha = if (i == 0) DISABLED_ALPHA else 1f
                onItemSelected?.let { it(i) }
            }
        }
        spinner.setOnTouchListener(SpinnerTouchListener())
    }

    data class Filters(
        val search: String,
        val presence: Int,
        val gender: Int,
        val safety: Int,
        val minSum: Int,
        val bodySkinColour: Int, val bodyHairColour: Int,
        val bodyEyeColour: Int, val bodyEyeShape: Int,
        val bodyFaceShape: Int, val bodyFat: Int,
        val bodyBreasts: Int, val bodyPenis: Int,
        val bodyMuscle: Int,
    ) {
        fun any() = search.isNotBlank() ||
                presence != 0 || gender != 0 || safety != 0 ||
                minSum > 0 ||
                bodySkinColour != 0 || bodyHairColour != 0 ||
                bodyEyeColour != 0 || bodyEyeShape != 0 ||
                bodyFaceShape != 0 || bodyFat != 0 ||
                bodyBreasts != 0 || bodyPenis != 0 ||
                bodyMuscle != 0
    }
}
