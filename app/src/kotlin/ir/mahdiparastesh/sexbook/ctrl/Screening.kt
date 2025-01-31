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
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ScreeningBinding

/** An advanced interface for filtering people. */
class Screening : BaseDialog<People>() {
    private lateinit var b: ScreeningBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = ScreeningBinding.inflate(c.layoutInflater)

        prepareSpinner(b.gender, R.array.genders, c.m.screening?.gender) { i ->
            b.bodyBreasts.isVisible = i == 1 || i == 3
            b.bodyPenis.isVisible = i == 2 || i == 3
        }
        prepareSpinner(
            b.fiction, arrayOf(
                getString(R.string.isFictional),
                getString(R.string.realPerson),
                getString(R.string.fictionalCharacter),
            ), c.m.screening?.fiction
        )
        prepareSpinner(
            b.safety, arrayOf(
                getString(R.string.isUnsafePerson),
                getString(R.string.safePerson),
                getString(R.string.unsafePerson),
            ), c.m.screening?.safety
        )
        val body = c.m.screening?.body
        prepareSpinner(
            b.bodySkinColour, R.array.bodySkinColour,
            body?.let { (body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second }
        )
        prepareSpinner(
            b.bodyHairColour, R.array.bodyHairColour,
            body?.let { (body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second }
        )
        prepareSpinner(
            b.bodyEyeColour, R.array.bodyEyeColour,
            body?.let { (body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second }
        )
        prepareSpinner(
            b.bodyEyeShape, R.array.bodyEyeShape,
            body?.let { (body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second }
        )
        prepareSpinner(
            b.bodyFaceShape, R.array.bodyFaceShape,
            body?.let { (body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second }
        )
        prepareSpinner(
            b.bodyFat, R.array.bodyFat,
            body?.let { (body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second }
        )
        prepareSpinner(
            b.bodyBreasts, R.array.bodyBreasts,
            body?.let { (body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second }
        )
        prepareSpinner(
            b.bodyPenis, R.array.bodyPenis,
            body?.let { (body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second }
        )
        prepareSpinner(
            b.bodyMuscle, R.array.bodyMuscle,
            body?.let { (body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second }
        )
        prepareSpinner(
            b.bodySexuality, R.array.bodySexuality,
            body?.let { (body and Crush.BODY_SEXUALITY.first) shr Crush.BODY_SEXUALITY.second }
        )

        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.filter)
            setView(b.root)
            setPositiveButton(R.string.apply) { _, _ ->
                c.m.screening = Filters(
                    b.gender.selectedItemPosition,
                    b.fiction.selectedItemPosition,
                    b.safety.selectedItemPosition,
                    (b.bodySkinColour.selectedItemPosition shl Crush.BODY_SKIN_COLOUR.second) or
                            (b.bodyHairColour.selectedItemPosition shl Crush.BODY_HAIR_COLOUR.second) or
                            (b.bodyEyeColour.selectedItemPosition shl Crush.BODY_EYE_COLOUR.second) or
                            (b.bodyEyeShape.selectedItemPosition shl Crush.BODY_EYE_SHAPE.second) or
                            (b.bodyFaceShape.selectedItemPosition shl Crush.BODY_FACE_SHAPE.second) or
                            (b.bodyFat.selectedItemPosition shl Crush.BODY_FAT.second) or
                            (b.bodyMuscle.selectedItemPosition shl Crush.BODY_MUSCLE.second) or
                            (b.bodyBreasts.selectedItemPosition shl Crush.BODY_BREASTS.second) or
                            (b.bodyPenis.selectedItemPosition shl Crush.BODY_PENIS.second) or
                            (b.bodySexuality.selectedItemPosition shl Crush.BODY_SEXUALITY.second)
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
        val body: Int,
    ) {
        fun any() = gender != 0 || fiction != 0 || safety != 0 || body != 0
    }
}
