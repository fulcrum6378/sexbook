package ir.mahdiparastesh.sexbook.more

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
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
    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        checkDb(c) { n -> ids.forEach { id -> manager.updateAppWidget(id, update(c, n)) } }
    }

    /** partiallyUpdateAppWidget sucks and sending broadcast is not recommended by the guide! */
    companion object {
        private fun checkDb(c: Context, func: suspend (number: Int) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = Database.build(c)
                val number = ((now() - db.dao().whenWasTheLastTime()) / 3600000L).toInt()
                db.close()
                withContext(Dispatchers.Main) { func(number) }
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

        fun externalUpdate(c: Context) {
            /*c.sendBroadcast(
                Intent(c, LastOrgasm::class.java)
                    .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_IDS, AppWidgetManager.getInstance(c)
                            .getAppWidgetIds(ComponentName(c, LastOrgasm::class.java))
                    )
            )*/
            checkDb(c) { n ->
                /*AppWidgetManager.getInstance(c).apply {
                    partiallyUpdateAppWidget(
                        getAppWidgetIds(ComponentName(c, LastOrgasm::class.java)),
                        RemoteViews(c.packageName, R.layout.last_orgasm)
                            .apply { setTextViewText(R.id.number, n.toString()) }
                    )
                }*/
                AppWidgetManager.getInstance(c).updateAppWidget(
                    ComponentName(c, LastOrgasm::class.java.name), update(c, n)
                )
            }
        }
    }
}
