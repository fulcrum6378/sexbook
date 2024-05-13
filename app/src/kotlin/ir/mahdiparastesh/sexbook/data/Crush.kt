package ir.mahdiparastesh.sexbook.data

import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.toDefaultType
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import java.util.Locale
import kotlin.experimental.and

@Entity
class Crush(
    @PrimaryKey var key: String,
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
    constructor() : this(
        "", null, null, null, 0, null, -1f, 0,
        null, null, null
    )

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
        val BODY_SKIN_COLOUR = 0x00000007 to 0

        /** `body` offset  3; 3 bits; their hair colour (0..5)
         * 0=>unspecified, 1=>black, 2=>brunette, 3=>blonde, 4=>red, 5=>other, (6,7) */
        val BODY_HAIR_COLOUR = (0x00000007 shl 3) to 3

        /** `body` offset  6; 3 bits; their eye colour (0..5)
         * 0=>unspecified, 1=>brown, 2=>hazel, 3=>blue, 4=>green, 5=>other, (6,7) */
        val BODY_EYE_COLOUR = (0x00000007 shl 6) to 6

        /** `body` offset  9; 2 bits; whether they have round or almond eyes (0..2)
         * 0=>unspecified, 1=>round, 2=>almond, (3) */
        val BODY_EYE_SHAPE = (0x00000003 shl 9) to 9

        /** `body` offset 11; 3 bits; their face shape (0..7)
         * 0=>unspecified, 1=>diamond, 2=>heart, 3=>long, 4=>oval, 5=>round, 6=>square, 7=>triangle */
        val BODY_FACE_SHAPE = (0x00000007 shl 11) to 11

        /** `body` offset 14; 2 bits; how fat they are (0..3)
         * 0=> unspecified, 1=>thin, 2=>medium, 3=>fat */
        val BODY_FAT = (0x00000003 shl 14) to 14

        /** `body` offset 16; 2 bits; how muscular they are (0..3)
         * 0=>unspecified, 1=>normal, 2=>low, 3=>high */
        val BODY_MUSCLE = (0x00000003 shl 16) to 16

        /** `body` offset 18; 2 bits; how big their breasts are (0..3)
         * 9+1 more bits are unassigned.
         * 0=>unspecified, 1=>normal, 2=>prominent, 3=>large */
        val BODY_BREASTS = (0x00000003 shl 18) to 18

        /** `body` offset 20; 2 bits; how long their penis is (0..3)
         * do not merge into BODY_BREASTS for bigenders' sake.
         * 0=>unspecified, 1=>short, 2=>medium, 3=>long */
        val BODY_PENIS = (0x00000003 shl 20) to 20

        /** `body` offset 22; 3 bits; their sexual orientation (0..)
         * 0=>unspecified, 1=>heterosexual, 2=>homosexual, 3=>bisexual, 4=>asexual, 5=>other, (6,7) */
        val BODY_SEXUALITY = (0x00000007 shl 22) to 22
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

    class Sort(private val c: BaseActivity, spByKey: String, spAscKey: String) : Comparator<Crush> {
        private val by = c.sp.getInt(spByKey, 0)
        private val asc = c.sp.getBoolean(spAscKey, true)

        override fun compare(aa: Crush, bb: Crush): Int {
            val a = if (asc) aa else bb
            val b = if (asc) bb else aa
            return when (by) {
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

    class GsonAdapter : TypeAdapter<Crush>() {
        override fun write(w: JsonWriter, o: Crush) {
            w.beginObject()
            w.name("key").value(o.key)
            if (!o.fName.isNullOrBlank()) w.name("first_name").value(o.fName)
            if (!o.mName.isNullOrBlank()) w.name("middle_name").value(o.mName)
            if (!o.lName.isNullOrBlank()) w.name("last_name").value(o.lName)
            if (o.status != 0.toByte()) w.name("status").value(o.status)
            if (!o.birth.isNullOrBlank()) w.name("birth").value(o.birth)
            if (o.height != -1f) w.name("height").value(o.height)
            if (o.body != 0) w.name("body").value(o.body)
            if (!o.address.isNullOrBlank()) w.name("address").value(o.address)
            if (!o.first.isNullOrBlank()) w.name("first_met").value(o.first)
            if (!o.insta.isNullOrBlank()) w.name("instagram").value(o.insta)
            w.endObject()
        }

        override fun read(r: JsonReader): Crush {
            val o = Crush()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "key" -> o.key = r.nextString()
                "first_name", "fName" -> o.fName = r.nextString()
                "middle_name", "mName" -> o.mName = r.nextString()
                "last_name", "lName" -> o.lName = r.nextString()
                "status" -> o.status = r.nextInt().toByte()
                "birth" -> o.birth = r.nextString()
                "height" -> o.height = r.nextDouble().toFloat()
                "body" -> o.body = r.nextInt()
                "address" -> o.address = r.nextString()
                "first_met", "first" -> o.first = r.nextString()
                "instagram", "insta" -> o.insta = r.nextString()
            }
            r.endObject()
            return o
        }
    }
}
