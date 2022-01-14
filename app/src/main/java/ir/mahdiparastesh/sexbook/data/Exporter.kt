package ir.mahdiparastesh.sexbook.data

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import java.io.*

class Exporter(that: AppCompatActivity) {
    var exported: Exported? = null

    private var exportLauncher: ActivityResultLauncher<Intent> =
        that.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val bExp = try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                    FileOutputStream(des.fileDescriptor).use { fos ->
                        fos.write(Gson().toJson(exported).toByteArray())
                    }
                }
                true
            } catch (ignored: Exception) {
                false
            }
            Toast.makeText(
                c, if (bExp) R.string.exportDone else R.string.exportUndone, Toast.LENGTH_LONG
            ).show()
        }

    private var importLauncher: ActivityResultLauncher<Intent> =
        that.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            import(it.data!!.data!!)
        }

    companion object {
        val mime =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
            else "application/json"

        fun import(uri: Uri, makeSure: Boolean = false, that: AppCompatActivity? = null) {
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(uri, "r")?.use { des ->
                    val sb = StringBuffer()
                    FileInputStream(des.fileDescriptor).apply {
                        var i: Int
                        while (read().also { r -> i = r } != -1) sb.append(i.toChar())
                        close()
                    }
                    data = sb.toString()
                }
                data!!
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importOpenError, Toast.LENGTH_LONG).show()
                return
            }
            var imported: Exported
            try {
                imported = Gson().fromJson(data, Exported::class.java)
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
                return
            }
            if (!makeSure) replace(imported)
            else AlertDialog.Builder(that!!).apply {
                setTitle(that.resources.getString(R.string.momImport))
                setMessage(that.resources.getString(R.string.askImport))
                setPositiveButton(R.string.yes) { _, _ -> replace(imported) }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                Fun.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
            }
        }

        fun replace(imported: Exported) {
            Work(Work.REPLACE_ALL, imported.reports?.toList()).start()
            Work(Work.C_REPLACE_ALL, imported.crushes?.toList()).start()
        }
    }

    fun launchExport(m: Model): Boolean {
        exported = Exported(
            m.onani.value?.toTypedArray(),
            m.liefde.value?.toTypedArray(),
            m.places.value?.toTypedArray(),
            m.guesses.value?.toTypedArray()
        )
        if (exported!!.isEmpty()) {
            Toast.makeText(c, R.string.noRecords, Toast.LENGTH_LONG).show(); return true; }
        exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, "sexbook.json")
        })
        return true
    }

    fun launchImport(): Boolean {
        importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
        })
        return true
    }

    class Exported(
        val reports: Array<Report>?,
        val crushes: Array<Crush>?,
        val places: Array<Place>?,
        val guesses: Array<Guess>?
    ) {
        fun isEmpty() = reports.isNullOrEmpty()
    }
}
