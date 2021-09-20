//declaring package & adding imports
package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.databinding.ContentBookmarkInfoBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

//declaring single parameter class
class BookmarkInfoWindowAdapter(val context: Activity) :
    GoogleMap.InfoWindowAdapter {
    //initializing variable
    private val binding =
        ContentBookmarkInfoBinding.inflate(context.layoutInflater)
    //
    override fun getInfoWindow(marker: Marker): View? {
        // This function is required, but can return null if
        // not replacing the entire info window
        return null
    }
    //filling in title and text views on the layout
    override fun getInfoContents(marker: Marker): View? {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        val imageView = binding.photo
        return binding.root
        when (marker.tag) {
            //setting the imageView bitmap
            //directly from the PlaceInfo object
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap(
                    (marker.tag as MapsActivity.PlaceInfo).image)
            }
            //setting the imageView
            //bitmap from the BookmarkMarkerView
            is MapsViewModel.BookmarkView -> {
                val bookMarkview = marker.tag as
                        MapsViewModel.BookmarkView
                //Setting imageView bitmap here
                imageView.setImageBitmap(bookMarkview.getImage(context))
            }
        }
    }
}