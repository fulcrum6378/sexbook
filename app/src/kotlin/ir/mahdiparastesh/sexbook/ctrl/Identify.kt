package ir.mahdiparastesh.sexbook.ctrl

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.sexbook.People
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.stat.Singular
import ir.mahdiparastesh.sexbook.util.NumberUtils.DISABLED_ALPHA
import ir.mahdiparastesh.sexbook.view.SpinnerTouchListener
import ir.mahdiparastesh.sexbook.view.UiTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.and
import kotlin.experimental.or

/** An AlertDialog for filling data for a [Crush] */
class Identify<Activity> private constructor() :
    BaseDialog<Activity>() where Activity : BaseActivity {

    companion object {
        private const val TAG = "identify"
        const val BUNDLE_CRUSH_KEY = "crush_key"
        var crush: Crush? = null

        fun <Activity> create(c: BaseActivity, crush: String) where Activity : BaseActivity {
            Companion.crush = c.c.people[crush]
            Identify<Activity>().apply {
                arguments = Bundle().apply { putString(BUNDLE_CRUSH_KEY, crush) }
                show(c.supportFragmentManager, TAG)
            }
        }
    }

    private lateinit var b: IdentifyBinding
    private val cancellability: CountDownTimer = object : CountDownTimer(15000, 15000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            isCancelable = false
        }
    }
    private val accFromUrl = arrayOf(Crush.INSTA, "https://instagram.com/")

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = IdentifyBinding.inflate(c.layoutInflater)  // takes ~550 milliseconds
        ContextCompat.getColorStateList(c, R.color.chip)
            .also { b.notifyBirth.trackTintList = it }
        b.root.scrollTo(0, 0)

        // gender
        b.gender.adapter = ArrayAdapter(
            c, R.layout.spinner_white, c.resources.getStringArray(R.array.genders)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.gender.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                b.gender.alpha = if (i == 0) DISABLED_ALPHA else 1f
                b.bodyBreasts.isVisible = i == 1 || i == 3
                b.bodyPenis.isVisible = i == 2 || i == 3
            }
        }
        b.gender.setOnTouchListener(SpinnerTouchListener())

        // Instagram
        b.instagram.filters = arrayOf(object : InputFilter {
            override fun filter(
                source: CharSequence?, start: Int, end: Int,
                dest: Spanned?, dstart: Int, dend: Int
            ): CharSequence? {
                if (source?.let { it.length > 20 } != true) return null
                val str = source.toString()
                for (host in accFromUrl)
                    if (str.startsWith(host))
                        return str.substringAfter(host).substringBefore("/")
                            .substringBefore("?")
                return null
            }
        })

        // body characteristics
        prepareBodyAttrSpinner(b.bodySkinColour, R.array.bodySkinColour)
        prepareBodyAttrSpinner(b.bodyHairColour, R.array.bodyHairColour)
        prepareBodyAttrSpinner(b.bodyEyeColour, R.array.bodyEyeColour)
        prepareBodyAttrSpinner(b.bodyEyeShape, R.array.bodyEyeShape)
        prepareBodyAttrSpinner(b.bodyFaceShape, R.array.bodyFaceShape)
        prepareBodyAttrSpinner(b.bodyFat, R.array.bodyFat)
        prepareBodyAttrSpinner(b.bodyBreasts, R.array.bodyBreasts)
        prepareBodyAttrSpinner(b.bodyPenis, R.array.bodyPenis)
        prepareBodyAttrSpinner(b.bodyMuscle, R.array.bodyMuscle)
        prepareBodyAttrSpinner(b.bodySexuality, R.array.bodySexuality)

        // default values
        val crushKey = crush?.key ?: requireArguments().getString(BUNDLE_CRUSH_KEY)!!
        b.key.setText(crushKey)
        crush?.apply {
            b.fName.setText(fName)
            b.mName.setText(mName)
            b.lName.setText(lName)
            b.gender.setSelection((status and Crush.STAT_GENDER).toInt())
            b.fiction.isChecked = fiction().also { onFictionChanged(it) }
            b.unsafe.isChecked = unsafe()
            b.notifyBirth.isChecked = notifyBirth()
            if (height != -1f) b.height.setText(height.toString())
            b.address.setText(address)
            b.instagram.setText(insta)
            b.bodySkinColour.setSelection(
                (body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second
            )
            b.bodyHairColour.setSelection(
                (body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second
            )
            b.bodyEyeColour.setSelection(
                (body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second
            )
            b.bodyEyeShape.setSelection(
                (body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second
            )
            b.bodyFaceShape.setSelection(
                (body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second
            )
            b.bodyFat.setSelection(
                (body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second
            )
            b.bodyMuscle.setSelection(
                (body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second
            )
            b.bodyBreasts.setSelection(
                (body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second
            )
            b.bodyPenis.setSelection(
                (body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second
            )
            b.bodySexuality.setSelection(
                (body and Crush.BODY_SEXUALITY.first) shr Crush.BODY_SEXUALITY.second
            )
        }
        b.birth.setText(UiTools.validateDateTime(crush?.birth ?: ""))
        b.firstMet.setText(UiTools.validateDateTime(crush?.first ?: ""))

        // fictionality
        b.fiction.setOnCheckedChangeListener { _, isChecked -> onFictionChanged(isChecked) }

        // date-time Fields
        b.birth.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) b.birth.setText(UiTools.validateDateTime(b.birth.text.toString()))
        }
        b.firstMet.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) b.firstMet.setText(UiTools.validateDateTime(b.firstMet.text.toString()))
        }

        // should send birthday notifications?
        val needsNtfPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    c, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        if (needsNtfPerm && crush?.notifyBirth() == true) reqNotificationPerm(c)
        b.notifyBirth.setOnCheckedChangeListener { _, isChecked ->
            if (!needsNtfPerm && isChecked) reqNotificationPerm(c)
            b.notifyBirth.alpha = if (isChecked) 1f else DISABLED_ALPHA
        } // changing isChecked programmatically won't invoke the listener!
        b.notifyBirth.alpha = if (b.notifyBirth.isChecked) 1f else DISABLED_ALPHA

        isCancelable = true
        cancellability.start()
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.identify)
            setView(b.root)
            setPositiveButton(R.string.save) { _, _ ->

                // insert input data in a Crush object
                val inserted = Crush(
                    b.key.text.toString().ifBlank { crushKey },

                    // full name
                    b.fName.text.toString().ifBlank { null },
                    b.mName.text.toString().ifBlank { null },
                    b.lName.text.toString().ifBlank { null },

                    // gender
                    b.gender.selectedItemPosition.toByte() or
                            (if (b.fiction.isChecked) Crush.STAT_FICTION else 0) or
                            (if (b.notifyBirth.isChecked) Crush.STAT_NOTIFY_BIRTH else 0) or
                            (if (b.unsafe.isChecked) Crush.STAT_UNSAFE_PERSON else 0) or
                            (crush?.let { it.status and Crush.STAT_INACTIVE } ?: 0),

                    // birthday
                    UiTools.compressDateTime(UiTools.validateDateTime(b.birth.text.toString())),

                    // height
                    if (b.height.text.toString() != "")
                        b.height.text.toString().toFloat() else -1f,

                    // body characteristics
                    (b.bodySkinColour.selectedItemPosition shl Crush.BODY_SKIN_COLOUR.second) or
                            (b.bodyHairColour.selectedItemPosition shl Crush.BODY_HAIR_COLOUR.second) or
                            (b.bodyEyeColour.selectedItemPosition shl Crush.BODY_EYE_COLOUR.second) or
                            (b.bodyEyeShape.selectedItemPosition shl Crush.BODY_EYE_SHAPE.second) or
                            (b.bodyFaceShape.selectedItemPosition shl Crush.BODY_FACE_SHAPE.second) or
                            (b.bodyFat.selectedItemPosition shl Crush.BODY_FAT.second) or
                            (b.bodyMuscle.selectedItemPosition shl Crush.BODY_MUSCLE.second) or
                            (b.bodyBreasts.selectedItemPosition shl Crush.BODY_BREASTS.second) or
                            (b.bodyPenis.selectedItemPosition shl Crush.BODY_PENIS.second) or
                            (b.bodySexuality.selectedItemPosition shl Crush.BODY_SEXUALITY.second),

                    // addresses & special dates
                    b.address.text.toString().ifBlank { null },
                    UiTools.compressDateTime(UiTools.validateDateTime(b.firstMet.text.toString())),
                    b.instagram.text.toString().ifBlank { null },
                )

                if (inserted.key in c.c.people) {
                    Toast.makeText(c, R.string.duplicateCrush, Toast.LENGTH_LONG).show()
                    return@setPositiveButton  // TODO DO NOT CLOSE THE DIALOG
                }

                CoroutineScope(Dispatchers.IO).launch {

                    // if the Crush key is changed...
                    if (inserted.key != crushKey) {
                        c.c.dao.cUpdateKey(crushKey, inserted.key)
                        c.c.people.remove(crushKey)
                        c.c.liefde.indexOf(crushKey).also { pos ->
                            if (pos == -1) return@also
                            c.c.liefde[pos] = inserted.key
                        }
                        if (crushKey in c.c.unsafe) {
                            c.c.unsafe.remove(crushKey)
                            c.c.unsafe.add(inserted.key)
                        }
                        if (c is People) (c as People).mm.visPeople.indexOf(crushKey).also { pos ->
                            if (pos == -1) return@also
                            (c as People).mm.visPeople[pos] = inserted.key
                        }
                        if (c is Settings) (c as Settings).mm.bNtfCrushes.indexOf(crushKey)
                            .also { pos ->
                                if (pos == -1) return@also
                                (c as Settings).mm.bNtfCrushes[pos] = inserted.key
                            }
                    }

                    // insert the Crush object
                    if (crush == null) c.c.dao.cInsert(inserted)
                    else c.c.dao.cUpdate(inserted)
                    c.c.people[inserted.key] = inserted

                    // handle the UI
                    withContext(Dispatchers.Main) {
                        c.c.onCrushChanged(c, inserted.key, if (crush == null) 0 else 1)
                        if (c is Singular && inserted.key != crushKey) c.finish()
                    }
                }
                c.shake()
            }
            setNegativeButton(R.string.discard, null)
            setNeutralButton(R.string.clear) { ad1, _ ->
                if (crush == null) return@setNeutralButton
                MaterialAlertDialogBuilder(c).apply {
                    setTitle(c.getString(R.string.crushClear, crush!!.key))
                    setMessage(R.string.crushClearSure)
                    setPositiveButton(R.string.yes) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            c.c.dao.cDelete(crush!!)
                            c.c.people.remove(crush!!.key)
                            withContext(Dispatchers.Main) {
                                c.c.onCrushChanged(c, crush!!.key, 2)
                            }
                        }
                        c.shake()
                        ad1.dismiss()
                    }
                    setNegativeButton(R.string.no, null)
                }.show()
                c.shake()
            }
            setOnDismissListener { cancellability.cancel() }
        }.create()
    }

    @RequiresApi(33)
    private fun reqNotificationPerm(c: BaseActivity) {
        ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
    }

    private fun onFictionChanged(bb: Boolean) {
        //b.unsafe.isVisible = !bb
        b.notifyBirth.isVisible = !bb
        b.instagramIL.isVisible = !bb
        b.addressIL.hint = if (bb) getString(R.string.creator) else getString(R.string.address)
        b.birthIL.hint = if (bb) getString(R.string.creationDate) else getString(R.string.birth)
    }

    private fun prepareBodyAttrSpinner(spinner: Spinner, @ArrayRes arr: Int) {
        spinner.adapter = ArrayAdapter(
            c, R.layout.spinner_white_small, c.resources.getStringArray(arr)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        spinner.setOnTouchListener(SpinnerTouchListener())
    }
}
