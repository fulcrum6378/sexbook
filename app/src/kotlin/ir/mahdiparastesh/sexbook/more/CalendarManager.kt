package ir.mahdiparastesh.sexbook.more

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import androidx.core.database.getLongOrNull
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.CalendarContract.Calendars as CCC
import android.provider.CalendarContract.Events as CCE

/** API for maintaining Sexbook data in the system calendar. */
@Suppress("RedundantSuspendModifier")
class CalendarManager(private val c: BaseActivity, private var crushes: Iterable<String>?) {
    var id = -1L
    private val accName = "sexbook"
    private val accType = CalendarContract.ACCOUNT_TYPE_LOCAL
    private val tz = "GMT"
    private lateinit var index: HashMap<String/*CRUSH_KEY*/, Long/*EVENT_ID*/>

    fun initialize(): CalendarManager {
        CoroutineScope(Dispatchers.IO).launch { initialise() }
        return this
    }

    suspend fun initialise(): CalendarManager {
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
        }
        crushes = null
        return this
    }

    private suspend fun insertEvents(crushes: Iterable<String>) {
        index = hashMapOf()
        for (cr in crushes) insertEvent(cr)
    }

    private suspend fun deleteEvents() {
        c.contentResolver.delete(CCE.CONTENT_URI, "calendar_id = ?", arrayOf(id.toString()))
    }

    private suspend fun deleteCalendar() {
        c.contentResolver.delete(CCC.CONTENT_URI, "account_name = ?", arrayOf(accName))
    }

    suspend fun replaceEvents(crushes: Iterable<String>?) {
        deleteEvents()
        if (crushes != null) insertEvents(crushes)
    }

    private fun Uri.getId() =
        toString().substringAfterLast("/").substringBefore("?").toLong()

    /** Don't forget to write() the Index after executing this function. */
    private fun insertEvent(crush: String) {
        val cr = c.m.people[crush] ?: return
        val cal = cr.bCalendar(tz = TimeZone.getTimeZone(tz)) ?: return
        ContentValues().apply {
            put(CCE.CALENDAR_ID, id)
            put(CCE.TITLE, c.getString(R.string.sBirthday, cr.visName()))
            put(CCE.DTSTART, cal.timeInMillis)
            put(CCE.RRULE, "FREQ=YEARLY")
            put(CCE.DURATION, "P1D")
            put(CCE.ALL_DAY, 1)
            put(CCE.EVENT_TIMEZONE, tz)
            c.contentResolver.insert(CCE.CONTENT_URI, this)
                ?.also { index[crush] = it.getId() }
        }
    }

    fun terminate() {
        CoroutineScope(Dispatchers.IO).launch {
            deleteEvents()
            deleteCalendar()
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
