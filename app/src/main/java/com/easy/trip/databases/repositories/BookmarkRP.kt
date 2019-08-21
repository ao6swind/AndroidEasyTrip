package com.easy.trip.databases.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.easy.trip.databases.EasyTripDatabase
import com.easy.trip.models.Bookmark

class BookmarkRP(context: Context){
    private var db: EasyTripDatabase = EasyTripDatabase.getInstance(context)

    val bookmarks: LiveData<List<Bookmark>> get() { return db.bookmarkDao().list() }

    fun addBookmark(bookmark: Bookmark): Long {
        return db.bookmarkDao().insert(bookmark)
    }

    fun editBookmark(bookmark: Bookmark) {
        db.bookmarkDao().update(bookmark)
    }

    fun findBookmarkByPlace(placeId: String): Bookmark? {
        return db.bookmarkDao().findByPlace(placeId)
    }

    fun deleteBookmark(bookmark: Bookmark) {
        db.bookmarkDao().delete(bookmark)
    }
}