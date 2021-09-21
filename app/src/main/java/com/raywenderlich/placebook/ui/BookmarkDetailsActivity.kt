package com.raywenderlich.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.ActivityBookmarkDetailsBinding
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import java.io.File

class BookmarkDetailsActivity : AppCompatActivity(),
        PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private var photoFile: File? = null
    private lateinit var databinding:
            ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView:
            BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?)
    {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this,
            R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    private fun setupToolbar() {
        setSupportActionBar(databinding.toolbar)
    }

    override fun onCaptureClick() {
        //clearing previously assigned photofiles
        photoFile = null
        try {
            //creating uniquely named imagefile & assigning it to photofile
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException) {
            //if an exception is thrown, method returns after doing nothing
            return
        }
        //making sure photoFile is not null before continuing with method
        photoFile?.let { photoFile ->
            //getting a Uri for temporary photo file
            val photoUri = FileProvider.getUriForFile(this,
                    "com.raywenderlich.placebook.fileprovider",
                    photoFile)
            //creating new Intent
            val captureIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //letting Intent know where to save the full-size image captured by User
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                    photoUri)
            //giving temporary write permissions on photouri to Intent
            val intentActivities = packageManager.queryIntentActivities(
                    captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                    .forEach { grantUriPermission(it, photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
            //invoking Intent & passing request code
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }
    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    private fun getImageWithAuthority(uri: Uri) =
            ImageUtils.decodeUriStreamToSize(
                    uri,
                    resources.getDimensionPixelSize(R.dimen.default_image_width),
                    resources.getDimensionPixelSize(R.dimen.default_image_height),
                    this
            )

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                databinding.imageViewPlace.setImageBitmap(placeImage)
            }
        }
        databinding.imageViewPlace.setOnClickListener {
            replaceImage()
        }
    }

    private fun getIntentData() {
        //pulling bookmarkid from intent data
        val bookmarkId = intent.getLongExtra(
            MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)
        //retrieving BookmarkDetailsView from BookmarkDetailsViewModel &
        //then observing it for changes
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this,
            {
                //assigning bookmarkDetailsView property & populating bookmark
                //fields from data
                it?.let {
                    bookmarkDetailsView = it
                    //setting databinding's variable
                    databinding.bookmarkDetailsView = it
                    populateImageView()
                    populateCategoryList()
                }
            })
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu):
            Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    private fun saveChanges() {
        val name = databinding.editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = databinding.editTextName.text.toString()
            bookmarkView.notes =
                databinding.editTextNotes.text.toString()
            bookmarkView.address =
                databinding.editTextAddress.text.toString()
            bookmarkView.phone =
                databinding.editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    private fun updateImage(image: Bitmap) {
        bookmarkDetailsView?.let {
            databinding.imageViewPlace.setImageBitmap(image)
            it.setImage(this, image)
        }
    }

    private fun getImageWithPath(filePath: String) =
            ImageUtils.decodeFileToSize(
                    filePath,
                    resources.getDimensionPixelSize(R.dimen.default_image_width),
                    resources.getDimensionPixelSize(R.dimen.default_image_height)
            )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //checking to make sure User didn’t cancel photo capture
        if (resultCode == android.app.Activity.RESULT_OK) {
            //checking to see which call is returning a result
            when (requestCode) {
                //if requestCode matches REQUEST_CAPTURE_IMAGE, processing
                //continues
                REQUEST_CAPTURE_IMAGE -> {
                    //returning early from the method if no photoFile defined
                    val photoFile = photoFile ?: return
                    //revoking permissions
                    val uri = FileProvider.getUriForFile(this,
                            "com.raywenderlich.placebook.fileprovider",
                            photoFile)
                    revokeUriPermission(uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    //getting image from new photo path & updating bookmark image
                    val image = getImageWithPath(photoFile.absolutePath)
                    val bitmap = ImageUtils.rotateImageIfRequired(this,
                            image , uri)
                    updateImage(bitmap)
                }
                //
                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null)
                {
                    val imageUri = data.data as Uri
                    val image = getImageWithAuthority(imageUri)
                    image?.let {
                        val bitmap = ImageUtils.rotateImageIfRequired(this, it,
                                imageUri)
                        updateImage(bitmap)
                    }
                }
            }
        }
    }

    private fun populateCategoryList() {
        //returns immediately if bookmarkDetailsView is null
        val bookmarkView = bookmarkDetailsView ?: return
        //retrieving the category icon resourceId from view model
        val resourceId =
                bookmarkDetailsViewModel.getCategoryResourceId(bookmarkView.category)
        //if resourceId is not null, updating imageViewCategory to category icon
        resourceId?.let{ databinding.imageViewCategory.setImageResource(it) }
        //retrieving list of categories from view model
        val categories = bookmarkDetailsViewModel.getCategories()
        //creating adapter & assigning Adapter to built-in Layout resource
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, categories)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //assigning Adapter to spinnerCategory control
        databinding.spinnerCategory.adapter = adapter
        //updating spinnerCategory to reflect current category selection
        val placeCategory = bookmarkView.category

        databinding.spinnerCategory.setSelection(adapter.getPosition(placeCategory))
    }

    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}