package ir.mahdiparastesh.sexbook.data

import android.content.Context
import android.os.Handler
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.mahdiparastesh.sexbook.*

class Work(
    val c: Context,
    val action: Int,
    private val values: List<Any>? = null,
    val handler: Handler? = when {
        action < 10 -> if (PageSex.handler.value != null) PageSex.handler.value!! else Main.handler
        action < 20 -> if (PageLove.handler.value != null) PageLove.handler.value!! else Main.handler
        action < 30 -> Places.handler ?: Main.handler
        action < 40 -> Estimation.handler ?: Main.handler
        else -> Main.handler
    }
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

        // Place
        const val P_VIEW_ONE = 20
        const val P_VIEW_ALL = 21
        const val P_INSERT_ONE = 22
        const val P_REPLACE_ALL = 23
        const val P_UPDATE_ONE = 24
        const val P_DELETE_ONE = 25

        // Guess
        const val G_VIEW_ONE = 30
        const val G_VIEW_ALL = 31
        const val G_INSERT_ONE = 32
        const val G_REPLACE_ALL = 33
        const val G_UPDATE_ONE = 34
        const val G_DELETE_ONE = 35

        // PURPOSES
        const val ADD_NEW_ITEM = 0

        // Other
        const val TIMEOUT = 5000L
        const val SPECIAL_ADD = 100
        // const val ADMOB_LOADED = 101
    }

    @Suppress("UNCHECKED_CAST")
    override fun run() {
        val db = Room.databaseBuilder(c, Database::class.java, Database.DbFile.DATABASE)
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Crush RENAME TO Crush_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Crush` (`key` TEXT NOT NULL, " +
                                "`first_name` TEXT, `middle_name` TEXT, `last_name` TEXT, " +
                                "`masculine` INTEGER NOT NULL, `height` REAL NOT NULL, " +
                                "`birth_year` INTEGER NOT NULL, `birth_month` INTEGER NOT NULL, " +
                                "`birth_day` INTEGER NOT NULL, `location` TEXT, `instagram` TEXT, " +
                                "`notify_birth` INTEGER NOT NULL, PRIMARY KEY(`key`))"
                    )
                    val columns = "key, first_name, last_name, masculine, height, " +
                            "birth_year, birth_month, birth_day, location, instagram, notify_birth"
                    db.execSQL(
                        "INSERT INTO Crush (" + columns + ") SELECT "
                                + columns + " FROM Crush_old;"
                    )
                    db.execSQL("DROP TABLE Crush_old")
                }
            }, object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Guess RENAME TO Guess_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Guess` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `crsh` TEXT, " +
                                "`sinc` INTEGER NOT NULL, `till` INTEGER NOT NULL, " +
                                "`freq` REAL NOT NULL, `type` INTEGER NOT NULL, " +
                                "`desc` TEXT, `plac` INTEGER NOT NULL)"
                    )
                    val columns = "id, sinc, till, freq, type, desc, plac"
                    db.execSQL(
                        "INSERT INTO Guess (" + columns + ") SELECT "
                                + columns + " FROM Guess_old;"
                    )
                    db.execSQL("DROP TABLE Guess_old")
                }
            }) // Do not remove migrations so hurriedly! Wait at least for a few months...
            .build()
        val dao = db.dao()
        when (action) {
            // Report
            VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.get(values[0] as Long)
            )?.sendToTarget()

            VIEW_ALL -> handler?.obtainMessage(action, dao.getAll())?.sendToTarget()

            INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.insert(values[0] as Report)
                handler?.obtainMessage(action, result)?.sendToTarget()
            }

            REPLACE_ALL -> if (!values.isNullOrEmpty()) {
                dao.deleteAll(dao.getAll())
                dao.replaceAll(values as List<Report>)
                handler?.obtainMessage(action)?.sendToTarget()
            }

            UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.update(values[0] as Report)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }

            DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.delete(values[0] as Report)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }


            // Crush
            C_VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.cGet(values[0] as String)
            )?.sendToTarget()

            C_VIEW_ALL -> handler?.obtainMessage(action, dao.cGetAll())?.sendToTarget()

            C_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.cInsert(values[0] as Crush)
                handler?.obtainMessage(action, result)?.sendToTarget()
            }

            C_REPLACE_ALL -> if (!values.isNullOrEmpty()) {
                dao.cDeleteAll(dao.cGetAll())
                dao.cReplaceAll(values as List<Crush>)
                handler?.obtainMessage(action)?.sendToTarget()
            }

            C_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.cUpdate(values[0] as Crush)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, values[0]
                )?.sendToTarget()
            }

            C_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.cDelete(values[0] as Crush)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, values[0]
                )?.sendToTarget()
            }


            // Place
            P_VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.pGet(values[0] as Long)
            )?.sendToTarget()

            P_VIEW_ALL -> handler?.obtainMessage(action, dao.pGetAll())?.sendToTarget()

            P_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.pInsert(values[0] as Place)
                handler?.obtainMessage(action, result)?.sendToTarget()
            }

            P_REPLACE_ALL -> if (!values.isNullOrEmpty()) {
                dao.pDeleteAll(dao.pGetAll())
                dao.pReplaceAll(values as List<Place>)
                handler?.obtainMessage(action)?.sendToTarget()
            }

            P_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.pUpdate(values[0] as Place)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }

            P_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                val old = values[0] as Place
                dao.pDelete(old)
                for (mig in dao.getByPlace(old.id))
                    dao.update(mig.apply { plac = values[1] as Long })
                handler?.obtainMessage(
                    action, if (values.size > 2) values[2] as Int else 0,
                    if (values.size > 3) values[3] as Int else 0, null
                )?.sendToTarget()
            }


            // Guess
            G_VIEW_ONE -> if (!values.isNullOrEmpty()) handler?.obtainMessage(
                action,
                if (values.size > 1) values[1] as Int else 0,
                if (values.size > 2) values[2] as Int else 0,
                dao.gGet(values[0] as Long)
            )?.sendToTarget()

            G_VIEW_ALL -> handler?.obtainMessage(action, dao.gGetAll())?.sendToTarget()

            G_INSERT_ONE -> {
                var result: Long = -1
                if (!values.isNullOrEmpty()) result = dao.gInsert(values[0] as Guess)
                handler?.obtainMessage(action, result)?.sendToTarget()
            }

            G_REPLACE_ALL -> if (!values.isNullOrEmpty()) {
                dao.gDeleteAll(dao.gGetAll())
                dao.gReplaceAll(values as List<Guess>)
                handler?.obtainMessage(action)?.sendToTarget()
            }

            G_UPDATE_ONE -> if (!values.isNullOrEmpty()) {
                dao.gUpdate(values[0] as Guess)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }

            G_DELETE_ONE -> if (!values.isNullOrEmpty()) {
                dao.gDelete(values[0] as Guess)
                handler?.obtainMessage(
                    action, if (values.size > 1) values[1] as Int else 0,
                    if (values.size > 2) values[2] as Int else 0, null
                )?.sendToTarget()
            }
        }
        db.close()
    }
}
