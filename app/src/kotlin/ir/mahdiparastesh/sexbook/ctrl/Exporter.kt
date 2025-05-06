package ir.mahdiparastesh.sexbook.ctrl

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import ir.mahdiparastesh.sexbook.BuildConfig
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Handles exporting and importing the contents of Sexbook.
 * An instance of this class must be injected as a dependency of the [Main] Activity.
 */
class Exporter(private val c: BaseActivity) {

    /** Date model of the JSON file being exported */
    private var exported: Exported? = null

    /** Name of the file being exported */
    private val EXPORT_NAME = "sexbook.json"

    /** MIME type of a JSON file */
    private val mime =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"

    /**
     * Used by [launchExport]
     * This field must be initialised as soon as [Main] is instantiated.
     */
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
                exported = null

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        c, if (bExp) R.string.exportDone else R.string.exportUndone,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    /**
     * Used by [launchImport]
     * This field must be initialised as soon as [Main] is instantiated.
     */
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

    /** Launches a file manager in order to choose a place in the storage for exporting. */
    fun launchExport() {
        if (!export()) return
        exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, EXPORT_NAME)
        })
    }

    /** Launches a file manager in order to pick a file for importing. */
    fun launchImport(): Boolean {
        importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
        })
        return true
    }

    /**
     * Exports the entire [Database] plus [SharedPreferences] into a cache JSON file,
     * then shares that cache file as a [FileProvider].
     */
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
            exported = null
        }
    }

    /** Exports the entire [Database] plus [SharedPreferences] into a JSON file. */
    private fun export(): Boolean {
        exported = Exported(
            c.c.reports.filter<Report> { !it.guess }.sortedBy { it.time }.toTypedArray(),
            c.c.people.values.sortedBy { it.key }.sortedBy { it.getFirstOrgasm(c.c) }
                .toTypedArray(),
            c.c.places.sortedBy { it.name }.toTypedArray(),
            c.c.guesses.sortedBy { it.crsh }.sortedWith(Guess.Sort()).toTypedArray(),
            c.c.sp.all.toSortedMap()
        )
        val emp = exported!!.isEmpty()
        if (emp) Toast.makeText(c, R.string.noRecords, Toast.LENGTH_LONG).show()
        return !emp
    }

    /**
     * Imports data from a JSON file in order to be replaced with
     * the contents of [Database] and [SharedPreferences].
     */
    fun import(c: BaseActivity, uri: Uri) {
        var data: String? = null
        try {
            c.contentResolver.openFileDescriptor(uri, "r")?.use { des ->
                data = FileInputStream(des.fileDescriptor).use { it.readBytes() }
                    .toString(Charsets.UTF_8)
            }
            data!!
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) throw e
            Toast.makeText(
                c, R.string.importOpenError, Toast.LENGTH_LONG
            ).show()
            return
        }
        val imported: Exported
        try {
            imported = GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create()
                .fromJson(data, Exported::class.java)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) throw e
            Toast.makeText(
                c, R.string.importReadError, Toast.LENGTH_LONG
            ).show()
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

    /**
     * Replaces contents of an [Exported] with contents of the [Database] and [SharedPreferences].
     */
    private fun replace(c: BaseActivity, imported: Exported) {
        CoroutineScope(Dispatchers.IO).launch {
            var id = 1L

            c.c.dao.rDeleteAll()
            imported.reports?.toList()
                ?.sortedBy { it.time }
                ?.onEach { it.id = id++ }
                ?.also { c.c.dao.rReplaceAll(it) }

            c.c.dao.cDeleteAll()
            imported.crushes?.toList()
                ?.sortedBy { it.key }
                ?.also { c.c.dao.cReplaceAll(it) }

            c.c.dao.pDeleteAll()
            imported.places?.toList()
                ?.sortedBy { it.name }
                ?.also { c.c.dao.pReplaceAll(it) }

            id = 1L
            c.c.dao.gDeleteAll()
            imported.guesses?.toList()
                ?.sortedBy { it.crsh }?.sortedWith(Guess.Sort())
                ?.onEach { it.id = id++ }
                ?.also { c.c.dao.gReplaceAll(it) }

            if (imported.settings != null) c.c.sp.edit().apply {
                clear()
                imported.settings.forEach { (k, v) ->
                    when (v) {
                        is Boolean -> putBoolean(k, v)
                        is Double -> when (k) { // all numbers become Double because of Gson
                            Settings.spCalType, Settings.spNotifyBirthDaysBefore,
                            Settings.spPageLoveSortBy ->
                                putInt(k, v.toInt())
                            Settings.spStatSince, Settings.spStatUntil, Settings.spDefPlace,
                            Settings.spLastNotifiedBirthAt ->
                                putLong(k, v.toLong())
                        }
                        is String -> putString(k, v)
                    }
                }
            }.apply()

            LastOrgasm.updateAll(c.c)
            withContext(Dispatchers.Main) {
                Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                if (c is Main) c.onDataChanged()
                else Main.changed = true
            }
        }
    }

    /** Date model of the JSON file being exported */
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
