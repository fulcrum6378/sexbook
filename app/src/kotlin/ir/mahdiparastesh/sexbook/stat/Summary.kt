package ir.mahdiparastesh.sexbook.stat

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import ir.mahdiparastesh.sexbook.data.Report
import java.io.Serializable

class Summary(reports: List<Report>, var nExcluded: Int, total: Int) {
    var scores: HashMap<String, ArrayList<Orgasm>> = HashMap()
    var unknown = 0f
    var nonCrush = 0f
    var apparent = (total - nExcluded).toFloat()

    init {
        for (r in reports) {
            if (r.analysis == null) r.analyse()
            for (key in r.analysis!!)
                scores.insert(key, r.time, 1f / r.analysis!!.size)
        }
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

    fun results(): Result {
        var results = HashMap<Float, ArrayList<String>>()
        for (s in scores) {
            val key = s.key
            val sumErect = s.value.sumOf { it.value.toDouble() }.toFloat()
            if (isUnknown(key)) {
                unknown = sumErect
                apparent -= unknown
                continue
            }
            if (!results.containsKey(sumErect)) results[sumErect] = arrayListOf(key)
            else results[sumErect]!!.add(key)
            results[sumErect]!!.sort()
        }
        results =
            results.toSortedMap(reverseOrder()).toMutableMap() as HashMap<Float, ArrayList<String>>
        return Result(results/*, scores*/)
    }


    companion object {
        fun isUnknown(name: String) = name == "" || name == " " || name == "\n"
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
