package ir.mahdiparastesh.sexbook.data

import androidx.room.*
import androidx.room.Dao

@Dao
interface Dao {
    // Report
    @Query("SELECT * FROM Report WHERE id LIKE :id LIMIT 1")
    fun get(id: Long): Report

    @Query("SELECT * FROM Report")
    fun getAll(): List<Report>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: Report): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replaceAll(list: List<Report>)

    @Update
    fun update(item: Report)

    @Delete
    fun delete(item: Report)

    @Delete
    fun deleteAll(item: List<Report>)


    // Crush
    @Query("SELECT * FROM Crush WHERE key LIKE :key LIMIT 1")
    fun cGet(key: String): Crush

    @Query("SELECT * FROM Crush")
    fun cGetAll(): List<Crush>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun cReplaceAll(list: List<Crush>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun cInsert(item: Crush): Long

    @Update
    fun cUpdate(item: Crush)

    @Delete
    fun cDelete(item: Crush)

    @Delete
    fun cDeleteAll(item: List<Crush>)


    // Place
    @Query("SELECT * FROM Place WHERE id LIKE :id LIMIT 1")
    fun pGet(id: Long): Place

    @Query("SELECT * FROM Place")
    fun pGetAll(): List<Place>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun pInsert(item: Place): Long

    @Update
    fun pUpdate(item: Place)
}
