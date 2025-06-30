package ir.mahdiparastesh.sexbook.data

import androidx.annotation.IdRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.view.UiTools
import java.util.Locale
import kotlin.experimental.and

/**
 * A `Crush` is a person who has ever engaged in any kind of sexual activity with the user.
 * The user can optionally add various kinds of information about them, then Sexbook shall create
 * various kinds of statistics based on them.
 */
@Suppress("PropertyName")
@Entity
class Crush(

    /** A unique identifier that can come exclusively or inclusively inside a [Report.name] string */
    @PrimaryKey
    var key: String,

    var first_name: String? = null,
    var middle_name: String? = null,
    var last_name: String? = null,

    /**
     * A 16-bit integer which stores multiple essential data about a person:
     *
     * - Gender
     * - Are they a real person or a fictional character?
     * - Should Sexbook notify their birthday to the user?
     * - Do they have an unsafe personality?
     * - Are they actively engaged?
     */
    @ColumnInfo(index = true, defaultValue = "0")
    var status: Short = 0,

    /**
     * If real person => Their birthday
     * If fictional character => Their creation date
     *
     * Pattern: `YYYY/MM/DD HH:MM:SS` (only Gregorian is allowed)
     */
    var birthday: String? = null,

    @ColumnInfo(defaultValue = "-1.0")
    var height: Float = -1f,

    /** A 32-bit integer which stores multiple characteristics of a person's body */
    @ColumnInfo(defaultValue = "0")
    var body: Int = 0,

    /**
     * If real person => Their address
     * If fictional character => Their creator
     */
    var address: String? = null,

    /**
     * The date the user saw them for the first time
     *
     * Pattern: `YYYY/MM/DD HH:MM:SS` (only Gregorian is allowed)
     */
    var first_met: String? = null,

    /** Their Instagram username */
    var instagram: String? = null,

    /** A preferred hue number of the HSV color system for this person (0f..360f) */
    var hue: Float? = null,
) {

    companion object {

        /**
         * - 0 => Unspecified
         * - 1 => Fictional character
         * - 2 => Reachable (willing to meet in-person)
         * - 3 => Contactable (willing to chat)
         * - 4 => Observable (e.g. active in social media)
         * - 5 => Vanished
         * - 6 => Dead
         * - (7): empty
         */
        private const val STAT_EXISTENCE = 0b111.toShort()  // 7

        /**
         * - 0 => Unspecified
         * - 1 => Female
         * - 2 => Male
         * - 3 => Bigender
         * - 4 => Agender
         * - (5,6,7): empty
         */
        private const val STAT_GENDER = (0b111 shl 3).toShort()  // 56

        /** Whether or not they like men */
        const val STAT_ANDROPHILIA = (0b1 shl 7).toShort()  // 128

        /** Whether or not they like women */
        const val STAT_GYNEPHILIA = (0b1 shl 8).toShort()  // 256

        /** Whether or not they have an unsafe personality */
        const val STAT_UNSAFETY = (0b1 shl 9).toShort()  // 512

        /** Shall the user be notified of their birthday? */
        const val STAT_NOTIFY_BIRTH = (0b1 shl 10).toShort()  // 1024

        /** Whether or not they are currently an active crush
         * (sign bit, inactive ones would be negative)
         * 4 bits are unused before this */
        const val STAT_INACTIVE = (0b1 shl 15).toShort()  // 32768


        /** `body` offset  0; 3 bits; their skin colour (0..6)
         * 0=>unspecified, 1=>black, 2=>brown, 3=>olive, 4=>medium, 5=>fair, 6=>pale, (7) */
        val BODY_SKIN_COLOUR = 0b111 to 0

        /** `body` offset  3; 3 bits; their hair colour (0..5)
         * 0=>unspecified, 1=>black, 2=>brunette, 3=>blonde, 4=>red, 5=>other, (6,7) */
        val BODY_HAIR_COLOUR = (0b111 shl 3) to 3

        /** `body` offset  6; 3 bits; their eye colour (0..5)
         * 0=>unspecified, 1=>dark_brown, 2=>light_brown, 3=>hazel, 4=>green, 5=>blue, 6=>other, (7) */
        val BODY_EYE_COLOUR = (0b111 shl 6) to 6

        /** `body` offset  9; 2 bits; whether they have round or almond eyes (0..2)
         * 0=>unspecified, 1=>round, 2=>almond, (3) */
        val BODY_EYE_SHAPE = (0b11 shl 9) to 9

        /** `body` offset 11; 3 bits; their face shape (0..7)
         * 0=>unspecified, 1=>diamond, 2=>heart, 3=>long, 4=>oval, 5=>round, 6=>square, 7=>triangle */
        val BODY_FACE_SHAPE = (0b111 shl 11) to 11

        /** `body` offset 14; 2 bits; how fat they are (0..3)
         * 0=> unspecified, 1=>thin, 2=>medium, 3=>fat */
        val BODY_FAT = (0b11 shl 14) to 14

        /** `body` offset 16; 2 bits; how muscular they are (0..3)
         * 0=>unspecified, 1=>normal, 2=>low, 3=>high */
        val BODY_MUSCLE = (0b11 shl 16) to 16

        /** `body` offset 18; 2 bits; how big their breasts are (0..3)
         * 9+1 more bits are unassigned.
         * 0=>unspecified, 1=>normal, 2=>prominent, 3=>large */
        val BODY_BREASTS = (0b11 shl 18) to 18

        /** `body` offset 20; 2 bits; how long their penis is (0..3)
         * do not merge into BODY_BREASTS for bigenders' sake.
         * 0=>unspecified, 1=>short, 2=>medium, 3=>long */
        val BODY_PENIS = (0b11 shl 20) to 20


        const val INSTA = "https://www.instagram.com/"

        var statsCleared = true
    }


    fun visName(): String =
        if (first_name.isNullOrEmpty() || last_name.isNullOrEmpty()) when {
            !first_name.isNullOrEmpty() -> first_name!!
            !last_name.isNullOrEmpty() -> last_name!!
            !middle_name.isNullOrEmpty() -> middle_name!!
            else -> key
        } else "$first_name $last_name"

    fun presence(): Int = (status and STAT_EXISTENCE).toInt()

    fun fiction(): Boolean = presence() == 1

    fun gender(): Int = (status and STAT_GENDER).toInt() shr 3

    fun androphile(): Boolean = (status and STAT_ANDROPHILIA) != 0.toShort()

    fun gynephile(): Boolean = (status and STAT_GYNEPHILIA) != 0.toShort()

    fun unsafe(): Boolean = (status and STAT_UNSAFETY) != 0.toShort()

    fun notifyBirth(): Boolean = (status and STAT_NOTIFY_BIRTH) != 0.toShort()

    fun active(): Boolean = (status and STAT_INACTIVE) == 0.toShort()

    fun body(field: Pair<Int, Int>): Int = (body and field.first) shr field.second


    /** Birthday as a timestamp */
    @delegate:Ignore
    @delegate:Transient
    val birthTime: Long? by lazy {
        birthday?.let { UiTools.compDateTimeToCalendar(it).timeInMillis }
    }

    /** First met as a timestamp */
    @delegate:Ignore
    @delegate:Transient
    val firstTime: Long? by lazy {
        first_met?.let { UiTools.compDateTimeToCalendar(it).timeInMillis }
    }


    @Ignore
    @Transient
    private var sum_: Float? = null

    private fun sum(c: Sexbook): Float? =
        c.summary?.scores?.get(key)?.sum

    fun getSum(c: Sexbook): Float {
        if (sum_ == null) sum(c)?.also {
            sum_ = it
            statsCleared = false
        }
        return sum_ ?: 0f
    }


    @Ignore
    @Transient
    private var lastOrgasm_: Long? = null

    private fun lastOrgasm(c: Sexbook): Long? =
        c.summary?.scores?.get(key)?.lastOrgasm

    fun getLastOrgasm(c: Sexbook): Long {
        if (lastOrgasm_ == null) lastOrgasm(c)?.also {
            lastOrgasm_ = it
            statsCleared = false
        }
        return lastOrgasm_ ?: 0L
    }


    @Ignore
    @Transient
    private var firstOrgasm_: Long? = null

    private fun firstOrgasm(c: Sexbook): Long? =
        c.summary?.scores?.get(key)?.firstOrgasm

    fun getFirstOrgasm(c: Sexbook): Long {
        if (firstOrgasm_ == null) firstOrgasm(c)?.also {
            firstOrgasm_ = it
            statsCleared = false
        }
        return firstOrgasm_ ?: 0L
    }


    fun resetStats() {
        sum_ = null
        lastOrgasm_ = null
        firstOrgasm_ = null
    }


    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = key
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return key == (other as Crush).key
    }


    class Sort(
        private val c: Sexbook, private val by: Int, private val asc: Boolean
    ) : Comparator<String> {

        constructor(c: Sexbook, spByKey: String, spAscKey: String) : this(
            c, c.sp.getInt(spByKey, 0), c.sp.getBoolean(spAscKey, true)
        )

        override fun compare(aa: String, bb: String): Int {
            val a = if (asc) aa else bb
            val b = if (asc) bb else aa
            return when (by) {
                SORT_BY_NAME -> c.people[a]!!.visName().lowercase(Locale.getDefault())
                    .compareTo(c.people[b]!!.visName().lowercase(Locale.getDefault()))
                SORT_BY_SUM -> c.people[a]!!.getSum(c)
                    .compareTo(c.people[b]!!.getSum(c))
                SORT_BY_AGE -> (c.people[b]!!.birthTime ?: 0L)
                    .compareTo(c.people[a]!!.birthTime ?: 0L)
                SORT_BY_HEIGHT -> c.people[a]!!.height
                    .compareTo(c.people[b]!!.height)
                SORT_BY_BEGINNING -> (c.people[a]!!.firstTime ?: 0L)
                    .compareTo(c.people[b]!!.firstTime ?: 0L)
                SORT_BY_LAST -> c.people[a]!!.getLastOrgasm(c)
                    .compareTo(c.people[b]!!.getLastOrgasm(c))
                SORT_BY_FIRST -> c.people[a]!!.getFirstOrgasm(c)
                    .compareTo(c.people[b]!!.getFirstOrgasm(c))
                else -> throw IllegalArgumentException("Invalid sorting method!")
            }
        }

        companion object {
            const val SORT_BY_NAME = 0
            const val SORT_BY_SUM = 1
            const val SORT_BY_AGE = 2
            const val SORT_BY_HEIGHT = 3
            const val SORT_BY_BEGINNING = 4
            const val SORT_BY_LAST = 5
            const val SORT_BY_FIRST = 6

            fun sort(@IdRes menuItemId: Int): Any? = when (menuItemId) {
                R.id.sortByName -> SORT_BY_NAME
                R.id.sortBySum -> SORT_BY_SUM
                R.id.sortByAge -> SORT_BY_AGE
                R.id.sortByHeight -> SORT_BY_HEIGHT
                R.id.sortByBeginning -> SORT_BY_BEGINNING
                R.id.sortByLast -> SORT_BY_LAST
                R.id.sortByFirst -> SORT_BY_FIRST
                R.id.sortAsc -> true
                R.id.sortDsc -> false
                else -> null
            }

            fun findSortMenuItemId(sortBy: Int) = when (sortBy) {
                SORT_BY_NAME -> R.id.sortByName
                SORT_BY_SUM -> R.id.sortBySum
                SORT_BY_AGE -> R.id.sortByAge
                SORT_BY_HEIGHT -> R.id.sortByHeight
                SORT_BY_BEGINNING -> R.id.sortByBeginning
                SORT_BY_LAST -> R.id.sortByLast
                SORT_BY_FIRST -> R.id.sortByFirst
                else -> throw IllegalArgumentException("Invalid sorting method!")
            }
        }
    }


    class GsonAdapter : TypeAdapter<Crush>() {

        override fun write(w: JsonWriter, o: Crush) {
            w.beginObject()
            w.name("key").value(o.key)
            if (!o.first_name.isNullOrBlank()) w.name("first_name").value(o.first_name)
            if (!o.middle_name.isNullOrBlank()) w.name("middle_name").value(o.middle_name)
            if (!o.last_name.isNullOrBlank()) w.name("last_name").value(o.last_name)
            if (o.status != 0.toShort()) w.name("status").value(o.status)
            if (!o.birthday.isNullOrBlank()) w.name("birthday").value(o.birthday)
            if (o.height != -1f) w.name("height").value(o.height)
            if (o.body != 0) w.name("body").value(o.body)
            if (!o.address.isNullOrBlank()) w.name("address").value(o.address)
            if (!o.first_met.isNullOrBlank()) w.name("first_met").value(o.first_met)
            if (!o.instagram.isNullOrBlank()) w.name("instagram").value(o.instagram)
            if (o.hue != null) w.name("hue").value(o.hue)
            w.endObject()
        }

        override fun read(r: JsonReader): Crush {
            val o = Crush("")
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "key" -> o.key = r.nextString()
                "first_name", "fName" -> o.first_name = r.nextString()
                "middle_name", "mName" -> o.middle_name = r.nextString()
                "last_name", "lName" -> o.last_name = r.nextString()
                "status" -> o.status = r.nextInt().toShort()
                "birthday", "birth" -> o.birthday = r.nextString()
                "height" -> o.height = r.nextDouble().toFloat()
                "body" -> o.body = r.nextInt()
                "address" -> o.address = r.nextString()
                "first_met", "first" -> o.first_met = r.nextString()
                "instagram", "insta" -> o.instagram = r.nextString()
                "hue" -> o.hue = r.nextDouble().toFloat()
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
