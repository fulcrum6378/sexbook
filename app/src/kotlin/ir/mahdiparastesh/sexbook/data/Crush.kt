package ir.mahdiparastesh.sexbook.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity
class Crush(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "first_name") var fName: String?,
    @ColumnInfo(name = "middle_name") var mName: String?,
    @ColumnInfo(name = "last_name") var lName: String?,
    @ColumnInfo(name = "masculine") var masc: Boolean,
    @ColumnInfo(name = "height") var height: Float,
    @ColumnInfo(name = "birth_year") var bYear: Short,
    @ColumnInfo(name = "birth_month") var bMonth: Byte,
    @ColumnInfo(name = "birth_day") var bDay: Byte,
    @ColumnInfo(name = "location") var locat: String?,
    @ColumnInfo(name = "instagram") var insta: String?,
    @ColumnInfo(name = "notify_birth") var notifyBirth: Boolean,
) {

    fun visName(): String =
        if (fName.isNullOrEmpty() || lName.isNullOrEmpty()) when {
            !fName.isNullOrEmpty() -> fName!!
            !lName.isNullOrEmpty() -> lName!!
            !mName.isNullOrEmpty() -> mName!!
            else -> key
        } else "$fName $lName"


    fun hasFullBirth() = bYear.toInt() != -1 && bMonth.toInt() != -1 && bDay.toInt() != -1

    fun copy() = Crush(
        key, fName, mName, lName, masc, height, bYear, bMonth, bDay, locat, insta, notifyBirth
    )

    class Sort : Comparator<Crush> {
        override fun compare(a: Crush, b: Crush): Int =
            a.visName().lowercase(Locale.getDefault())
                .compareTo(b.visName().lowercase(Locale.getDefault()))
    }
}
