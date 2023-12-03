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
    @ColumnInfo(name = "body") var body: Int,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "first_met") var first: String?,
    @ColumnInfo(name = "instagram") var insta: String?,
) {
    companion object {
        /** `status` offset 0; 3 bits; their gender (0..4)
         * 0=>unspecified, 1=>female, 2=>male, 3=>bigender, 4=>agender, (5,6,7) */
        const val STAT_GENDER = 0x07.toByte()

        /** `status` offset 3; 1 bit; whether or not they are a fictional character. */
        const val STAT_FICTION = 0x08.toByte()

        /** `status` offset 4; 1 bit; whether or not should the user be notified of their birthday. */
        const val STAT_NOTIFY_BIRTH = 0x10.toByte()

        /** `status` offset 7; 1 bit; whether or not they are currently an active crush.
         * (sign bit, inactive oneswould be negative)
         * unassigned bits: 0x20 (32) , 0x40 (64) */
        const val STAT_INACTIVE = 0x80.toByte()


        /** `body` offset  0; 3 bits; their skin colour (0..6)
         * 0=>unspecified, 1=>black, 2=>brown, 3=>olive, 4=>medium, 5=>fair, 6=>pale, (7) */
        val BODY_SKIN_COLOUR = 0x00000005 to 0

        /** `body` offset  3; 3 bits; their hair colour (0..4)
         * 0=>unspecified, 1=>black, 2=>brunette, 3=>blonde, 4=>red, (5,6,7) */
        val BODY_HAIR_COLOUR = 0x00000028 to 3  // 0000000x05 shl 3

        /** `body` offset  6; 3 bits; their eye colour (0..6)
         * 0=>unspecified, 1=>brown, 2=>hazel, 3=>blue, 4=>green, 5=>grey, 6=>other (7) */
        val BODY_EYE_COLOUR = 0x00000140 to 6  // 0000000x05 shl 6

        /** `body` offset  9; 2 bits; whether they have almond eyes (0..2)
         * 0=>unspecified, 1=>no, 2=>yes */
        val BODY_ALMOND_EYES = 0x00000600 to 9  // 0000000x03 shl 9

        /** `body` offset 11; 3 bits; their face shape (0..7)
         * 0=>unspecified, 1=>diamond, 2=>heart, 3=>long, 4=>oval, 5=>round, 6=>square, 7=>triangle */
        val BODY_FACE_SHAPE = 0x00002800 to 11  // 0000000x05 shl 11

        /** `body` offset 14; 2 bits; how fat they are (0..3)
         * 0=> unspecified, 1=>thin, 2=>medium, 3=>fat */
        val BODY_FAT = 0x0000c000 to 14  // 0000000x03 shl 14

        /** `body` offset 16; 2 bits; how muscular they are (0..3)
         * 0=>unspecified, 1=>normal, 2=>low, 3=>high */
        val BODY_MUSCLE = 0x00030000 to 16  // 0000000x03 shl 16

        /** `body` offset 18; 2 bits; how long their penis is (0..3)
         * do not merge into BODY_BREASTS for bigenders' sake.
         * 0=>unspecified, 1=>short, 2=>medium, 3=>long */
        val BODY_PENIS = 0x000c0000 to 18  // 0000000x03 shl 18

        /** `body` offset 20; 2 bits; how big their breasts are (0..3)
         * 9+1 more bits are unassigned.
         * 0=>unspecified, 1=>normal, 2=>prominent, 3=>extra */
        val BODY_BREASTS = 0x00300000 to 20  // 0000000x03 shl 20
    }

    fun active(): Boolean = (status and STAT_INACTIVE) == 0.toByte()

    fun fiction(): Boolean = (status and STAT_FICTION) != 0.toByte()

    fun notifyBirth(): Boolean = (status and STAT_NOTIFY_BIRTH) != 0.toByte()

    fun body(field: Pair<Int, Int>): Int = (body and field.first) shr field.second

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

    fun sum(m: Model): Float? = m.summary?.scores?.get(key)
        ?.sumOf { it.value.toDouble() }?.toFloat()

    fun last(m: Model): Long? = m.summary?.scores?.get(key)?.maxOf { it.time }

    fun copy() = Crush(
        key, fName, mName, lName, status, birth, height, body, address, first, insta
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
