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

/** An app widget that counts hours since the user's latest orgasm! */
class LastOrgasm : AppWidgetProvider() {
    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            Widget(c).render { rv -> ids.forEach { id -> manager.updateAppWidget(id, rv) } }
        }
    }

    private class Widget(private val c: Context) {
        suspend fun render(then: (rv: RemoteViews) -> Unit) {
            val number: Int? = Database.Builder(c).build().use { db ->
                db.dao().whenWasTheLastTime()?.let { ((now() - it) / 3600000L).toInt() }
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
                        R.id.number, number?.toString() ?: c.getString(R.string.none)
                    )
                })
            }
        }
    }

    companion object {
        suspend fun updateAll(c: Context) {
            Widget(c).render { rv ->
                AppWidgetManager.getInstance(c).updateAppWidget(
                    ComponentName(c, LastOrgasm::class.java.name), rv
                )
            }
        }

        fun doUpdateAll(c: Context) {
            CoroutineScope(Dispatchers.IO).launch { updateAll(c) }
        }
    }
}
