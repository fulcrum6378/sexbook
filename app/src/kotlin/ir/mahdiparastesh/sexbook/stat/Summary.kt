package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.util.NumberUtils.sumOf
import java.io.Serializable

/**
 * Summarises orgasms and their target people for statisticisation.
 * This operation is required for nearly all statistical analyses.
 */
class Summary(
    reports: List<Report>,
    var nExcluded: Int,
    total: Int,
    private val people: Set<String>
) {
    val scores: HashMap<String, Score> = hashMapOf()
    var unknown = 0f
    var apparent = (total - nExcluded).toFloat()
    var nonCrush = 0f
    var unsafe = 0f
    var classification: HashMap<Float, ArrayList<String>>? = null

    init {
        for (r in reports) {
            if (r.analysis == null) r.analyse()
            if (r.analysis!!.isEmpty())
                unknown++
            else for (key in r.analysis!!) {
                if (key.isNotEmpty())
                    scores.insert(key, Orgasm(r.time, 1f / r.analysis!!.size))
                else
                    unknown += 1f / r.analysis!!.size
            }
        }
        apparent -= unknown
    }

    private fun HashMap<String, Score>.insert(key: String, value: Orgasm) {
        for (k in keys) if (k.equals(key, true)) {
            this[k]?.orgasms?.add(value)
            return
        }
        for (p in people) if (p.equals(key, true)) {
            this[p] = Score(arrayListOf(value))
            return
        }
        this[key] = Score(arrayListOf(value))
    }

    fun classify(c: Sexbook) {
        if (classification != null) return

        // determine filtering criteria
        val statOnlyCrushes =
            c.sp.getBoolean(Settings.spStatOnlyCrushes, false) && c.liefde.isNotEmpty()
        val hideUnsafe = c.hideUnsafe()

        var results = HashMap<Float, ArrayList<String>>()
        for ((key, target) in scores) {

            // apply filters
            if (statOnlyCrushes && key !in c.liefde) {
                nonCrush += target.sum; continue; }
            if (hideUnsafe && key in c.unsafe) {
                unsafe += target.sum; continue; }

            if (!results.containsKey(target.sum)) results[target.sum] = arrayListOf()
            results[target.sum]!!.add(key)
        }
        for (k in results.keys) results[k]!!.sort()
        apparent -= nonCrush + unsafe  // will double if results() is called twice, but it doesn't.

        classification =
            results.toSortedMap(reverseOrder()).toMutableMap() as HashMap<Float, ArrayList<String>>
    }

    class Score(val orgasms: ArrayList<Orgasm>) {

        val sum: Float by lazy {
            orgasms.sumOf { it.value }  // TODO
        }

        val firstTime: Long by lazy { orgasms.minOf { it.time } }
        val lastTime: Long by lazy { orgasms.maxOf { it.time } }
    }

    data class Orgasm(val time: Long, val value: Float) : Serializable
}
