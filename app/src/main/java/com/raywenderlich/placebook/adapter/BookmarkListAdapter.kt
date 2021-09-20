package com.raywenderlich.placebook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.BookmarkItemBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

//defining class & class properties
class BookmarkListAdapter(
        private var bookmarkData: List<MapsViewModel.BookmarkView>?,
        private val mapsActivity: MapsActivity
) : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {
    //defining viewholder class to hold widgets
    class ViewHolder(
            val binding: BookmarkItemBinding,
            private val mapsActivity: MapsActivity
    ) : RecyclerView.ViewHolder(binding.root) {init {
        binding.root.setOnClickListener {
            val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
            mapsActivity.moveToBookmark(bookmarkView)
        }
    }

    }
    //assigning bookmarks & refreshing recycler view
    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>) {
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }
    //creating a viewholder
    override fun onCreateViewHolder(parent: ViewGroup, viewType:
    Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = BookmarkItemBinding.inflate(layoutInflater,
                parent, false)
        return ViewHolder(binding, mapsActivity)
    }
    override fun onBindViewHolder(holder: ViewHolder, position:
    Int) {
        //making sure bookmarkdata is not null before binding
        bookmarkData?.let { list->
            //assigning bookmark data for the current item position
            val bookmarkViewData = list[position]
            //references being populated from bookmarkview data
            holder.binding.root.tag = bookmarkViewData
            holder.binding.bookmarkData = bookmarkViewData

            bookmarkViewData.categoryResourceId?.let {
                holder.binding.bookmarkIcon.setImageResource(it)
            }
        }
    }

    //returning number of items in the bookmark data list
    override fun getItemCount() = bookmarkData?.size ?: 0
}
