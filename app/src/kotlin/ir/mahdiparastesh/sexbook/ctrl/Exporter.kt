package ir.mahdiparastesh.sexbook.ctrl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import ir.mahdiparastesh.sexbook.BuildConfig
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.page.Main
import ir.mahdiparastesh.sexbook.page.Settings
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
 * It can also be instantiated inside a BroadcastReceiver like [NotificationActions].
 */
class Exporter {

    private val c: Context
    private var activity: BaseActivity? = null
    private var sexbook: Sexbook? = null

    constructor(activity: BaseActivity) {
        this.activity = activity
        c = activity

        exportLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            CoroutineScope(Dispatchers.IO).launch {
                var bExp = false
                runCatching {
                    c.contentResolver.openFileDescriptor(
                        it.data!!.data!!, "w"
                    )?.use { des ->
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

        importLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            import(it.data!!.data!!)
        }
    }

    constructor(sexbook: Sexbook) {
        this.sexbook = sexbook
        c = sexbook
    }

    /** Date model of the JSON file being exported */
    private var exported: Exported? = null

    /** Name of the file being exported */
    val exportName = "sexbook.json"

    /** MIME type of a JSON file */
    private val mime =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
        else "application/json"

    /**
     * Used by [launchExport]
     * This field must be initialised as soon as [Main] is instantiated.
     */
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>

    /**
     * Used by [launchImport]
     * This field must be initialised as soon as [Main] is instantiated.
     */
    private lateinit var importLauncher: ActivityResultLauncher<Intent>

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
    @MainThread
    fun launchExport() {
        if (!export(true)) return
        exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, exportName)
        })
    }

    /** Launches a file manager in order to pick a file for importing. */
    @MainThread
    fun launchImport(): Boolean {
        importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
        })
        return true
    }

    /**
     * Shares a cache file containing the exported data via [FileProvider].
     */
    @MainThread
    fun send() {
        cache(toastOnError = true, resumeOnMainThread = true) { cache ->
            c.startActivity(
                Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            c, "${c.packageName}.send", cache
                        )
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
    }

    /** Exports the entire [Database] plus [SharedPreferences] into a cache JSON file. */
    @MainThread
    fun cache(toastOnError: Boolean, resumeOnMainThread: Boolean, then: suspend (File) -> Unit) {
        if (!export(toastOnError)) return
        val cache = File(c.cacheDir, exportName)
        CoroutineScope(Dispatchers.IO).launch {
            FileOutputStream(cache).use { it.write(exported!!.binary()) }
            exported = null
            if (resumeOnMainThread)
                withContext(Dispatchers.Main) { then(cache) }
            else
                then(cache)
        }
    }

    /** Exports the entire [Database] plus [SharedPreferences]. */
    @MainThread
    fun export(toastOnError: Boolean): Boolean {
        val c = sexbook ?: activity!!.c
        exported = Exported(
            c.reports.filter { !it.guess }.sortedBy { it.time }.toTypedArray(),
            c.people.values.sortedBy { it.key }.sortedBy { it.getFirstOrgasm(c) }
                .toTypedArray(),
            c.places.sortedBy { it.name }.toTypedArray(),
            c.guesses.sortedBy { it.name }.sortedWith(Guess.Sort()).toTypedArray(),
            c.sp.all.toSortedMap()
        )
        val emp = exported!!.isEmpty()
        if (emp && toastOnError) Toast.makeText(
            c, R.string.noRecords, Toast.LENGTH_LONG
        ).show()
        return !emp
    }

    /**
     * Imports data from a JSON file in order to be replaced with
     * the contents of [Database] and [SharedPreferences].
     */
    fun import(uri: Uri) {
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
                c, R.string.importOpenError,
                Toast.LENGTH_LONG
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
                c, R.string.importReadError,
                Toast.LENGTH_LONG
            ).show()
            return
        }
        MaterialAlertDialogBuilder(activity!!).apply {
            setTitle(c.resources.getString(R.string.dataImport))
            setMessage(c.resources.getString(R.string.askImport))
            setPositiveButton(R.string.yes) { _, _ -> replace(imported) }
            setNegativeButton(R.string.no, null)
            setCancelable(true)
        }.show()
    }

    /**
     * Replaces contents of an [Exported] with contents of the [Database] and [SharedPreferences].
     */
    private fun replace(imported: Exported) {
        val c = sexbook ?: activity!!.c
        CoroutineScope(Dispatchers.IO).launch {
            var id = 1L

            c.dao.rDeleteAll()
            imported.reports?.toList()
                ?.sortedBy { it.time }
                ?.onEach { it.id = id++ }
                ?.also { c.dao.rReplaceAll(it) }

            c.dao.cDeleteAll()
            imported.crushes?.toList()
                ?.sortedBy { it.key }
                ?.also { c.dao.cReplaceAll(it) }

            c.dao.pDeleteAll()
            imported.places?.toList()
                ?.sortedBy { it.name }
                ?.also { c.dao.pReplaceAll(it) }

            id = 1L
            c.dao.gDeleteAll()
            imported.guesses?.toList()
                ?.sortedBy { it.name }?.sortedWith(Guess.Sort())
                ?.onEach { it.id = id++ }
                ?.also { c.dao.gReplaceAll(it) }

            if (imported.settings != null) c.sp.edit().apply {
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

            LastOrgasm.updateAll(c)
            withContext(Dispatchers.Main) {
                Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                if (activity is Main) (activity as Main).onDataChanged()
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
