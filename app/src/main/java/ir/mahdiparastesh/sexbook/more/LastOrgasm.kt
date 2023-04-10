package ir.mahdiparastesh.sexbook.more

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import ir.mahdiparastesh.sexbook.Fun.now
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LastOrgasm : AppWidgetProvider() {
    override fun onUpdate(c: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Database.build(c)
            val number = ((now() - db.dao().whenWasTheLastTime()) / 3600000L).toInt()
            db.close()

            withContext(Dispatchers.Main) {
                appWidgetIds.forEach { appWidgetId ->
                    appWidgetManager.updateAppWidget(appWidgetId, update(c, number))
                }
            }
        }
    }

    private fun update(
        c: Context, n: Int
    ) = RemoteViews(c.packageName, R.layout.last_orgasm).apply {
        setOnClickPendingIntent(
            R.id.root, PendingIntent
                .getActivity(c, 0, Intent(c, Main::class.java), PendingIntent.FLAG_IMMUTABLE)
        )
        setTextViewText(R.id.number, n.toString())
    }
}
