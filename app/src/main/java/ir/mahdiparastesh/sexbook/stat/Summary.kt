package ir.mahdiparastesh.sexbook.stat

import android.os.Parcel
import android.os.Parcelable
import ir.mahdiparastesh.sexbook.data.Report
import java.io.Serializable

@Suppress("SameParameterValue")
class Summary(list: List<Report>, val nEstimated: Int, val nExcluded: Int) {
    var scores: HashMap<String, ArrayList<Erection>>
    var unknown = 0f
    val actual = list.size

    init {
        var all = arrayListOf<List<List<String>>?>()
        var meanings = arrayListOf<List<Meaning>?>()

        // For Each Masturbation...
        for (m in list) {
            var key = fixKey(m.name)

            // ONLY 2+ characters allowed
            if (key.length <= 1) {
                all.add(null)
                meanings.add(null)
                continue
            }

            // Split
            val split = splitNotes(key)
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
            for (s in shape) meanArray.add(means(s))
            meanings.add(meanArray.toList())
        }

        // Again For Each Masturbation, Gather Scores...
        scores = HashMap()
        for (e in list.indices) {
            val k = fixKey(list[e].name)
            val a = all[e]
            val m = meanings[e]
            val time = list[e].time

            // Exclude faulty or one-character notes
            if (m == null || a == null) {
                insertIntoMap(scores, k, time); continue
            }

            // Then...
            val allExceptLast = a.subList(0, a.size - 1)
            val mAllExceptLast = m.subList(0, a.size - 1)
            val last = a[a.size - 1]

            if (mAreAll(m, Meaning.ONE)) {
                a.forEach { each ->
                    insertIntoMap(scores, each.joinToString(""), time, 1f / a.size.toFloat())
                }
            }
            // His & Her & Their
            else if (a.size > 1 && last.size > 1 &&
                (last[0].equals("His", true) ||
                        last[0].equals("Her", true) ||
                        last[0].equals("Their", true)
                        ) && mAreAll(mAllExceptLast, Meaning.ONE)
            ) {
                allExceptLast.forEach { each ->
                    insertIntoMap(
                        scores, each.joinToString(), time, 1f / allExceptLast.size.toFloat()
                    )
                }
            }
            // All Are Real Persons with second names
            else if (mAnyOf(m, listOf(Meaning.ONE, Meaning.MORE_THAN_ONE))) {
                a.forEach { each ->
                    insertIntoMap(scores, each.joinToString(" "), time, 1f / a.size.toFloat())
                }
            }
            // 's
            else if (k.contains("'s ", true)) {
                if (a.size > 1 &&
                    mAnyOf(mAllExceptLast, listOf(Meaning.ONE, Meaning.MORE_THAN_ONE)) &&
                    a[a.size - 2][0].length > 2 &&
                    a[a.size - 2][0].substring(a[a.size - 2][0].length - 2)
                        .equals("'s", true)
                ) allExceptLast.forEach { each ->
                    insertIntoMap(
                        scores, each.joinToString(), time, 1f / allExceptLast.size.toFloat()
                    )
                }
                else if (a.size == 1) insertIntoMap(
                    scores, a[0][0].replace("'s", "", ignoreCase = true), time
                )
                else if (last[0].length > 2 &&
                    last[0].substring(last[0].length - 2)
                        .equals("'s", true)
                ) for (each in a) {
                    insertIntoMap(scores, unEs(each[0]), time, 1f / a.size.toFloat())
                } else insertIntoMap(scores, k, time)
            } else insertIntoMap(scores, k, time)
        }
    }

    fun results(): Result {
        var results = HashMap<Float, ArrayList<String>>()
        for (s in scores) {
            var key = s.key
            val sumErect = sumErections(s.value)
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

        fun sumErections(list: ArrayList<Erection>): Float {
            var ret = 0f
            for (i in list) ret += i.value
            return ret
        }
    }

    private fun containsKeyIgnoreCase(map: Map<String, ArrayList<Erection>>, key: String): String? {
        var index: String? = null
        for (m in map) if (m.key.equals(key, ignoreCase = true)) index = m.key
        return index
    }

    private fun replaceAll(string: String, oldStr: String, newStr: String): String {
        var ruturn = string
        while (ruturn.contains(oldStr)) ruturn = ruturn.replace(oldStr, newStr)
        return ruturn
    }

    private fun fixKey(key: String): String {
        var fixKey = key
        if (fixKey.length > 1) {
            if (fixKey[0].toString() == " ") fixKey = fixKey.substring(1)
            if (fixKey[fixKey.length - 1].toString() == " ") fixKey =
                fixKey.substring(0, fixKey.length - 2)
            if (fixKey[0].toString() == "\n") fixKey = fixKey.substring(1)
            if (fixKey[fixKey.length - 1].toString() == "\n") fixKey =
                fixKey.substring(0, fixKey.length - 2)
        }
        fixKey = replaceAll(fixKey, " and ", " + ")
        fixKey = replaceAll(fixKey, " & ", " + ")
        return fixKey
    }

    private fun splitNotes(key: String): List<List<String>> {
        val split = arrayListOf<List<String>>()
        for (s in key.split(" + ")) split.add(s.split(" "))
        return split.toList()
    }

    private fun insertIntoMap(
        sum: HashMap<String, ArrayList<Erection>>, theKey: String, time: Long, value: Float = 1f
    ) {
        var key = theKey
        var ckic = containsKeyIgnoreCase(sum, key)
        if (ckic != null) key = ckic
        if (!sum.containsKey(key) && ckic == null) sum[key] = arrayListOf(Erection(time, value))
        else sum[key]?.add(Erection(time, value))
    }

    private fun areAll(booleans: List<Boolean>, that: Boolean): Boolean {
        var yes = true
        booleans.forEach { b -> if (b != that) yes = false }
        return yes
    }

    private fun howMany(booleans: List<Boolean>, that: Boolean): Int {
        var n = 0
        booleans.forEach { b -> if (b == that) n += 1 }
        return n
    }

    private fun mAreAll(list: List<Meaning>, that: Meaning): Boolean {
        var yes = true
        list.forEach { m -> if (m != that) yes = false }
        return yes
    }

    @Suppress("unused")
    private fun mHowMany(list: List<Meaning>, that: Meaning): Int {
        var n = 0
        list.forEach { m -> if (m == that) n += 1 }
        return n
    }

    private fun mAnyOf(list: List<Meaning>, those: List<Meaning>): Boolean {
        var sum = arrayListOf<Int>()
        those.forEach { t ->
            var n = 0
            list.forEach { m -> if (m == t) n += 1 }
            sum.add(n)
        }
        var sumOfList = 0
        sum.forEach { s -> sumOfList += s }
        return sumOfList == list.size
    }

    private fun means(shape: List<Boolean>) = when {
        areAll(shape, false) -> Meaning.MERE_DESCRIPTION
        areAll(shape, true) -> when {
            shape.size > 1 -> Meaning.MORE_THAN_ONE
            shape.size == 1 -> Meaning.ONE
            else -> Meaning.NOT_SURE
        }
        else -> when {
            howMany(shape, true) == 1 -> Meaning.ONE_WITH_DESCRIPTION
            howMany(shape, true) > 1 -> Meaning.SOME_WITH_DESCRIPTION
            else -> Meaning.NOT_SURE
        }
    }

    private fun unEs(name: String) = if (name.indexOf("'s", ignoreCase = true) != -1)
        name.substring(0, name.indexOf("'s", ignoreCase = true))
    else name


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
