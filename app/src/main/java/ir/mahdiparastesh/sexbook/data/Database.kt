package ir.mahdiparastesh.sexbook.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Report::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao
}