package com.easy.trip.databases.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.easy.trip.models.Bookmark
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface BookmarkDAO{
    @Query("SELECT * FROM Bookmarks")
    fun list():LiveData<List<Bookmark>>

    @Query("SELECT * FROM Bookmarks WHERE id = :id")
    fun find(id: Int): Bookmark

    @Query("SELECT * FROM Bookmarks WHERE placeId = :placeId")
    fun findByPlace(placeId: String): Bookmark

    @Insert(onConflict = IGNORE)
    fun insert(bookmark: Bookmark): Long

    @Update(onConflict = REPLACE)
    fun update(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)
}