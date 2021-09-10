package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.repository.BookmarkRepo

//defining class which inherits from AndroidViewModel
class MapsViewModel(application: Application) :
    AndroidViewModel(application) {
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
}