package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

//defining class with required Context Object
class BookmarkRepo(context: Context) {
    //defining properties
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    //adding single bookmark to repo,
    //returns unique id of newly saved bookmark or null if unable to be saved
    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }
    //returning freshly initialized bookmark Object
    fun createBookmark(): Bookmark {
        return Bookmark()
    }
    //returning LiveData List of all bookmarks in repository
    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }
}