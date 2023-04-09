package ir.mahdiparastesh.sexbook.more

import android.content.ContentValues
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.database.getLongOrNull
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import android.provider.CalendarContract.Calendars as CCC
import android.provider.CalendarContract.Events as CCE

class CalendarManager(private val c: BaseActivity) {
    private val accName = "sexbook"
    private val accType = CalendarContract.ACCOUNT_TYPE_LOCAL
    private val tz = "GMT"
    var id = -1L

    init {
        //c.contentResolver.delete(CCC.CONTENT_URI, "account_name = ?", arrayOf(accName))
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
            // FIXME insertEvents()
        }/* else if () ContentValues().apply {
            //put(CCC.ALLOWED_REMINDERS, "") // 0,1,2
            //put(CCC.ALLOWED_AVAILABILITY, "") // 0,1,2
            //put(CCC.ALLOWED_ATTENDEE_TYPES, "") // 0,1
            c.contentResolver.update(CCC.CONTENT_URI, this, "account_name = ?", arrayOf(accName))
        }*/
    }

    fun insertEvents(crushes: Iterable<Crush>) {
        for (cr in crushes) if (cr.hasFullBirth()) ContentValues().apply {
            put(CCE.CALENDAR_ID, id)
            put(CCE.TITLE, c.getString(R.string.sBirthday, cr.visName()))
            put(CCE.DTSTART, GregorianCalendar(TimeZone.getTimeZone(tz)).let {
                it.set(cr.bYear.toInt(), cr.bMonth.toInt(), cr.bDay.toInt()); it.timeInMillis
            })
            put(CCE.RRULE, "FREQ=YEARLY")
            put(CCE.DURATION, "P1D")
            put(CCE.ALL_DAY, 1)
            put(CCE.EVENT_TIMEZONE, tz)
            c.contentResolver.insert(CCE.CONTENT_URI, this)
        }
    }

    fun deleteEvents() {
        c.contentResolver.delete(CCE.CONTENT_URI, "calendar_id = ?", arrayOf(id.toString()))
    }

    private fun Uri.getId() =
        toString().substringAfterLast("/").substringBefore("?").toLong()
}
