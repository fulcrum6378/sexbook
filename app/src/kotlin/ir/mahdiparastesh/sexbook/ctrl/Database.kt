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
    version = 7, exportSchema = false
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
