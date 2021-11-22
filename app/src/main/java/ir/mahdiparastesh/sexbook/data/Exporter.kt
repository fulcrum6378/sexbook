package ir.mahdiparastesh.sexbook.data

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ir.mahdiparastesh.sexbook.Fun.Companion.c
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
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "r")?.use { des ->
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
                return@registerForActivityResult
            }
            var imported: Exported
            try {
                imported = Gson().fromJson(data, Exported::class.java)
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            Work(Work.REPLACE_ALL, imported.reports?.toList()).start()
            Work(Work.C_REPLACE_ALL, imported.crushes?.toList()).start()
        }

    companion object {
        val mime = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"
    }

    fun export(reports: ArrayList<Report>?, crushes: ArrayList<Crush>?): Boolean {
        exported = Exported(reports?.toTypedArray(), crushes?.toTypedArray())
        if (exported!!.isEmpty()) {
            Toast.makeText(c, R.string.noRecords, Toast.LENGTH_LONG).show(); return true; }
        exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, "sexbook.json")
        })
        return true
    }

    fun import(): Boolean {
        importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
        })
        return true
    }

    data class Exported(val reports: Array<Report>?, val crushes: Array<Crush>?) {
        fun isEmpty() = reports.isNullOrEmpty() || crushes.isNullOrEmpty()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Exported
            if (reports != null) {
                if (other.reports == null) return false
                if (!reports.contentEquals(other.reports)) return false
            } else if (other.reports != null) return false
            if (crushes != null) {
                if (other.crushes == null) return false
                if (!crushes.contentEquals(other.crushes)) return false
            } else if (other.crushes != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = reports?.contentHashCode() ?: 0
            result = 31 * result + (crushes?.contentHashCode() ?: 0)
            return result
        }
    }
}
