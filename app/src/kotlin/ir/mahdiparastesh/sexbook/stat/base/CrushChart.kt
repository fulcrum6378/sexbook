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
 * Any page that draws charts based on a property of [Crush] instances
 * Subinterfaces are implemented by pages inside [Taste] and [CrushesStat].
 */
interface CrushChart {

    fun crushProperty(cr: Crush): Short

    val isFiltered: Boolean get() = false
    fun crushFilter(cr: Crush): Boolean = true
}


interface CrushQualitativeChart : CrushChart {

    @get:ArrayRes
    val modes: Int
}

interface CrushGenderChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.genders
    override fun crushProperty(cr: Crush): Short =
        (cr.status and Crush.STAT_GENDER).toShort()
}

interface CrushSkinColourChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodySkinColour
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second).toShort()
}

interface CrushHairColourChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyHairColour
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second).toShort()
}

interface CrushEyeColourChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyEyeColour
    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second).toShort()
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
        (cr.status and Crush.STAT_GENDER).let { it != 2.toByte() && it != 4.toByte() }

    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second).toShort()
}

interface CrushPenisChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.bodyPenis
    override val isFiltered: Boolean get() = true

    override fun crushFilter(cr: Crush): Boolean =
        (cr.status and Crush.STAT_GENDER).let { it != 1.toByte() && it != 4.toByte() }

    override fun crushProperty(cr: Crush): Short =
        ((cr.body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second).toShort()
}

interface CrushFictionalityChart : CrushQualitativeChart {
    override val modes: Int get() = R.array.fictionality
    override fun crushProperty(cr: Crush): Short =
        (((cr.status and Crush.STAT_FICTION).toInt() shr 3) + 1).toShort()
}


interface CrushQuantitativeChart : CrushChart {

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
        if (cr.birth.isNullOrBlank()) return 0.toShort()
        val year = cr.birth!!.split("/")[0]
        if (year.isEmpty()) return 0.toShort()
        return (year.toInt() / 10).toShort()
    }
}

interface CrushFirstMetChart : CrushQuantitativeChart {
    override val topic: Int get() = R.string.firstMet
    override fun divisionName(division: Int): String = "$division"
    override fun crushProperty(cr: Crush): Short {
        if (cr.first == null) return 0.toShort()
        val year = UiTools.compDateTimeToCalendar(cr.first!!)[Calendar.YEAR]
        if (year == 1970) return 0.toShort()
        return year.toShort()
    }
}
