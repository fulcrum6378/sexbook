package ir.mahdiparastesh.sexbook.stat.base

import androidx.annotation.MainThread
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import kotlinx.coroutines.Job

/**
 * An Activity which displays only one chart at a time;
 * namely it has only one page, but can have multiple chart types.
 */
interface SingleChartActivity {

    var job: Job?

    suspend fun prepareData(): AbstractChartData

    @MainThread
    suspend fun drawChart(data: AbstractChartData)
}
