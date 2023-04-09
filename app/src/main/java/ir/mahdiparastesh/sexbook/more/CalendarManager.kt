package ir.mahdiparastesh.sexbook.more

import android.content.ContentValues
import android.provider.CalendarContract
import ir.mahdiparastesh.sexbook.R
import android.provider.CalendarContract.Calendars as CCC

class CalendarManager(private val c: BaseActivity) {
    private val accName = "sexbook"
    private val accType = CalendarContract.ACCOUNT_TYPE_LOCAL

    fun init() {
        var bb = false
        c.contentResolver.query(
            CCC.CONTENT_URI, arrayOf(CCC.NAME, CCC._ID),
            "account_name = ?", arrayOf(accName), CCC._ID
        )?.use { bb = it.moveToFirst() }

        if (!bb) ContentValues().apply {
            put(CCC.ACCOUNT_NAME, accName)
            put(CCC.ACCOUNT_TYPE, accType)
            put(CCC.NAME, "Sexbook")
            put(CCC.CALENDAR_DISPLAY_NAME, "Sexbook")
            put(CCC.CALENDAR_COLOR, c.color(R.color.CP))
            put(CCC.CALENDAR_ACCESS_LEVEL, CCC.CAL_ACCESS_READ)
            put(CCC.SYNC_EVENTS, 0)
            put(CCC.CALENDAR_TIME_ZONE, "Asia/Tehran")
            c.contentResolver.insert(
                CCC.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CCC.ACCOUNT_NAME, accName)
                    .appendQueryParameter(CCC.ACCOUNT_TYPE, accType).build(), this
            )
        }/* else ContentValues().apply {
            //put(CCC.ALLOWED_REMINDERS, "") // 0,1,2
            //put(CCC.ALLOWED_AVAILABILITY, "") // 0,1,2
            //put(CCC.ALLOWED_ATTENDEE_TYPES, "") // 0,1
            c.contentResolver.update(CCC.CONTENT_URI, this, "account_name = ?", arrayOf(accName))
        }*/
    }
}
