package ir.mahdiparastesh.sexbook.ctrl

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import java.io.File

@androidx.room.Database(
    entities = [Report::class, Crush::class, Place::class, Guess::class],
    version = 8, exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao

    class Builder(c: Context) {
        private val room = Room.databaseBuilder(c, Database::class.java, DATABASE)
            .addMigrations(object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Report RENAME TO Report_old")
                    db.execSQL(
                        "CREATE TABLE `Report` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`time` INTEGER NOT NULL, " +
                                "`name` TEXT, " +
                                "`type` INTEGER NOT NULL, " +
                                "`desc` TEXT, " +
                                "`accu` INTEGER NOT NULL, " +
                                "`plac` INTEGER NOT NULL, " +
                                "`ogsm` INTEGER NOT NULL)"
                    )
                    db.execSQL(
                        "INSERT INTO Report (id, time, name, type, desc, accu, plac, ogsm) " +
                                "SELECT id, time, name, type, desc, accu, plac, ogsm FROM Report_old"
                    )
                    db.execSQL("DROP TABLE Report_old")
                }
            }, object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {

                    // Report
                    db.execSQL("ALTER TABLE Report RENAME TO Report_old")
                    db.execSQL(
                        "CREATE TABLE `Report` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`time` INTEGER NOT NULL, " +
                                "`name` TEXT, " +
                                "`type` INTEGER NOT NULL DEFAULT 1, " +
                                "`place` INTEGER NOT NULL DEFAULT -1, " +
                                "`description` TEXT, " +
                                "`accurate` INTEGER NOT NULL DEFAULT 1, " +
                                "`orgasmed` INTEGER NOT NULL DEFAULT 1)"
                    )
                    db.execSQL(
                        "INSERT INTO Report (id, time, name, type, place, description, " +
                                "accurate, orgasmed) " +
                                "SELECT id, time, name, type, plac, desc, accu, ogsm FROM Report_old"
                    )
                    db.execSQL("DROP TABLE Report_old")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_Report_time` ON `Report` (`time`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_Report_place` ON `Report` (`place`)")

                    // Crush
                    db.execSQL("ALTER TABLE Crush RENAME TO Crush_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Crush` (" +
                                "`key` TEXT NOT NULL, " +
                                "`first_name` TEXT, " +
                                "`middle_name` TEXT, " +
                                "`last_name` TEXT, " +
                                "`status` INTEGER NOT NULL DEFAULT 0, " +
                                "`birthday` TEXT, " +
                                "`height` REAL NOT NULL DEFAULT -1.0, " +
                                "`body` INTEGER NOT NULL DEFAULT 0, " +
                                "`address` TEXT, " +
                                "`first_met` TEXT, " +
                                "`instagram` TEXT, " +
                                "`hue` REAL, " +
                                "PRIMARY KEY(`key`))"
                    )
                    db.execSQL(
                        "INSERT INTO Crush (key, first_name, middle_name, last_name, status, " +
                                "birthday, height, body, address, first_met, instagram) " +
                                "SELECT key, first_name, middle_name, last_name, status, " +
                                "birth, height, body, address, first_met, instagram FROM Crush_old"
                    )
                    db.execSQL("DROP TABLE Crush_old")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_Crush_status` ON `Crush` (`status`)")

                    // Place
                    db.execSQL("ALTER TABLE Place RENAME TO Place_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Place` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`name` TEXT, " +
                                "`latitude` REAL, " +
                                "`longitude` REAL)"
                    )
                    db.execSQL(
                        "INSERT INTO Place (id, name, latitude, longitude) " +
                                "SELECT id, name, latitude, longitude FROM Place_old"
                    )
                    db.execSQL("DROP TABLE Place_old")

                    // Guess
                    db.execSQL("ALTER TABLE Guess RENAME TO Guess_old")
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `Guess` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`name` TEXT, " +
                                "`since` INTEGER NOT NULL DEFAULT -1, " +
                                "`until` INTEGER NOT NULL DEFAULT -1, " +
                                "`frequency` REAL NOT NULL DEFAULT 0.0, " +
                                "`type` INTEGER NOT NULL DEFAULT 1, " +
                                "`place` INTEGER NOT NULL DEFAULT -1, " +
                                "`description` TEXT, " +
                                "`active` INTEGER NOT NULL DEFAULT 1)"
                    )
                    db.execSQL(
                        "INSERT INTO Guess (id, name, since, until, frequency, type, " +
                                "place, description, active) " +
                                "SELECT id, crsh, sinc, till, freq, type, plac, desc, able FROM Guess_old"
                    )
                    db.execSQL("DROP TABLE Guess_old")
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
        "/data/data/" + Sexbook::class.java.`package`!!.name + "/databases/" + DATABASE + which.s
    ) {
        /** Helps resolve the file names of the triple SQLite database files. */
        enum class Triple(val s: String) {
            MAIN(""), SHARED_MEMORY("-shm"), WRITE_AHEAD_LOG("-wal")
        }
    }

    companion object {
        const val DATABASE = "sexbook.db"
    }
}
