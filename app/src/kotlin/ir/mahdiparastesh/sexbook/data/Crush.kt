package ir.mahdiparastesh.sexbook.data

import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.toDefaultType
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.more.BaseActivity
import java.util.Locale
import kotlin.experimental.and

@Entity
class Crush(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "first_name") var fName: String?,
    @ColumnInfo(name = "middle_name") var mName: String?,
    @ColumnInfo(name = "last_name") var lName: String?,
    @ColumnInfo(name = "status") var status: Byte,
    @ColumnInfo(name = "birth") var birth: String?,
    @ColumnInfo(name = "height") var height: Float,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "first_met") var first: String?,
    @ColumnInfo(name = "instagram") var insta: String?,
) {
    companion object {
        /** 3 bytes in `status` (0..7) dedicated to gender.
         * 0=>unspecified, 1=>female, 2=>male, 3=>bigender, 4=>agender (empty:5,6,7) */
        const val STAT_GENDER = 0x07.toByte()

        /** whether or not should the user be notified of their birthday. */
        const val STAT_NOTIFY_BIRTH = 0x08.toByte()

        /** whether or not they are currently an active crush.
         * (sign bit, inactive oneswould be negative)
         * unassigned bits: 0x10 (16) 0x20 (32) 0x40 (64)*/
        const val STAT_INACTIVE = 0x80.toByte()
    }

    @Ignore
    @Transient
    private var bCalendar_: GregorianCalendar? = null

    @Ignore
    @Transient
    private var fCalendar_: Calendar? = null

    fun visName(): String =
        if (fName.isNullOrEmpty() || lName.isNullOrEmpty()) when {
            !fName.isNullOrEmpty() -> fName!!
            !lName.isNullOrEmpty() -> lName!!
            !mName.isNullOrEmpty() -> mName!!
            else -> key
        } else "$fName $lName"

    /**
     * @param c if given null, only a GregorianCalendar is returned.
     * @return Do NOT alter the returned Calendar instance!
     */
    fun bCalendar(c: BaseActivity? = null, tz: TimeZone = TimeZone.getDefault()): Calendar? {
        if (bCalendar_ == null) {
            val spl = birth?.split(".") ?: return null
            bCalendar_ = GregorianCalendar(tz).apply {
                set(spl[0].toInt(), spl[1].toInt() - 1, spl[2].toInt())
            }.let { McdtpUtils.trimToMidnight(it) }
        }
        return bCalendar_?.let {
            if (c?.sp?.getBoolean(
                    Settings.spGregorianForBirthdays, Settings.spGregorianForBirthdaysDef
                ) != false
            ) it
            else it.toDefaultType(c)
        }
    }

    fun fCalendar(c: BaseActivity, tz: TimeZone = TimeZone.getDefault()): Calendar? {
        if (fCalendar_ != null) return fCalendar_
        val spl = first?.split(".") ?: return null
        fCalendar_ = GregorianCalendar(tz)
            .apply { set(spl[0].toInt(), spl[1].toInt() - 1, spl[2].toInt()) }
            .toDefaultType(c)
            .let { McdtpUtils.trimToMidnight(it) }
        return fCalendar_
    }

    fun notifyBirth(): Boolean = (status and STAT_NOTIFY_BIRTH) != 0.toByte()

    fun sum(m: Model): Float? = m.summary?.scores?.get(key)
        ?.sumOf { it.value.toDouble() }?.toFloat()

    fun last(m: Model): Long? = m.summary?.scores?.get(key)?.maxOf { it.time }

    fun copy() = Crush(
        key, fName, mName, lName, status, birth, height, address, first, insta
    )

    class Sort(private val c: BaseActivity) : Comparator<Crush> {
        private val by = c.sp.getInt(Settings.spPageLoveSortBy, 0)

        override fun compare(a: Crush, b: Crush): Int = when (by) {
            Fun.SORT_BY_NAME -> a.visName().lowercase(Locale.getDefault())
                .compareTo(b.visName().lowercase(Locale.getDefault()))
            Fun.SORT_BY_SUM -> (a.sum(c.m) ?: 0f).compareTo(b.sum(c.m) ?: 0f)
            Fun.SORT_BY_AGE -> (b.bCalendar(null)?.timeInMillis ?: 0L)
                .compareTo(a.bCalendar(null)?.timeInMillis ?: 0L)
            Fun.SORT_BY_HEIGHT -> a.height.compareTo(b.height)
            Fun.SORT_BY_BEGINNING -> (a.fCalendar(c)?.timeInMillis ?: 0L)
                .compareTo(b.fCalendar(c)?.timeInMillis ?: 0L)
            Fun.SORT_BY_LAST -> (a.last(c.m) ?: 0L).compareTo(b.last(c.m) ?: 0L)
            else -> throw IllegalArgumentException("Invalid sorting method!")
        }
    }
}
