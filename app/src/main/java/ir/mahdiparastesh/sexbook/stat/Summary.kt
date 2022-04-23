package ir.mahdiparastesh.sexbook.stat

import android.os.Parcel
import android.os.Parcelable
import ir.mahdiparastesh.sexbook.data.Report
import java.io.Serializable

class Summary(list: List<Report>, val nEstimated: Int, val nExcluded: Int) {
    var scores: HashMap<String, ArrayList<Erection>>
    var unknown = 0f
    val actual = list.size

    init {
        var all = arrayListOf<List<List<String>>?>()
        var meanings = arrayListOf<List<Meaning>?>()

        // An intial partial analysis
        for (m in list) {
            var key = m.name.fixKey()

            // ONLY 2+ characters allowed
            if (key.length < 2) {
                all.add(null)
                meanings.add(null)
                continue
            }

            // Split crushes
            val split = arrayListOf<List<String>>()
            for (s in key.split(" + ")) split.add(s.split(" "))
            all.add(split)

            // Analyze the notes' shape
            val shape = arrayListOf<ArrayList<Boolean>>()
            split.forEach { s1 ->
                var thisShape = arrayListOf<Boolean>()
                // Check if it is a  person's name
                s1.forEach { s2 -> thisShape.add(s2[0].isUpperCase() || s2[0] == '#') }
                shape.add(thisShape)
            } // shape.size is certainly 1+ now...
            var meanArray = arrayListOf<Meaning>()
            for (sh in shape) meanArray.add(when {
                sh.all { !it } -> Meaning.MERE_DESCRIPTION
                sh.all { it } -> when {
                    sh.size > 1 -> Meaning.MORE_THAN_ONE
                    sh.size == 1 -> Meaning.ONE
                    else -> Meaning.NOT_SURE
                }
                else -> {
                    val number = sh.count { it }
                    when {
                        number == 1 -> Meaning.ONE_WITH_DESCRIPTION
                        number > 1 -> Meaning.SOME_WITH_DESCRIPTION
                        else -> Meaning.NOT_SURE
                    }
                }
            })
            meanings.add(meanArray.toList())
        }

        // Identify crushes from each record and gather scores for them...
        scores = HashMap()
        for (e in list.indices) {
            val k = list[e].name.fixKey()
            val a = all[e]
            val m = meanings[e]
            val time = list[e].time

            // Exclude faulty or one-character notes
            if (m == null || a == null) {
                scores.insert(k, time); continue
            }

            // Then...
            val allExceptLast = a.subList(0, a.size - 1)
            val mAllExceptLast = m.subList(0, a.size - 1)
            val last = a[a.size - 1]

            if (m.all { it == Meaning.ONE }) {
                a.forEach { each ->
                    scores.insert(each.joinToString(""), time, 1f / a.size.toFloat())
                }
            }
            // His & Her & Their
            else if (a.size > 1 && last.size > 1 &&
                (last[0].equals("His", true) ||
                        last[0].equals("Her", true) ||
                        last[0].equals("Their", true)
                        ) && mAllExceptLast.all { it == Meaning.ONE }
            ) {
                allExceptLast.forEach { each ->
                    scores.insert(each.joinToString(), time, 1f / allExceptLast.size.toFloat())
                }
            }
            // All Are Real Persons with second names,
            // or complicated name like "Queeny <v>an <d>er Zande"
            else if (m.mAnyOf(
                    Meaning.ONE, Meaning.MORE_THAN_ONE,
                    Meaning.ONE_WITH_DESCRIPTION, Meaning.SOME_WITH_DESCRIPTION
                )
            ) a.forEach { each ->
                scores.insert(each.joinToString(" "), time, 1f / a.size.toFloat())
            }
            // 's
            else if (k.contains("'s ", true)) {
                if (a.size > 1 &&
                    mAllExceptLast.mAnyOf(
                        Meaning.ONE, Meaning.MORE_THAN_ONE,
                        Meaning.ONE_WITH_DESCRIPTION, Meaning.SOME_WITH_DESCRIPTION
                    ) &&
                    a[a.size - 2][0].length > 2 &&
                    a[a.size - 2][0].substring(a[a.size - 2][0].length - 2)
                        .equals("'s", true)
                ) allExceptLast.forEach { each ->
                    scores.insert(each.joinToString(), time, 1f / allExceptLast.size.toFloat())
                }
                else if (a.size == 1) scores.insert(
                    a[0][0].replace("'s", "", true), time
                )
                else if (last[0].length > 2 &&
                    last[0].substring(last[0].length - 2)
                        .equals("'s", true)
                ) for (each in a) {
                    scores.insert(unEs(each[0]), time, 1f / a.size.toFloat())
                } else scores.insert(k, time)
            } else scores.insert(k, time)
        }
    }

