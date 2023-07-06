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
    version = 5, exportSchema = false
)
abstract class Database : RoomDatabase(), Closeable {
    abstract fun dao(): Dao

    class Builder(c: Context) {
        private val room = Room.databaseBuilder(c, Database::class.java, Fun.DATABASE)
            .addMigrations(object : Migration(2, 3) {
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
            }, object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Guess ADD COLUMN able INTEGER NOT NULL DEFAULT 1")
                }
            }, object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Crush RENAME TO Crush_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Crush` (`key` TEXT NOT NULL, " +
                                "`first_name` TEXT, `middle_name` TEXT, `last_name` TEXT, " +
                                "`gender` INTEGER NOT NULL, `birth` TEXT, `height` REAL NOT NULL, " +
                                "`address` TEXT, `instagram` TEXT, `first_met` TEXT, " +
                                "`notify_birth` INTEGER NOT NULL, PRIMARY KEY(`key`))"
                    )
                    val cur = db.query("SELECT * FROM Crush_old")
                    while (cur.moveToNext()) {
                        val bYear = cur.getInt(6)
                        val bMonth = cur.getInt(7)
                        val bDay = cur.getInt(8)
                        db.execSQL(
                            "INSERT INTO `Crush` (key, first_name, middle_name, last_name, gender, " +
                                    "birth, height, address, instagram, first_met, notify_birth) " +
                                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)", arrayOf(
                                cur.getString(0),
                                cur.getString(1), cur.getString(2), cur.getString(3),
                                cur.getInt(4),
                                if (bYear != -1 && bMonth != -1 && bDay != -1)
                                    "$bYear.${bMonth + 1}.$bDay" else null,
                                cur.getFloat(5), cur.getString(9), cur.getString(10), null,
                                cur.getInt(11)
                            )
                        )
                    }
                    cur.close()
                    db.execSQL("DROP TABLE Crush_old")
                }
            }) // Do not remove migrations so hurriedly! Wait at least for a few months...

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
