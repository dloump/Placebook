package com.raywenderlich.placebook.db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import com.raywenderlich.placebook.model.Bookmark

//BookmarkDao "defines" CRUD:
//~C: Create-Create new objects in the database
//~R: Read-Read objects from the database
//~U: Update-Update objects in the database
//~D: Delete. Delete objects in the database

//"telling" Room this is a Data Access Object (DAOs must be: interface or abstract)
@Dao
interface BookmarkDao {
    //defining an SQL statement to read all bookmarks from database & return them as a List
    //wrapping them with LiveData to notify observing objects when data changes
    @Query("SELECT * FROM Bookmark")
    fun loadAll(): LiveData<List<Bookmark>>
    //returning a single bookmark object
    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadBookmark(bookmarkId: Long): Bookmark
    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadLiveBookmark(bookmarkId: Long): LiveData<Bookmark>
    //saving a single bookmark to the database & returning the new
    //primary key associated with the new bookmark
    @Insert(onConflict = IGNORE)
    fun insertBookmark(bookmark: Bookmark): Long
    //updating a single bookmark, set to REPLACE so that
    //existing bookmark is replaced with new bookmark data
    @Update(onConflict = REPLACE)
    fun updateBookmark(bookmark: Bookmark)
    //deletes an existing bookmark
    @Delete
    fun deleteBookmark(bookmark: Bookmark)
}