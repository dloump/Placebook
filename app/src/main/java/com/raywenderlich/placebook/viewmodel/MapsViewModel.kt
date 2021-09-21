package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
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
    private var bookmarks: LiveData<List<BookmarkView>>? =
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
        image?.let { bookmark.setImage(it, getApplication()) }
    }

    //creating method to initialize & return bookmark marker views to MapsActivity
    fun getBookmarkViews() :
            LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark):
            BookmarkView {
        return BookmarkView(
                bookmark.id,
                LatLng(bookmark.latitude, bookmark.longitude),
                bookmark.name,
                bookmark.phone,
                bookmarkRepo.getCategoryResourceId(bookmark.category))
    }

    private fun mapBookmarksToBookmarkView() {
        //dynamically mapping bookmark objects into BookmarkMarkerView objects
        //as updated in database
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks)
        { repoBookmarks ->
            //provided List of bookmarks returned from repo
            //to be stored in bookmarks variable
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    private fun getPlaceCategory(place: Place): String {
        //setting category default to Other for places with unassigned types
        var category = "Other"
        val types = place.types
        types?.let { placeTypes ->
            //checking placetypes list to see if it's populated
            if (placeTypes.size > 0) {
                //if populated, extracting first type from List & calling
                    //placeTypeToCategory() to make conversion
                val placeType = placeTypes[0]
                category = bookmarkRepo.placeTypeToCategory(placeType)
            }
        }
        //returning category
        return category
    }

    data class BookmarkView(val id: Long? = null,
                            val location: LatLng = LatLng(0.0, 0.0),
                            val name: String = "",
                            val phone: String = "",
                            val categoryResourceId: Int? = null) {
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context,
                Bookmark.generateImageFilename(it))
        }
    }

}