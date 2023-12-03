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

    @Query("SELECT * FROM Report WHERE plac == :place")
    fun getByPlace(place: Long): List<Report>

    @Query("SELECT MAX(time) FROM Report")
    fun whenWasTheLastTime(): Long?

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
    @Query("SELECT * FROM Crush WHERE `key` LIKE :key LIMIT 1")
    fun cGet(key: String): Crush

    @Query("SELECT * FROM Crush")
    fun cGetPeople(): List<Crush>

    @Query("SELECT * FROM Crush WHERE (status & 128) == 0")
    fun cGetAll(): List<Crush>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun cInsert(item: Crush): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun cReplaceAll(list: List<Crush>)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun pReplaceAll(list: List<Place>)

    @Update
    fun pUpdate(item: Place)

    @Delete
    fun pDelete(item: Place)

    @Delete
    fun pDeleteAll(item: List<Place>)


    // Guess
    @Query("SELECT * FROM Guess WHERE id LIKE :id LIMIT 1")
    fun gGet(id: Long): Guess

    @Query("SELECT * FROM Guess")
    fun gGetAll(): List<Guess>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun gInsert(item: Guess): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun gReplaceAll(list: List<Guess>)

    @Update
    fun gUpdate(item: Guess)

    @Delete
    fun gDelete(item: Guess)

    @Delete
    fun gDeleteAll(item: List<Guess>)
}
