package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

//defining class with required Context Object
class BookmarkRepo(private var context: Context) {
    //defining properties
    val categories: List<String>
        get() = ArrayList(allCategories.keys)
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    private var categoryMap: HashMap<Place.Type, String> =
            buildCategoryMap()
    private var allCategories: HashMap<String, Int> =
            buildCategories()
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

    private fun buildCategoryMap() : HashMap<Place.Type, String> {
        return hashMapOf(
                Place.Type.BAKERY to "Restaurant",
                Place.Type.BAR to "Restaurant",
                Place.Type.CAFE to "Restaurant",
                Place.Type.FOOD to "Restaurant",
                Place.Type.RESTAURANT to "Restaurant",
                Place.Type.MEAL_DELIVERY to "Restaurant",
                Place.Type.MEAL_TAKEAWAY to "Restaurant",
                Place.Type.GAS_STATION to "Gas",
                Place.Type.CLOTHING_STORE to "Shopping",
                Place.Type.DEPARTMENT_STORE to "Shopping",
                Place.Type.FURNITURE_STORE to "Shopping",
                Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
                Place.Type.HARDWARE_STORE to "Shopping",
                Place.Type.HOME_GOODS_STORE to "Shopping",
                Place.Type.JEWELRY_STORE to "Shopping",
                Place.Type.SHOE_STORE to "Shopping",
                Place.Type.SHOPPING_MALL to "Shopping",
                Place.Type.STORE to "Shopping",
                Place.Type.LODGING to "Lodging",
                Place.Type.ROOM to "Lodging"
        )
    }

    fun placeTypeToCategory(placeType: Place.Type): String {
        var category = "Other"
        if (categoryMap.containsKey(placeType)) {
            category = categoryMap[placeType].toString()
        }
        return category
    }

    private fun buildCategories() : HashMap<String, Int> {
        return hashMapOf(
                "Gas" to R.drawable.ic_gas,
                "Lodging" to R.drawable.ic_lodging,
                "Other" to R.drawable.ic_other,
                "Restaurant" to R.drawable.ic_restaurant,
                "Shopping" to R.drawable.ic_shopping
        )
    }

    fun getCategoryResourceId(placeCategory: String): Int? {
        return allCategories[placeCategory]
    }

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }
    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }

    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> =
        bookmarkDao.loadLiveBookmark(bookmarkId)

}