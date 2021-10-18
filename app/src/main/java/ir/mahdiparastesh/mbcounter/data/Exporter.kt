package ir.mahdiparastesh.mbcounter.data

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ir.mahdiparastesh.mbcounter.Fun.Companion.c
import ir.mahdiparastesh.mbcounter.Main
import com.mahdiparastesh.mbcounter.R
import java.io.*

class Exporter(that: AppCompatActivity) {
    var onani: ArrayList<Report>? = null
    private var exportLauncher: ActivityResultLauncher<Intent> =
        that.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val bExp = try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                    FileOutputStream(des.fileDescriptor).use { fos ->
                        fos.write(Gson().toJson(onani).toByteArray())
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
            var imported: List<Report>
            try {
                imported = Gson().fromJson(data, Array<Report>::class.java).toList()
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            Work(c, Main.handler, Work.REPLACE_ALL, imported).start()
        }

    companion object {
        val mime = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"
    }

    fun export(onani: ArrayList<Report>?): Boolean {
        this.onani = onani
        if (onani == null) {
            Toast.makeText(c, R.string.noRecords, Toast.LENGTH_LONG).show(); return true; }
        exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, "exported.json")
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
}
