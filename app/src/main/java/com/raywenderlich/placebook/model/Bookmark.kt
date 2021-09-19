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
    // 1
    fun setImage(image: Bitmap, context: Context) {
        // 2
        id?.let {
            ImageUtils.saveBitmapToFile(context, image,
                generateImageFilename(it))
        }
    }
    //3
    companion object {
        fun generateImageFilename(id: Long): String {
            // 4
            return "bookmark$id.png"
        }
    }
}