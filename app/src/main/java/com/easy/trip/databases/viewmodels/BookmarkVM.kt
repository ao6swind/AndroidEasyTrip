package com.easy.trip.databases.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.easy.trip.databases.repositories.BookmarkRP
import com.easy.trip.models.Bookmark
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkVM(application: Application): AndroidViewModel(application) {
    private var bookmarkRP: BookmarkRP = BookmarkRP(getApplication())
    private lateinit var bookmarks:LiveData<List<Bookmark>>

    fun findBookmarkByPlace(placeId: String): Bookmark? {
        var bookmark: Bookmark? = null
        GlobalScope.launch {
            bookmark = bookmarkRP.findBookmarkByPlace(placeId)
        }
        return bookmark
    }

    fun listBookmarks(): LiveData<List<Bookmark>> {
        bookmarks = bookmarkRP.bookmarks;
        return bookmarks
    }

    fun addBookmark(bookmark: Bookmark, comment: String) {
        GlobalScope.launch {
            bookmark.comment = comment
            bookmarkRP.addBookmark(bookmark)
        }
    }

    fun editBookmark(bookmark: Bookmark) {
        GlobalScope.launch {
            bookmarkRP.editBookmark(bookmark)
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        GlobalScope.launch {
            bookmarkRP.deleteBookmark(bookmark)
        }
    }
}