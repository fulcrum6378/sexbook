package ir.mahdiparastesh.sexbook.ctrl

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Guess
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report

@Dao
interface Dao {

    /* --- Report --- */

    @Query("SELECT * FROM Report")
    suspend fun rGetAll(): List<Report>

    @Query("SELECT * FROM Report WHERE `plac` == :place")
    suspend fun rGetByPlace(place: Long): List<Report>

    @Query("SELECT MAX(time) FROM Report WHERE `ogsm` LIKE 1")
    suspend fun whenWasTheLastTime(): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun rInsert(item: Report): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun rReplaceAll(list: List<Report>)

    @Update
    suspend fun rUpdate(item: Report)

    @Delete
    suspend fun rDelete(item: Report)

    @Query("DELETE FROM Report")
    suspend fun rDeleteAll()


    /* --- Crush --- */

    @Query("SELECT * FROM Crush")
    suspend fun cGetAll(): List<Crush>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun cInsert(item: Crush): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cReplaceAll(list: List<Crush>)

    @Query("UPDATE OR ABORT Crush SET `key` = :newKey WHERE `key` = :oldKey")
    suspend fun cUpdateKey(oldKey: String, newKey: String)

    @Update
    suspend fun cUpdate(item: Crush)

    @Delete
    suspend fun cDelete(item: Crush)

    @Query("DELETE FROM Crush")
    suspend fun cDeleteAll()


    /* --- Place --- */

    @Query("SELECT * FROM Place")
    suspend fun pGetAll(): List<Place>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun pInsert(item: Place): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pReplaceAll(list: List<Place>)

    @Update
    suspend fun pUpdate(item: Place)

    @Delete
    suspend fun pDelete(item: Place)

    @Query("DELETE FROM Place")
    suspend fun pDeleteAll()


    /* --- Guess --- */

    @Query("SELECT * FROM Guess")
    suspend fun gGetAll(): List<Guess>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun gInsert(item: Guess): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun gReplaceAll(list: List<Guess>)

    @Update
    suspend fun gUpdate(item: Guess)

    @Delete
    suspend fun gDelete(item: Guess)

    @Query("DELETE FROM Guess")
    suspend fun gDeleteAll()
}
