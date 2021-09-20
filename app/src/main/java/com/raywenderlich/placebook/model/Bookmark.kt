package com.raywenderlich.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

//informing Room this is a database entity class
@Entity
//constructing Bookmark class
data class Bookmark(
    //defining required id property &
    //"telling" Room to generate incrementing numbers
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    //defining fields with default values
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes: String = ""
)

{
    //providing public interface for saving an image for a bookmark
    fun setImage(image: Bitmap, context: Context) {
        //if bookmark has an id, saving image to a file
        id?.let {
            ImageUtils.saveBitmapToFile(context, image,
                generateImageFilename(it))
        }
    }
    //allowing another object to load an image without loading bookmark from database
    companion object {
        fun generateImageFilename(id: Long): String {
            //returns a filename based on a bookmark id
            return "bookmark$id.png"
        }
    }
}