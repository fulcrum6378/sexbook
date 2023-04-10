package ir.mahdiparastesh.sexbook.more

import android.content.ContentValues
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.CalendarContract
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

@Suppress("BlockingMethodInNonBlockingContext", "RedundantSuspendModifier")
class CalendarManager(private val c: BaseActivity, private var crushes: Iterable<Crush>?) {
    var id = -1L
    private val accName = "sexbook"
    private val accType = CalendarContract.ACCOUNT_TYPE_LOCAL
    private val tz = "GMT"
    private var index: HashMap<String/*CRUSH_KEY*/, Long/*EVENT_ID*/>? = null
    private val DEBUG = false

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
            val fIndex = Index()
            if (fIndex.exists()) {
                index = Gson().fromJson(
                    FileInputStream(fIndex).use { it.readBytes() }.toString(),
                    object : TypeToken<HashMap<String, Long>>() {}.type
                )
                // TODO CHECK FOR INTEGRITY using crushes
            } else {
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
                ?.also { index!![cr.key] = it.getId() }
        }
        FileOutputStream(Index()).use { it.write(Gson().toJson(index).encodeToByteArray()) }
    }

    private suspend fun deleteEvents() {
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

    fun updateEvent(crush: Crush) {
    }

    inner class Index : File(c.cacheDir, "calendar_index.json")
}
