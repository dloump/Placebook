package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo

//defining class which inherits from AndroidViewModel
class MapsViewModel(application: Application) :
    AndroidViewModel(application) {
    private var bookmarks: LiveData<List<BookmarkMarkerView>>? =
        null
    private val TAG = "MapsViewModel"
    //creating bookmarkRepo object
    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(
        getApplication())
    //taking a Google Place & Bitmap image
    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        //creating empty bookmark object & filling with Place data
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        //saving bookmark to repository
        val newId = bookmarkRepo.addBookmark(bookmark)
        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    //creating method to initialize & return the bookmark marker views to
    //the MapsActivity
    fun getBookmarkMarkerViews() :
            LiveData<List<BookmarkMarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToMarkerView()
        }
        return bookmarks
    }

    private fun bookmarkToMarkerView(bookmark: Bookmark) =
        BookmarkMarkerView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude))

    private fun mapBookmarksToMarkerView() {
        //dynamically mapping bookmark objects into BookmarkMarkerView objects
        //as they're updated in the database
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks)
        { repoBookmarks ->
            //provided List of bookmarks returned from repo
            //to be stored in the bookmarks variable
            repoBookmarks.map { bookmark ->
                bookmarkToMarkerView(bookmark)
            }
        }
    }

    data class BookmarkMarkerView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0))

}