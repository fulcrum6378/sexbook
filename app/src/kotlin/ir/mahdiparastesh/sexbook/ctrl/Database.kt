package ir.mahdiparastesh.sexbook.ctrl

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report

@androidx.room.Database(
    entities = [Report::class, Crush::class, Place::class, Guess::class],
    version = 10, exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao

    class Builder(c: Context) {
        private val room = Room.databaseBuilder(c, Database::class.java, DATABASE)
        /*.addMigrations(object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        })*/

        fun build(apply: (RoomDatabase.Builder<Database>.() -> Unit)? = null): Database {
            apply?.also { room.apply() }
            return room.build()
        }
    }

    companion object {
        const val DATABASE = "sexbook.db"
    }
}
