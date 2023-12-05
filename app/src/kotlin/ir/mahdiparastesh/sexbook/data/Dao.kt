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
    fun rGetAll(): List<Report>

    @Query("SELECT * FROM Report WHERE plac == :place")
    fun rGetByPlace(place: Long): List<Report>

    @Query("SELECT MAX(time) FROM Report WHERE ogsm LIKE 1")
    suspend fun whenWasTheLastTime(): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun rInsert(item: Report): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun rReplaceAll(list: List<Report>)

    @Update
    suspend fun rUpdate(item: Report)

    @Delete
    suspend fun rDelete(item: Report)

    @Delete
    fun rDeleteAll(item: List<Report>)


    // Crush
    @Query("SELECT * FROM Crush WHERE `key` LIKE :key LIMIT 1")
    fun cGet(key: String): Crush

    @Query("SELECT * FROM Crush")
    fun cGetPeople(): List<Crush>

    @Query("SELECT * FROM Crush WHERE (status & 128) LIKE 0")
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
