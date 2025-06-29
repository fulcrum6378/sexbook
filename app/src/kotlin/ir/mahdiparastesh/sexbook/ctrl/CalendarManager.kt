package ir.mahdiparastesh.sexbook.ctrl

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.app.ActivityCompat
import androidx.core.database.getLongOrNull
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.view.UiTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.provider.CalendarContract.Calendars as CCC
import android.provider.CalendarContract.Events as CCE

/** An interface for managing Sexbook data in the system calendar */
object CalendarManager {
    private const val accName = "sexbook"
    private const val tz = "GMT"
    const val reqCode = 1

    var id: Long? = null
    private var alteredOnce = false

    fun checkPerm(c: Context) = ActivityCompat.checkSelfPermission(
        c, Manifest.permission.READ_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        c, Manifest.permission.WRITE_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED

    @MainThread
    fun askPerm(c: BaseActivity) {
        ActivityCompat.requestPermissions(
            c, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            reqCode
        )
    }

    /** Creates the calendar if it doesn't exist, if it does, retrieves its ID. */
    suspend fun initialise(c: Sexbook) {
        //Log.d("ZOEY", "CalendarManager: initialise")
        if (alteredOnce) return
        val canCreateCalendar = // implicitly `alterCode != 2`
            c.sp.getBoolean(Settings.spCalOutput, false)
        val cache = AlterationCache()
        val cacheExists = cache.exists()
        if (!canCreateCalendar && !cacheExists) return

        // check the existence of our calendar
        c.contentResolver.query(
            CCC.CONTENT_URI, arrayOf(CCC.NAME, CCC._ID),
            "account_name = ?", arrayOf(accName), CCC._ID
        )?.use { if (it.moveToFirst()) id = it.getLongOrNull(it.getColumnIndex(CCC._ID)) }

        // check if a previous necessary alteration operation has to be executed now
        var alterCode = 0
        if (cacheExists) {
            alterCode = cache.readCode()
            cache.delete()
        }
        if (id != null && id != -1L) {
            when (alterCode) {
                1 -> update(c)
                2 -> destroy(c)
            }
            return
        }

        // insert calendar and fill it with events if required
        if (canCreateCalendar) ContentValues().apply {
            put(CCC.ACCOUNT_NAME, accName)
            put(CCC.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CCC.NAME, "Sexbook")
            put(CCC.CALENDAR_DISPLAY_NAME, c.getString(R.string.app_name))
            put(CCC.CALENDAR_COLOR, c.resources.getColor(R.color.CP_LIGHT, c.theme))
            put(CCC.CALENDAR_ACCESS_LEVEL, CCC.CAL_ACCESS_READ)
            put(CCC.SYNC_EVENTS, 0)
            put(CCC.CALENDAR_TIME_ZONE, tz)
            c.contentResolver.insert(
                CCC.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CCC.ACCOUNT_NAME, accName)
                    .appendQueryParameter(
                        CCC.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL
                    ).build(), this
            )?.also { id = it.getId() }
            Log.d("ZOEY", "CalendarManager: a calendar was created with ID: $id")
            insertEvents(c)
        }
    }

    private fun Uri.getId() =
        toString().substringAfterLast("/").substringBefore("?").toLong()

    @Suppress("RedundantSuspendModifier")
    private suspend fun insertEvents(c: Sexbook) {
        //Log.d("ZOEY", "CalendarManager: insertEvents")

        // prepare a list of people whose birthdays are important
        val bdList = hashSetOf<Crush>()
        for (crush in c.liefde) c.people[crush]?.also { bdList.add(it) }
        for (person in c.people.values) if (person.notifyBirth()) bdList.add(person)
        if (bdList.isEmpty()) return

        // add their birthdays one by one into the system calendar
        val thisTz = TimeZone.getTimeZone(tz)
        for (crush in bdList) {
            val birthTime = crush.birthday
                ?.replace(".", "/")
                ?.let { UiTools.compDateTimeToCalendar(it, thisTz).timeInMillis }
                ?: continue
            //Log.d("ZOEY", "CalendarManager: $crush in c.liefde")
            ContentValues().apply {
                put(CCE.CALENDAR_ID, id)
                put(CCE.TITLE, c.getString(R.string.sBirthday, crush.visName()))
                put(CCE.DTSTART, birthTime)
                put(CCE.RRULE, "FREQ=YEARLY")
                put(CCE.DURATION, "P1D")
                put(CCE.ALL_DAY, 1)
                put(CCE.EVENT_TIMEZONE, tz)
                c.contentResolver.insert(CCE.CONTENT_URI, this)
            }
        }
        alteredOnce = true
    }

    suspend fun update(c: Sexbook) {
        //Log.d("ZOEY", "CalendarManager: update (id: $id)")
        if (id == null) return
        if (alteredOnce) {
            AlterationCache().writeCode(1)
            return; }
        //Log.d("ZOEY", "CalendarManager: updating")

        deleteEvents(c)
        insertEvents(c)
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun deleteEvents(c: Sexbook) {
        c.contentResolver.delete(CCE.CONTENT_URI, "calendar_id = ?", arrayOf(id.toString()))
        alteredOnce = true
    }

    fun destroy(c: Sexbook) {
        //Log.d("ZOEY", "CalendarManager: destory (id: $id)")
        if (id == null) return
        if (alteredOnce) {
            AlterationCache().writeCode(2)
            return; }
        //Log.d("ZOEY", "CalendarManager: destorying")

        CoroutineScope(Dispatchers.IO).launch {
            deleteEvents(c)
            c.contentResolver.delete(CCC.CONTENT_URI, "account_name = ?", arrayOf(accName))
            id = null
        }
    }

    @SuppressLint("SdCardPath")
    class AlterationCache : File(
        "/data/data/" + Sexbook::class.java.`package`!!.name + "/cache/alter_calendar"
    ) {
        fun writeCode(code: Int) {
            outputStream().use { it.write(code) }
        }

        fun readCode(): Int = inputStream().use { it.read() }
    }
}
