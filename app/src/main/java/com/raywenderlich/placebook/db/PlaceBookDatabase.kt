package com.raywenderlich.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raywenderlich.placebook.model.Bookmark

//identifying a Database class to Room
@Database(entities = arrayOf(Bookmark::class), version = 3)
abstract class PlaceBookDatabase : RoomDatabase() {
    //returning a DAO interface
    abstract fun bookmarkDao(): BookmarkDao
    // 3
    companion object {
        // 4
        private var instance: PlaceBookDatabase? = null
        //taking in context & returning single instance
        fun getInstance(context: Context): PlaceBookDatabase {
            if (instance == null) {
                //if first instance, creating single instance &
                    //creating Room database
                instance = Room.databaseBuilder(context.applicationContext,
                    PlaceBookDatabase::class.java, "PlaceBook")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            //returning instance
            return instance as PlaceBookDatabase
        }
    }
}