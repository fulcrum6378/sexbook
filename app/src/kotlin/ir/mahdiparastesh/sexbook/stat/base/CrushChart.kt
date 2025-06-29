package ir.mahdiparastesh.sexbook.stat.base

import android.icu.util.Calendar
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.stat.CrushesStat
import ir.mahdiparastesh.sexbook.stat.Taste
import ir.mahdiparastesh.sexbook.view.UiTools
import kotlin.experimental.and

/**
 * Any page that draws charts based on an attribute of [Crush] instances
 * Subinterfaces are implemented by pages inside [Taste] and [CrushesStat].
 */
interface CrushAttrChart {

    fun crushProperty(cr: Crush): Short

    fun crushFilter(cr: Crush): Boolean = true
    val isFiltered: Boolean get() = false

    fun preferredColour(mode: Int): Int? = null
    val unspecifiedQualityColour: Int
}


interface CrushQualitativeChart : CrushAttrChart {

    @get:ArrayRes
    val modes: Int
}

interface CrushGenderChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.genders
    override fun crushProperty(cr: Crush): Short = cr.status and Crush.STAT_GENDER

    override fun preferredColour(mode: Int): Int? = when (mode) {
        0 -> unspecifiedQualityColour
        1 -> 0xFFFF0037
        2 -> 0xFF0095FF
        3 -> 0xFF7300FF
        4 -> 0xFFDDFF00
        else -> throw IllegalArgumentException("Unknown gender code: $mode")
    }.toInt()
}

interface CrushSkinColourChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodySkinColour
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second).toShort()

    override fun preferredColour(mode: Int): Int? = when (mode) {
        0 -> 0xFF777777
        1 -> 0xFF633F37
        2 -> 0xFFAB7A5F
        3 -> 0xFFC0A07A
        4 -> 0xFFE5C3AE
        5 -> 0xFFF7E1D6
        6 -> 0xFFF1C9CD
        else -> throw IllegalArgumentException("Unknown skin colour code: $mode")
    }.toInt()
}

interface CrushHairColourChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyHairColour
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second).toShort()

    override fun preferredColour(mode: Int): Int? = when (mode) {
        0 -> 0xFF777777
        1 -> 0xFF000000
        2 -> 0xFF6D4730
        3 -> 0xFFFBE7A1
        4 -> 0xFFC66531
        5 -> 0xFF052F9F
        else -> throw IllegalArgumentException("Unknown hair colour code: $mode")
    }.toInt()
}

interface CrushEyeColourChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyEyeColour
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second).toShort()

    override fun preferredColour(mode: Int): Int? = when (mode) {
        0 -> unspecifiedQualityColour
        1 -> 0xFF5D301D
        2 -> 0xFF8F5929
        3 -> 0xFF947B3E
        4 -> 0xFF868254
        5 -> 0xFF798FB0
        6 -> 0xFF54427A
        else -> throw IllegalArgumentException("Unknown eye colour code: $mode")
    }.toInt()
}

interface CrushEyeShapeChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyEyeShape
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second).toShort()
}

interface CrushFaceShapeChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyFaceShape
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second).toShort()
}

interface CrushFatChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyFat
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second).toShort()
}

interface CrushMuscleChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyMuscle
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second).toShort()
}

interface CrushBreastsChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyBreasts
    override val isFiltered: Boolean get() = true

    override fun crushFilter(cr: Crush): Boolean =
        (cr.status and Crush.STAT_GENDER).let { it != 2.toShort() && it != 4.toShort() }

    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second).toShort()
}

interface CrushPenisChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyPenis
    override val isFiltered: Boolean get() = true

    override fun crushFilter(cr: Crush): Boolean =
        (cr.status and Crush.STAT_GENDER).let { it != 1.toShort() && it != 4.toShort() }

    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second).toShort()
}

interface CrushFictionalityChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.fictionality
    override fun crushProperty(cr: Crush): Short =
        (((cr.status and Crush.STAT_FICTION).toInt() shr 3) + 1).toShort()
}


interface CrushQuantitativeChart : CrushAttrChart {

    @get:StringRes
    val topic: Int

    fun divisionName(division: Int): String
}

interface CrushHeightChart : CrushQuantitativeChart {
    override val topic: Int get() = R.string.height
    override fun divisionName(division: Int): String = "${division * 10}s"
    override fun crushProperty(cr: Crush): Short =
        (if (cr.height == -1f) 0 else (cr.height / 10f).toInt()).toShort()
}

interface CrushAgeChart : CrushQuantitativeChart {
    override val topic: Int get() = R.string.age
    override fun divisionName(division: Int): String = "${division * 10}s"
    override fun crushProperty(cr: Crush): Short {
        if (cr.birthday.isNullOrBlank()) return 0.toShort()
        val year = cr.birthday!!.split("/")[0]
        if (year.isEmpty()) return 0.toShort()
        return (year.toInt() / 10).toShort()
    }
}

interface CrushFirstMetChart : CrushQuantitativeChart {
    override val topic: Int get() = R.string.firstMet
    override fun divisionName(division: Int): String = "$division"
    override fun crushProperty(cr: Crush): Short {
        if (cr.first_met == null) return 0.toShort()
        val year = UiTools.compDateTimeToCalendar(cr.first_met!!)[Calendar.YEAR]
        if (year == 1970) return 0.toShort()
        return year.toShort()
    }
}
