package ir.mahdiparastesh.sexbook.data

import android.content.Context
import android.os.Handler
import androidx.room.Room
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.PageSex

class Work(
    val c: Context,
    val action: Int,
    val values: List<Any>? = null,
    val handler: Handler =
        if (action < 10)
            (if (PageSex.handler.value != null) PageSex.handler.value!! else Main.handler)
        else
            (if (PageLove.handler.value != null) PageLove.handler.value!! else Main.handler)
) : Thread() {
    companion object {
        // Report
        const val VIEW_ONE = 0
        const val VIEW_ALL = 1
        const val INSERT_ONE = 2
        const val REPLACE_ALL = 3
        const val UPDATE_ONE = 4
        const val DELETE_ONE = 5
        const val SCROLL = 6

        // Crush
        const val C_VIEW_ONE = 10
        const val C_VIEW_ALL = 11
        const val C_INSERT_ONE = 12
        const val C_REPLACE_ALL = 13
        const val C_UPDATE_ONE = 14
        const val C_DELETE_ONE = 15

        // PURPOSES
        const val ADD_NEW_ITEM = 0

        // Other
        const val TIMEOUT = 5000L
        const val SPECIAL_ADD = 100
    }

    @Suppress("UNCHECKED_CAST")
    override fun run() {
        val db = Room.databaseBuilder(c, Database::class.java, DbFile.DATABASE)
            //.allowMainThreadQueries()
            //.fallbackToDestructiveMigration()
            //.addMigrations(MIGRATION_1_2)
            .build()
        val dao = db.dao()
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
                handler.obtainMessage(action).sendToTarget()
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
            C_VIEW_ALL -> handler.obtainMessage(action, dao.cGetAll()).sendToTarget()

            C_VIEW_ONE -> if (!values.isNullOrEmpty()) handler.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.cGet(values[0] as String)
            ).sendToTarget()

            C_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.cInsert(values[0] as Crush)
                handler.obtainMessage(action, result).sendToTarget()
            }

            C_REPLACE_ALL -> if (!values.isNullOrEmpty()) {
                dao.cDeleteAll(dao.cGetAll())
                dao.cReplaceAll(values as List<Crush>)
                handler.obtainMessage(action).sendToTarget()
            }

            C_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.cUpdate(values[0] as Crush)
                handler.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                ).sendToTarget()
            }

            C_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.cDelete(values[0] as Crush)
                handler.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                ).sendToTarget()
            }
        }
        db.close()
    }
}