    private fun String.fixKey(): String {
        var fixKey = this
        if (fixKey.length > 1) {
            if (fixKey[0].toString() == " ") fixKey = fixKey.substring(1)
            if (fixKey[fixKey.length - 1].toString() == " ") fixKey =
                fixKey.substring(0, fixKey.length - 2)
            if (fixKey[0].toString() == "\n") fixKey = fixKey.substring(1)
            if (fixKey[fixKey.length - 1].toString() == "\n") fixKey =
                fixKey.substring(0, fixKey.length - 2)
        }
        return fixKey
            .replace(" and ", " + ")
            .replace(" & ", " + ")
    }

    private fun HashMap<String, ArrayList<Erection>>.insert(
        theKey: String, time: Long, value: Float = 1f
    ) {
        var key = theKey
        var ckic = containsKeyIgnoreCase(key)
        if (ckic != null) key = ckic
        if (!containsKey(key) && ckic == null) this[key] = arrayListOf(Erection(time, value))
        else this[key]?.add(Erection(time, value))
    }

    private fun HashMap<String, ArrayList<Erection>>.containsKeyIgnoreCase(key: String): String? {
        var index: String? = null
        for (m in this) if (m.key.equals(key, true)) index = m.key
        return index
    }

    private fun List<Meaning>.mAnyOf(vararg those: Meaning): Boolean {
        val sum = arrayListOf<Int>()
        those.forEach { t ->
            var n = 0
            forEach { m -> if (m == t) n += 1 }
            sum.add(n)
        }
        var sumOfList = 0
        sum.forEach { s -> sumOfList += s }
        return sumOfList == size
    }

    private fun unEs(name: String) = if (name.indexOf("'s", ignoreCase = true) != -1)
        name.substring(0, name.indexOf("'s", ignoreCase = true))
    else name

    fun results(): Result {
        var results = HashMap<Float, ArrayList<String>>()
        for (s in scores) {
            var key = s.key
            val sumErect = s.value.sumOf { it.value.toDouble() }.toFloat()
            if (isUnknown(key)) {
                unknown = sumErect
                continue
            }
            if (!results.containsKey(sumErect)) results[sumErect] = arrayListOf(key)
            else results[sumErect]!!.add(key)
            results[sumErect]!!.sort()
        }
        results =
            results.toSortedMap(reverseOrder()).toMutableMap() as HashMap<Float, ArrayList<String>>
        return Result(results, scores)
    }


    companion object {
        fun isUnknown(name: String) = name == "" || name == " " || name == "\n"
    }

    private enum class Meaning {
        MERE_DESCRIPTION, ONE, ONE_WITH_DESCRIPTION, SOME_WITH_DESCRIPTION, MORE_THAN_ONE, NOT_SURE
    }

    class Erection(val time: Long, val value: Float) : Serializable

    @Suppress("UNCHECKED_CAST")
    class Result(
        var calculations: HashMap<Float, ArrayList<String>>,
        var scores: HashMap<String, ArrayList<Erection>>
    ) : Parcelable {
        private constructor(parcel: Parcel) : this(
            calculations = parcel.readSerializable() as HashMap<Float, ArrayList<String>>,
            scores = parcel.readSerializable() as HashMap<String, ArrayList<Erection>>
        )

        override fun writeToParcel(out: Parcel?, flags: Int) {
            out?.writeSerializable(calculations)
            out?.writeSerializable(scores)
        }

        override fun describeContents() = 0

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR = object : Parcelable.Creator<Result> {
                override fun createFromParcel(parcel: Parcel) = Result(parcel)
                override fun newArray(size: Int) = arrayOfNulls<Result>(size)
            }
        }
    }
}
