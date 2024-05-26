package ir.mahdiparastesh.sexbook.stat

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import ir.mahdiparastesh.sexbook.Fun.sumOf
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Report
import java.io.Serializable

class Summary(reports: List<Report>, var nExcluded: Int, total: Int) {
    var scores: HashMap<String, ArrayList<Orgasm>> = HashMap()
    var unknown = 0f
    var apparent = (total - nExcluded).toFloat()
    var nonCrush = 0f
    var unsafe = 0f

    init {
        for (r in reports) {
            if (r.analysis == null) r.analyse()
            if (r.analysis!!.isEmpty())
                unknown++
            else for (key in r.analysis!!)
                scores.insert(key, r.time, 1f / r.analysis!!.size)
        }
        apparent -= unknown
    }

    private fun HashMap<String, ArrayList<Orgasm>>.insert(
        theKey: String, time: Long, value: Float = 1f
    ) {
        var key = theKey
        val ckic = containsKeyIgnoreCase(key)
        if (ckic != null) key = ckic
        if (!containsKey(key) && ckic == null) this[key] = arrayListOf(Orgasm(time, value))
        else this[key]?.add(Orgasm(time, value))
    }

    private fun HashMap<String, ArrayList<Orgasm>>.containsKeyIgnoreCase(key: String): String? {
        var index: String? = null
        for (m in this) if (m.key.equals(key, true)) index = m.key
        return index
    }

    fun results(c: BaseActivity): Result {
        // tools for filtering
        var liefde = hashSetOf<String>()
        var statOnlyCrushes =
            c.sp.getBoolean(Settings.spStatOnlyCrushes, false) && c.m.liefde != null
        if (statOnlyCrushes) {
            liefde = c.m.liefde!!.map { it.key }.toHashSet()
            statOnlyCrushes = liefde.isNotEmpty()
        }
        val hideUnsafe =
            c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.m.unsafe.isNotEmpty()

        var results = HashMap<Float, ArrayList<String>>()
        var key: String
        var sum: Float
        for (crush in scores) {
            key = crush.key
            sum = crush.value.sumOf { it.value }

            // filters
            if (statOnlyCrushes && key !in liefde) {
                nonCrush += sum; continue; }
            if (hideUnsafe && key in c.m.unsafe) {
                unsafe += sum; continue; }

            if (!results.containsKey(sum)) results[sum] = arrayListOf()
            results[sum]!!.add(key)
        }
        for (k in results.keys) results[k]!!.sort()
        apparent -= nonCrush + unsafe  // will double if results() is called twice, but it doesn't.

        results =
            results.toSortedMap(reverseOrder()).toMutableMap() as HashMap<Float, ArrayList<String>>
        return Result(results/*, scores*/)
    }


    class Orgasm(val time: Long, val value: Float) : Serializable

    class Result(var calculations: HashMap<Float, ArrayList<String>>) : Parcelable {
        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        constructor(parcel: Parcel) : this(
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                parcel.readSerializable<HashMap<*, *>>(null, HashMap::class.java)
            else parcel.readSerializable()) as HashMap<Float, ArrayList<String>>
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeSerializable(calculations)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Result> {
            override fun createFromParcel(parcel: Parcel): Result = Result(parcel)
            override fun newArray(size: Int): Array<Result?> = arrayOfNulls(size)
        }
    }
}
