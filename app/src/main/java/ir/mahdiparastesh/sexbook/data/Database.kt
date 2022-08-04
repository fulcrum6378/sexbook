package ir.mahdiparastesh.sexbook.data

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase
import ir.mahdiparastesh.sexbook.Main
import java.io.File

@Database(
    entities = [Report::class, Crush::class, Place::class, Guess::class],
    version = 4, exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao

    @SuppressLint("SdCardPath")
    class DbFile(which: Triple) : File(
        "/data/data/" + Main::class.java.`package`!!.name + "/databases/" + DATABASE + which.s
    ) {
        companion object {
            const val DATABASE = "sexbook.db"
        }

        enum class Triple(val s: String) {
            MAIN(""), SHARED_MEMORY("-shm"), WRITE_AHEAD_LOG("-wal")
        }
    }
}
