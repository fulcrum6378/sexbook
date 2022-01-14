package ir.mahdiparastesh.sexbook.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.more.BaseActivity
import java.io.*

class Exporter(val c: BaseActivity) {
    var exported: Exported? = null

    private var exportLauncher: ActivityResultLauncher<Intent> =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            import(c, it.data!!.data!!)
        }

    companion object {
        val mime =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
            else "application/json"

        fun import(c: BaseActivity, uri: Uri, makeSure: Boolean = false) {
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
            if (!makeSure) replace(c, imported)
            else AlertDialog.Builder(c).apply {
                setTitle(c.resources.getString(R.string.momImport))
                setMessage(c.resources.getString(R.string.askImport))
                setPositiveButton(R.string.yes) { _, _ -> replace(c, imported) }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.create().apply {
                show()
                c.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                c.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
            }
        }

        fun replace(c: Context, imported: Exported) {
            Work(c, Work.REPLACE_ALL, imported.reports?.toList()).start()
            Work(c, Work.C_REPLACE_ALL, imported.crushes?.toList()).start()
        }
    }

    fun launchExport(): Boolean {
        exported = Exported(
            c.m.onani.value?.toTypedArray(),
            c.m.liefde.value?.toTypedArray(),
            c.m.places.value?.toTypedArray(),
            c.m.guesses.value?.toTypedArray()
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
