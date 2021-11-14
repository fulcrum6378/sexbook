package ir.mahdiparastesh.sexbook.data

import android.os.Handler
import androidx.room.Room
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Main

@Suppress("UNCHECKED_CAST")
class Work(
    val action: Int,
    val values: List<Any>? = null,
    val handler: Handler = Main.handler
) : Thread() {
    companion object {
        const val VIEW_ONE = 0
        const val VIEW_ALL = 1
        const val INSERT_ONE = 2
        const val REPLACE_ALL = 3
        const val UPDATE_ONE = 4
        const val DELETE_ONE = 5
        const val SCROLL = 6

        // PURPOSES
        const val ADD_NEW_ITEM = 0
    }

    override fun run() {
        var db = Room.databaseBuilder(c, Database::class.java, "sexbook").build()
        var dao = db.dao()
        when (action) {
            // Report
            VIEW_ALL -> handler.obtainMessage(action, dao.getAll()).sendToTarget()

            VIEW_ONE -> if (!values.isNullOrEmpty()) handler.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.get(values[0] as Long)
            ).sendToTarget()

            INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.insert(values[0] as Report)
                handler.obtainMessage(action, result).sendToTarget()
            }

            REPLACE_ALL -> if (!values.isNullOrEmpty()) {
                dao.deleteAll(dao.getAll())
                dao.replaceAll(values as List<Report>)
                handler.obtainMessage(REPLACE_ALL).sendToTarget()
            }

            UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.update(values[0] as Report)
                handler.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                ).sendToTarget()
            }

            DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.delete(values[0] as Report)
                handler.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                ).sendToTarget()
            }


            // Crush
        }
        db.close()
    }
}