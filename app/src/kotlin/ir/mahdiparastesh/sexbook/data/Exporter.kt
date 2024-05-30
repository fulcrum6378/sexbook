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
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.more.LastOrgasm
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

    private val exportLauncher: ActivityResultLauncher<Intent> =
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

    private val importLauncher: ActivityResultLauncher<Intent> =
        c.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            import(c, it.data!!.data!!)
        }

    private val typeAdapterFactory = object : TypeAdapterFactory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? =
            when (type?.rawType) {
                Report::class.java -> Report.GsonAdapter() as TypeAdapter<T>
                Crush::class.java -> Crush.GsonAdapter() as TypeAdapter<T>
                Place::class.java -> Place.GsonAdapter() as TypeAdapter<T>
                Guess::class.java -> Guess.GsonAdapter() as TypeAdapter<T>
                else -> null
            }
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
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }.also { c.startActivity(it) }
                }
            }
        }
    }

    private fun export(): Boolean {
        exported = Exported(
            c.m.reports.values.filter { !it.guess }.sortedBy { it.time }.toTypedArray(),
            c.m.people.values.sortedBy { it.key }.sortedBy { it.getFirstOrgasm(c.m) }
                .toTypedArray(),
            c.m.places.sortedBy { it.name }.toTypedArray(),
            c.m.guesses.sortedBy { it.crsh }.sortedWith(Guess.Sort()).toTypedArray(),
            c.sp.all.toSortedMap()
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
            imported = GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create()
                .fromJson(data, Exported::class.java)
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
        CoroutineScope(Dispatchers.IO).launch {
            c.m.dao.rDeleteAll()
            imported.reports?.toList()?.also { c.m.dao.rReplaceAll(it) }
            c.m.dao.cDeleteAll()
            imported.crushes?.toList()?.also { c.m.dao.cReplaceAll(it) }
            c.m.dao.pDeleteAll()
            imported.places?.toList()?.also { c.m.dao.pReplaceAll(it) }
            c.m.dao.gDeleteAll()
            imported.guesses?.toList()?.also { c.m.dao.gReplaceAll(it) }

            if (imported.settings != null) c.sp.edit().apply {
                imported.settings.forEach { (k, v) ->
                    when (v) {
                        is Boolean -> putBoolean(k, v)
                        is Double -> when (k) { // all numbers become Double in SP.
                            Settings.spCalType, Settings.spNotifyBirthDaysBefore,
                            Settings.spPageLoveSortBy, Settings.spPrefersOrgType ->
                                putInt(k, v.toInt())
                            Settings.spStatSince, Settings.spStatUntil, Settings.spDefPlace,
                            Settings.spLastNotifiedBirthAt ->
                                putLong(k, v.toLong())
                        }
                        is String -> putString(k, v)
                    }
                }
            }.apply()
            LastOrgasm.updateAll(c)

            withContext(Dispatchers.Main) {
                Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                if (c is Main) c.onDataChanged()
                else Main.changed = true
            }
        }
    }

    inner class Exported(
        val reports: Array<Report>?,
        val crushes: Array<Crush>?,
        val places: Array<Place>?,
        val guesses: Array<Guess>?,
        val settings: Map<String, *>? = null
    ) {
        fun isEmpty() = reports.isNullOrEmpty()

        fun binary() = GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create()
            .toJson(this).toByteArray()
    }
}
