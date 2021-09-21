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

    override fun getInfoWindow(marker: Marker): View? {
        //this function is required, but can return null if
        //not replacing entire info window
        return null
    }
    //filling in title & text views on layout
    override fun getInfoContents(marker: Marker): View? {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        val imageView = binding.photo
        when (marker.tag) {
            //setting imageView bitmap directly from PlaceInfo object
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap(
                        (marker.tag as MapsActivity.PlaceInfo).image)
            }
            //setting imageView bitmap from bookmarkview
            is MapsViewModel.BookmarkView -> {
                val bookMarkview = marker.tag as
                        MapsViewModel.BookmarkView
                //setting imageView bitmap
                imageView.setImageBitmap(bookMarkview.getImage(context))
            }
        }
        return binding.root
    }
}