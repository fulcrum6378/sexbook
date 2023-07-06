package ir.mahdiparastesh.sexbook.data

import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.tripleRound
import ir.mahdiparastesh.sexbook.Model
import java.util.Locale

@Entity
class Crush(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "first_name") var fName: String?,
    @ColumnInfo(name = "middle_name") var mName: String?,
    @ColumnInfo(name = "last_name") var lName: String?,
    @ColumnInfo(name = "gender") var gender: Byte,
    @ColumnInfo(name = "birth") var birth: String?,
    @ColumnInfo(name = "height") var height: Float,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "instagram") var insta: String?,
    @ColumnInfo(name = "first_met") var first: String?,
    @ColumnInfo(name = "notify_birth") var notifyBirth: Boolean,
) {

    fun visName(): String =
        if (fName.isNullOrEmpty() || lName.isNullOrEmpty()) when {
            !fName.isNullOrEmpty() -> fName!!
            !lName.isNullOrEmpty() -> lName!!
            !mName.isNullOrEmpty() -> mName!!
            else -> key
        } else "$fName $lName"

    fun bCalendar(tz: TimeZone = TimeZone.getDefault()): GregorianCalendar? {
        val spl = birth?.split(".") ?: return null
        return GregorianCalendar(tz)
            .apply { set(spl[0].toInt(), spl[1].toInt() - 1, spl[2].toInt()) }
    }

    fun fCalendar(tz: TimeZone = TimeZone.getDefault()): GregorianCalendar? {
        val spl = first?.split(".") ?: return null
        return GregorianCalendar(tz)
            .apply { set(spl[0].toInt(), spl[1].toInt() - 1, spl[2].toInt()) }
    }

    fun sum(m: Model): Float? = m.summary?.scores?.get(key)
        ?.sumOf { it.value.toDouble() }?.toFloat()?.tripleRound()

    fun last(m: Model): Long? = m.summary?.scores?.get(key)?.maxOf { it.time }

    fun copy() = Crush(
        key, fName, mName, lName, gender, birth, height, address, insta, first, notifyBirth
    )

    class Sort(private val by: Int, private val m: Model) : Comparator<Crush> {
        override fun compare(a: Crush, b: Crush): Int = when (by) {
            Fun.SORT_BY_NAME -> a.visName().lowercase(Locale.getDefault())
                .compareTo(b.visName().lowercase(Locale.getDefault()))
            Fun.SORT_BY_SUM -> (a.sum(m) ?: 0f).compareTo(b.sum(m) ?: 0f)
            Fun.SORT_BY_AGE -> (b.bCalendar()?.timeInMillis ?: 0L)
                .compareTo(a.bCalendar()?.timeInMillis ?: 0L)
            Fun.SORT_BY_HEIGHT -> a.height.compareTo(b.height)
            Fun.SORT_BY_LAST -> (a.last(m) ?: 0L).compareTo(b.last(m) ?: 0L)
            else -> throw IllegalArgumentException("Invalid sorting method!")
        }
    }
}
