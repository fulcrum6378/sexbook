package ir.mahdiparastesh.sexbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface Dao {

    // Report
    @Query("SELECT * FROM Report")
    suspend fun rGetAll(): List<Report>

    @Query("SELECT * FROM Report WHERE plac == :place")
    suspend fun rGetByPlace(place: Long): List<Report>

    @Query("SELECT MAX(time) FROM Report WHERE ogsm LIKE 1")
    suspend fun whenWasTheLastTime(): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun rInsert(item: Report): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun rReplaceAll(list: List<Report>)

    @Update
    suspend fun rUpdate(item: Report)

    @Delete
    suspend fun rDelete(item: Report)

    @Delete
    suspend fun rDeleteAll(item: List<Report>)


    // Crush
    @Query("SELECT * FROM Crush")
    suspend fun cGetAll(): List<Crush>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun cInsert(item: Crush): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cReplaceAll(list: List<Crush>)

    @Update
    suspend fun cUpdate(item: Crush)

    @Delete
    suspend fun cDelete(item: Crush)

    @Delete
    suspend fun cDeleteAll(item: List<Crush>)


    // Place
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

    @Delete
    suspend fun pDeleteAll(item: List<Place>)


    // Guess
    @Query("SELECT * FROM Guess")
    suspend fun gGetAll(): List<Guess>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun gInsert(item: Guess): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun gReplaceAll(list: List<Guess>)

    @Update
    suspend fun gUpdate(item: Guess)

    @Delete
    suspend fun gDelete(item: Guess)

    @Delete
    suspend fun gDeleteAll(item: List<Guess>)
}
