package ir.mahdiparastesh.sexbook.data

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.more.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Exporter(val c: BaseActivity) {
    private var exported: Exported? = null
    private val EXPORT_NAME = "sexbook.json"
    private val mime =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"

    private var exportLauncher: ActivityResultLauncher<Intent> =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            CoroutineScope(Dispatchers.IO).launch {
                var bExp = false
                runCatching {
                    c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                        FileOutputStream(des.fileDescriptor).use { fos ->
                            fos.write(exported!!.binary())
                        }
                    }
                }.onSuccess { bExp = true }
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        c, if (bExp) R.string.exportDone else R.string.exportUndone,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private var importLauncher: ActivityResultLauncher<Intent> =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            import(c, it.data!!.data!!)
        }

    fun launchExport() {
        if (!export()) return
        exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, EXPORT_NAME)
        })
    }

    fun launchImport(): Boolean {
        importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
        })
        return true
    }

    fun send() {
        if (!export()) return
        val cache = File(c.cacheDir, EXPORT_NAME)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                FileOutputStream(cache).use { it.write(exported!!.binary()) }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = mime
                        putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(c, "${c.packageName}.send", cache)
                        )
                    }.also { c.startActivity(it) }
                }
            }
        }
    }

    private fun export(): Boolean {
        exported = Exported(
            c.m.onani.value?.filter { !it.guess }?.toTypedArray(),
            c.m.liefde.value?.toTypedArray(),
            c.m.places.value?.toTypedArray(),
            c.m.guesses.value?.toTypedArray(),
            c.sp.all
        )
        val emp = exported!!.isEmpty()
        if (emp) Toast.makeText(c, R.string.noRecords, Toast.LENGTH_LONG).show()
        return !emp
    }

    fun import(c: BaseActivity, uri: Uri) {
        var data: String? = null
        try {
            c.contentResolver.openFileDescriptor(uri, "r")?.use { des ->
                data = FileInputStream(des.fileDescriptor).use { it.readBytes() }
                    .toString(Charsets.UTF_8)
            }
            data!!
        } catch (e: Exception) {
            Toast.makeText(c, R.string.importOpenError, Toast.LENGTH_LONG).show()
            return
        }
        val imported: Exported
        try {
            imported = Gson().fromJson(data, Exported::class.java)
        } catch (e: Exception) {
            Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
            return
        }
        MaterialAlertDialogBuilder(c).apply {
            setTitle(c.resources.getString(R.string.momImport))
            setMessage(c.resources.getString(R.string.askImport))
            setPositiveButton(R.string.yes) { _, _ -> replace(c, imported) }
            setNegativeButton(R.string.no, null)
            setCancelable(true)
        }.show()
    }

    private fun replace(c: BaseActivity, imported: Exported) {
        Work(c, Work.REPLACE_ALL, imported.reports?.toList()).start()
        Work(c, Work.C_REPLACE_ALL, imported.crushes?.toList()).start()
        Work(c, Work.P_REPLACE_ALL, imported.places?.toList()).start()
        Work(c, Work.G_REPLACE_ALL, imported.guesses?.toList()).start()
        if (imported.settings != null) c.sp.edit().apply {
            imported.settings.forEach { (k, v) ->
                when (v) {
                    is Boolean -> putBoolean(k, v)
                    is Double -> // all numbers become Double in SP.
                        when (k) {
                            Settings.spDefPlace, Settings.spStatSince,
                            Settings.spStatUntil, Settings.spPageLoveSortBy,
                            Settings.spLastNotifiedBirthAt ->
                                putLong(k, v.toLong())
                            Settings.spCalType, Settings.spNotifyBirthDaysBefore,
                            Settings.spPrefersOrgType ->
                                putInt(k, v.toInt())
                        }
                    is String -> putString(k, v)
                }
            }
        }.apply()
        c.m.resetData()
    }

    class Exported(
        val reports: Array<Report>?,
        val crushes: Array<Crush>?,
        val places: Array<Place>?,
        val guesses: Array<Guess>?,
        val settings: Map<String, *>? = null
    ) {
        fun isEmpty() = reports.isNullOrEmpty()

        fun binary() = Gson().toJson(this).toByteArray()
    }
}
