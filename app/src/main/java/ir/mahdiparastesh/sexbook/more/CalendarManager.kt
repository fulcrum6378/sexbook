package ir.mahdiparastesh.sexbook.more

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.util.GregorianCalendar
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

@Suppress("RedundantSuspendModifier")
class CalendarManager(private val c: BaseActivity, private var crushes: Iterable<Crush>?) {
    var id = -1L
    private val accName = "sexbook"
    private val accType = CalendarContract.ACCOUNT_TYPE_LOCAL
    private val tz = "GMT"
    private lateinit var index: HashMap<String/*CRUSH_KEY*/, Long/*EVENT_ID*/>
    private val DEBUG = true

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
        if (DEBUG) {
            deleteEvents()
            c.contentResolver.delete(CCC.CONTENT_URI, "account_name = ?", arrayOf(accName))
            id = -1L
        }
        if (id == -1L) ContentValues().apply {
            put(CCC.ACCOUNT_NAME, accName)
            put(CCC.ACCOUNT_TYPE, accType)
            put(CCC.NAME, "Sexbook")
            put(CCC.CALENDAR_DISPLAY_NAME, c.getString(R.string.app_name))
            put(CCC.CALENDAR_COLOR, c.color(R.color.CP))
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
        } else {
            if (!Index().read()) {
                deleteEvents()
                insertEvents(crushes!!)
            }
            /*if () ContentValues().apply {
            //put(CCC.ALLOWED_REMINDERS, "") // 0,1,2
            //put(CCC.ALLOWED_AVAILABILITY, "") // 0,1,2
            //put(CCC.ALLOWED_ATTENDEE_TYPES, "") // 0,1
            c.contentResolver.update(CCC.CONTENT_URI, this, "account_name = ?", arrayOf(accName))
        }*/
        }
        crushes = null
    }

    private suspend fun insertEvents(crushes: Iterable<Crush>) {
        index = hashMapOf()
        for (cr in crushes) if (cr.hasFullBirth()) {
            cr.insertEvent()
            //if (cr.notifyBirth) cr.insertReminder()
        }
        Index().write()
    }

    private suspend fun deleteEvents() {
        /*val existingIds = arrayListOf<Long>()
        c.contentResolver.query(
            CCE.CONTENT_URI, arrayOf(CCE._ID), "calendar_id = ?", arrayOf("$id"), CCE._ID
        )?.use {
            if (it.moveToFirst()) for (i in 0 until it.count) {
                it.getLongOrNull(it.getColumnIndex(CCE._ID))?.also { l -> existingIds.add(l) }
                it.moveToNext()
            }
        }
        if (existingIds.isEmpty()) return
        for (ev in existingIds)
            c.contentResolver.delete(CCR.CONTENT_URI, "event_id = ?", arrayOf(ev.toString()))*/
        // CCR inherits "calendar_id" but the Reminders table actually has no such column!
        c.contentResolver.delete(CCE.CONTENT_URI, "calendar_id = ?", arrayOf(id.toString()))
    }

    fun replaceEvents(crushes: Iterable<Crush>) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteEvents()
            insertEvents(crushes)
        }
    }

    private fun Uri.getId() =
        toString().substringAfterLast("/").substringBefore("?").toLong()

    /** Write the index after executing this function. */
    private fun Crush.insertEvent() {
        ContentValues().apply {
            put(CCE.CALENDAR_ID, id)
            put(CCE.TITLE, c.getString(R.string.sBirthday, visName()))
            put(CCE.DTSTART, GregorianCalendar(TimeZone.getTimeZone(tz)).let {
                it.set(bYear.toInt(), bMonth.toInt(), bDay.toInt()); it.timeInMillis
            })
            put(CCE.RRULE, "FREQ=YEARLY")
            put(CCE.DURATION, "P1D")
            put(CCE.ALL_DAY, 1)
            put(CCE.EVENT_TIMEZONE, tz)
            c.contentResolver.insert(CCE.CONTENT_URI, this)
                ?.also { index[key] = it.getId() }
        }
    }

    // import android.provider.CalendarContract.Reminders as CCR
    /*private fun Crush.insertReminder() {
        ContentValues().apply {
            put(CCR.EVENT_ID, index[key])
            put(CCR.MINUTES, 1440 * 1)
            put(CCR.METHOD, CCR.METHOD_DEFAULT)
            c.contentResolver.insert(CCR.CONTENT_URI, this)
        }
    }*/

    fun updateEvent(oldCrush: Crush?, newCrush: Crush?) {
        when {
            oldCrush == null && newCrush != null -> {
                newCrush.insertEvent()
                Index().write()
                //newCrush.insertReminder()
            }
            oldCrush != null && newCrush != null -> {
                val ev = arrayOf(index[oldCrush.key].toString())
                ContentValues().apply {
                    if (oldCrush.visName() != newCrush.visName())
                        put(CCE.TITLE, c.getString(R.string.sBirthday, newCrush.visName()))
                    if (oldCrush.bYear != newCrush.bYear ||
                        oldCrush.bMonth != newCrush.bMonth ||
                        oldCrush.bDay != newCrush.bDay
                    ) put(CCE.DTSTART, GregorianCalendar(TimeZone.getTimeZone(tz)).let {
                        it.set(
                            newCrush.bYear.toInt(),
                            newCrush.bMonth.toInt(),
                            newCrush.bDay.toInt()
                        ); it.timeInMillis
                    })
                    if (size() > 0)
                        c.contentResolver.update(CCE.CONTENT_URI, this, "_id = ?", ev)
                }
                /*when {
                    !oldCrush.notifyBirth && newCrush.notifyBirth -> newCrush.insertReminder()
                    oldCrush.notifyBirth && !newCrush.notifyBirth ->
                        c.contentResolver.delete(CCR.CONTENT_URI, "event_id = ?", ev)
                }*/
            }
            oldCrush != null && newCrush == null -> {
                val ev = arrayOf(index[oldCrush.key].toString())
                //c.contentResolver.delete(CCR.CONTENT_URI, "event_id = ?", ev)
                c.contentResolver.delete(CCE.CONTENT_URI, "_id = ?", ev)
                index.remove(oldCrush.key)
                Index().write()
            }
            else -> throw IllegalArgumentException("At least one of the arguments must not be null.")
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
            c, Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        fun askPerm(c: BaseActivity) {
            ActivityCompat.requestPermissions(
                c, arrayOf(Manifest.permission.WRITE_CALENDAR), reqCode
            )
        }
    }
}
