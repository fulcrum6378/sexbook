package com.mahdiparastesh.mbcounter.data

import androidx.room.*
import androidx.room.Dao

@Dao
interface Dao {
    @Query("SELECT * FROM report WHERE id LIKE :id LIMIT 1")
    fun get(id: Long): Report

    @Query("SELECT * FROM report")
    fun getAll(): List<Report>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: Report): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(list: List<Report>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replace(item: Report)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replaceAll(list: List<Report>)


    @Update
    fun update(item: Report)

    @Update
    fun updateAll(list: List<Report>)


    @Delete
    fun delete(item: Report)

    @Delete
    fun deleteAll(item: List<Report>)
}