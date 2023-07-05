package ir.mahdiparastesh.sexbook.more

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import androidx.core.database.getLongOrNull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.provider.CalendarContract.Calendars as CCC
import android.provider.CalendarContract.Events as CCE

/** API for maintaining Sexbook data in the system calendar. */
@Suppress("RedundantSuspendModifier")
class CalendarManager(private val c: BaseActivity, private var crushes: Iterable<Crush>?) {
    var id = -1L
    private val accName = "sexbook"
    private val accType = CalendarContract.ACCOUNT_TYPE_LOCAL
    private val tz = "GMT"
    private lateinit var index: HashMap<String/*CRUSH_KEY*/, Long/*EVENT_ID*/>

    init {
        CoroutineScope(Dispatchers.IO).launch { initialise() }
    }

    private suspend fun initialise() {
        c.contentResolver.query(
            CCC.CONTENT_URI, arrayOf(CCC.NAME, CCC._ID),
            "account_name = ?", arrayOf(accName), CCC._ID
        )?.use {
            if (!it.moveToFirst()) return@use
            it.getLongOrNull(it.getColumnIndex(CCC._ID))?.also { l -> id = l }
        }
        if (id == -1L) ContentValues().apply {
            put(CCC.ACCOUNT_NAME, accName)
            put(CCC.ACCOUNT_TYPE, accType)
            put(CCC.NAME, "Sexbook")
            put(CCC.CALENDAR_DISPLAY_NAME, c.getString(R.string.app_name))
            put(CCC.CALENDAR_COLOR, c.color(R.color.CP_LIGHT))
            put(CCC.CALENDAR_ACCESS_LEVEL, CCC.CAL_ACCESS_READ)
            put(CCC.SYNC_EVENTS, 0)
            put(CCC.CALENDAR_TIME_ZONE, tz)
            c.contentResolver.insert(
                CCC.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CCC.ACCOUNT_NAME, accName)
                    .appendQueryParameter(CCC.ACCOUNT_TYPE, accType).build(), this
            )?.also { id = it.getId() }
            insertEvents(crushes!!)
        } else if (!Index().read()) {
            deleteEvents()
            insertEvents(crushes!!)
        }
        crushes = null
    }

    private suspend fun insertEvents(crushes: Iterable<Crush>) {
        index = hashMapOf()
        for (cr in crushes) cr.insertEvent()
        Index().write()
    }

    private suspend fun deleteEvents() {
        c.contentResolver.delete(CCE.CONTENT_URI, "calendar_id = ?", arrayOf(id.toString()))
    }

    private suspend fun deleteCalendar() {
        c.contentResolver.delete(CCC.CONTENT_URI, "account_name = ?", arrayOf(accName))
    }

    fun replaceEvents(crushes: Iterable<Crush>) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteEvents()
            insertEvents(crushes)
        }
    }

    private fun Uri.getId() =
        toString().substringAfterLast("/").substringBefore("?").toLong()

    private fun Crush?.containsBirth() = this != null && birth != null

    /** Write the index after executing this function. */
    private fun Crush.insertEvent() {
        val cal = calendar(TimeZone.getTimeZone(tz)) ?: return
        ContentValues().apply {
            put(CCE.CALENDAR_ID, id)
            put(CCE.TITLE, c.getString(R.string.sBirthday, visName()))
            put(CCE.DTSTART, cal.timeInMillis)
            put(CCE.RRULE, "FREQ=YEARLY")
            put(CCE.DURATION, "P1D")
            put(CCE.ALL_DAY, 1)
            put(CCE.EVENT_TIMEZONE, tz)
            c.contentResolver.insert(CCE.CONTENT_URI, this)
                ?.also { index[key] = it.getId() }
        }
    }

    fun updateEvent(oldCrush: Crush?, newCrush: Crush?) {
        when {
            !oldCrush.containsBirth() && newCrush.containsBirth() -> {
                newCrush!!.insertEvent()
                Index().write()
            }
            oldCrush.containsBirth() && newCrush.containsBirth() -> {
                val ev = arrayOf(index[oldCrush!!.key].toString())
                ContentValues().apply {
                    if (oldCrush.visName() != newCrush!!.visName())
                        put(CCE.TITLE, c.getString(R.string.sBirthday, newCrush.visName()))
                    if (oldCrush.birth != newCrush.birth)
                        put(CCE.DTSTART, newCrush.calendar(TimeZone.getTimeZone(tz))!!.timeInMillis)

                    if (size() > 0)
                        c.contentResolver.update(CCE.CONTENT_URI, this, "_id = ?", ev)
                }
            }
            oldCrush.containsBirth() && !newCrush.containsBirth() -> {
                val ev = arrayOf(index[oldCrush!!.key].toString())
                c.contentResolver.delete(CCE.CONTENT_URI, "_id = ?", ev)
                index.remove(oldCrush.key)
                Index().write()
            }
            else -> {}
        }
    }

    fun terminate() {
        CoroutineScope(Dispatchers.IO).launch {
            deleteEvents()
            deleteCalendar()
        }
    }

    inner class Index : File(c.cacheDir, "calendar_index.json") {
        fun read(): Boolean {
            if (!exists()) return false
            index = Gson().fromJson(
                FileInputStream(this).use { it.readBytes() }.toString(Charsets.UTF_8),
                object : TypeToken<HashMap<String, Long>>() {}.type
            )
            return true
        }

        fun write() {
            FileOutputStream(Index()).use { it.write(Gson().toJson(index).encodeToByteArray()) }
        }
    }

    companion object {
        const val reqCode = 1

        fun checkPerm(c: BaseActivity) = ActivityCompat.checkSelfPermission(
            c, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            c, Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        fun askPerm(c: BaseActivity) {
            ActivityCompat.requestPermissions(
                c, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                reqCode
            )
        }
    }
}
