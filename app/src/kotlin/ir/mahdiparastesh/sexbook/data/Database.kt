package ir.mahdiparastesh.sexbook.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Main
import java.io.Closeable
import java.io.File

@androidx.room.Database(
    entities = [Report::class, Crush::class, Place::class, Guess::class],
    version = 6, exportSchema = false
)
abstract class Database : RoomDatabase(), Closeable {
    abstract fun dao(): Dao

    class Builder(c: Context) {
        private val room = Room.databaseBuilder(c, Database::class.java, Fun.DATABASE)
            .addMigrations(object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Crush RENAME TO Crush_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Crush` (`key` TEXT NOT NULL, " +
                                "`first_name` TEXT, `middle_name` TEXT, `last_name` TEXT, " +
                                "`status` INTEGER NOT NULL, `birth` TEXT, `height` REAL NOT NULL, " +
                                "`address` TEXT, `first_met` TEXT, `instagram` TEXT, " +
                                "PRIMARY KEY(`key`))"
                    )
                    val cur = db.query("SELECT * FROM Crush_old")
                    while (cur.moveToNext()) {
                        db.execSQL(
                            "INSERT INTO `Crush` (key, first_name, middle_name, last_name, status, " +
                                    "birth, height, address, first_met, instagram) " +
                                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)", arrayOf(
                                cur.getString(0),
                                cur.getString(1), cur.getString(2), cur.getString(3),
                                (cur.getInt(4) + 1) or // gender
                                        (cur.getInt(11) shl 3), // notify_birth
                                cur.getString(5), // birth
                                cur.getFloat(6), // height
                                cur.getString(7), // address
                                cur.getString(9), // first_met
                                cur.getString(8), // instagram
                            )
                        )
                    }
                    cur.close()
                    db.execSQL("DROP TABLE Crush_old")
                }
            })

        fun build(apply: (RoomDatabase.Builder<Database>.() -> Unit)? = null): Database {
            apply?.also { room.apply() }
            return room.build()
        }
    }

    /** Resolves the path to databases. */
    @SuppressLint("SdCardPath")
    class DbFile(which: Triple = Triple.MAIN) : File(
        "/data/data/" + Main::class.java.`package`!!.name + "/databases/" + Fun.DATABASE + which.s
    ) {
        /** Helps resolve the file names of the triple SQLite database files. */
        enum class Triple(val s: String) {
            MAIN(""), SHARED_MEMORY("-shm"), WRITE_AHEAD_LOG("-wal")
        }
    }
}
