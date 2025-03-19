package ir.mahdiparastesh.sexbook.misc

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
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.data.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** An app widget that counts hours since the user's latest orgasm time! */
class LastOrgasm : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            Widget(context.applicationContext as Sexbook)
                .render { rv -> ids.forEach { id -> manager.updateAppWidget(id, rv) } }
        }
    }

    private class Widget(private val c: Sexbook) {
        suspend fun render(then: (rv: RemoteViews) -> Unit) {
            var number: Long? = null
            if (c.dbLoaded == true) number = c.dao.whenWasTheLastTime()
            if (number == null) {
                val db = Database.Builder(c).build()
                number = db.dao().whenWasTheLastTime()
            }

            withContext(Dispatchers.Main) {
                then(RemoteViews(c.packageName, R.layout.last_orgasm).apply {
                    setOnClickPendingIntent(
                        R.id.root, PendingIntent.getActivity(
                            c, 0, Intent(c, Main::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    setTextViewText(
                        R.id.number,
                        number?.let { ((now() - it) / 3600000L).toInt().toString() } ?: c.getString(
                            R.string.none
                        )
                    )
                })
            }
        }
    }

    companion object {
        suspend fun updateAll(c: Sexbook) {
            Widget(c).render { rv ->
                AppWidgetManager.getInstance(c).updateAppWidget(
                    ComponentName(c, LastOrgasm::class.java.name), rv
                )
            }
        }

        fun doUpdateAll(c: Sexbook) {
            CoroutineScope(Dispatchers.IO).launch { updateAll(c) }
        }
    }
}
