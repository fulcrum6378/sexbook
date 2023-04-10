package ir.mahdiparastesh.sexbook.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.mahdiparastesh.sexbook.Main
import java.io.File

@androidx.room.Database(
    entities = [Report::class, Crush::class, Place::class, Guess::class],
    version = 4, exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao

    companion object {
        const val DATABASE = "sexbook.db"

        fun build(c: Context) = Room.databaseBuilder(c, Database::class.java, DATABASE)
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
            }, object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Guess ADD COLUMN able INTEGER NOT NULL DEFAULT 1")
                    /* SQLiteException: duplicate column name: able (code 1 SQLITE_ERROR[1]): ,
                     * while compiling: ALTER TABLE Guess ADD COLUMN able INTEGER NOT NULL DEFAULT 1
                     * Apparently a new error because of the recent updates in Room.
                     * Someone said it's an error in androidx.work! Our Work class is not associated
                     * with coroutines. Presumably it's a coincident error both in Sexbook and
                     * androidx.work or a new error in Room! */
                }
            }) // Do not remove migrations so hurriedly! Wait at least for a few months...
            .build()
    }

    @SuppressLint("SdCardPath")
    class DbFile(which: Triple = Triple.MAIN) : File(
        "/data/data/" + Main::class.java.`package`!!.name + "/databases/" + DATABASE + which.s
    ) {
        enum class Triple(val s: String) {
            MAIN(""), SHARED_MEMORY("-shm"), WRITE_AHEAD_LOG("-wal")
        }
    }
}
