package com.raywenderlich.placebook.ui

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.adapter.BookmarkListAdapter
import com.raywenderlich.placebook.databinding.ActivityMapsBinding
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var markers = HashMap<Long, Marker>()
    private lateinit var bookmarkListAdapter: BookmarkListAdapter
    private val mapsViewModel by viewModels<MapsViewModel>()
    private lateinit var databinding: ActivityMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(databinding.root)

        // Obtain SupportMapFragment & get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupToolbar()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        createBookmarkObserver()
        getCurrentLocation()
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext,
            getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupLocationClient() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    //adding runtime permissions
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {
        //checking if fine location permission is granted
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            //if not granted permission
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true
            //requesting to be notified when location is ready
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    //if location is not null, creating object for map location
                    val latLng = LatLng(location.latitude,
                        location.longitude)
                    //creating CameraUpdate object to specify how map camera is updated
                    val update = CameraUpdateFactory.newLatLngZoom(latLng,
                        16.0f)
                    //updating camera with CameraUpdate object
                    map.moveCamera(update)
                } else {
                    //if result is null, log error message
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    //defining callback method to handle User's response to permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        showProgress()
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest:
                                       PointOfInterest) {
        val placeId = pointOfInterest.placeId
        val placeFields = listOf(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
                Place.Field.TYPES)
        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode)
                    hideProgress()
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        //getting photoMetadata from array for selected place
        val photoMetadata = place
            .getPhotoMetadatas()?.get(0)
        //if no photo for place, skip to next step
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        //creating FetchPhotoRequest
        val photoRequest = FetchPhotoRequest
            .builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(
                R.dimen.default_image_width
            ))
            .setMaxHeight(resources.getDimensionPixelSize(
                R.dimen.default_image_height
            ))
            .build()
        //calling fetchPhoto, if successful, assigning photo to bitmap
        //otherwise checking if API error occurred & logging error
        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode)
                    hideProgress()
                }
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?)
    {
        hideProgress()
        val marker = map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
        databinding.mainMapView.fab.setOnClickListener {
            searchAtCurrentLocation()
        }
        map.setOnMapLongClickListener { latLng ->
            newBookmark(latLng)
        }
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
            is PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place,
                            placeInfo.image)
                    }
                }
                marker.remove();
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkMarkerView = (marker.tag as
                        MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    //adds single blue marker to map based on a BookmarkView
    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookmarkView): Marker? {
        val marker = map.addMarker(MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(bookmark.categoryResourceId?.let {
                    BitmapDescriptorFactory.fromResource(it)
                })
                .alpha(0.8f))
        marker.tag = bookmark
        bookmark.id?.let { markers.put(it, marker) }
        return marker
    }

    //method for displaying all bookmark markers
    private fun displayAllBookmarks(
        bookmarks: List<MapsViewModel.BookmarkView>) {
        bookmarks.forEach { addPlaceMarker(it) }
    }

    private fun createBookmarkObserver() {
        //retrieving a LiveData object, telling observer to follow lifecycle
        //of current activity, & processing updated bookmarks
        mapsViewModel.getBookmarkViews()?.observe(
            this, {
                //clearing all existing markers from map
                map.clear()
                markers.clear()
                //calling dispayAllBookmarks with list of updated
                //BookmarkView objects
                it?.let {
                    displayAllBookmarks(it)
                    bookmarkListAdapter.setBookmarkData(it)
                }
            })
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(databinding.mainMapView.toolbar)
        val toggle = ActionBarDrawerToggle(
                this, databinding.drawerLayout,
                databinding.mainMapView.toolbar,
                R.string.open_drawer, R.string.close_drawer)
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        databinding.drawerViewMaps.bookmarkRecyclerView.layoutManager= layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        databinding.drawerViewMaps.bookmarkRecyclerView.adapter =
                bookmarkListAdapter
    }

    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                16.0f))
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        //closing navigation drawer
        databinding.drawerLayout.closeDrawer(databinding.drawerViewMaps.
        drawerView)
        //looking up marker
        val marker = markers[bookmark.id]
        //if marker is found, showing info window
        marker?.showInfoWindow()
        //location object created from bookmark & zooming map to bookmark
        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun searchAtCurrentLocation() {
        //defining fields
        val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.PHONE_NUMBER,
                Place.Field.PHOTO_METADATAS,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.TYPES)
        //computing bounds of currently visible region of map
        val bounds =
                RectangularBounds.newInstance(map.projection.visibleRegion.latLngBounds)
        try {
            //building Intent, passing map bounds to search current map window first
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, placeFields)
                    .setLocationBias(bounds)
                    .build(this)
            //starting activity & identifying results
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {
            Toast.makeText(this, "Problems Searching",
                    Toast.LENGTH_LONG).show()
        } catch (e: GooglePlayServicesNotAvailableException) {
            Toast.makeText(this, "Problems Searching. Google Play Not available", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        //checking to make sure request codes match
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                //if result code returns a real place found with non null data,
                //processing continues
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //taking data & returning populated place object
                    val place = Autocomplete.getPlaceFromIntent(data)
                    //converting place's latlng to a location & passing that to
                    //updatemaptolocation method, causing map to zoom to that place
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?: 0.0
                    updateMapToLocation(location)
                    showProgress()
                    //loading place photo & displaying place Info window
                    displayPoiGetPhotoStep(place)
                }
        }
    }

    private fun newBookmark(latLng: LatLng) {
        GlobalScope.launch {
            val bookmarkId = mapsViewModel.addBookmark(latLng)
            bookmarkId?.let {
                startBookmarkDetails(it)
            }
        }
    }

    private fun disableUserInteraction() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableUserInteraction() {
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgress() {
        databinding.mainMapView.progressBar.visibility =
                ProgressBar.VISIBLE
        disableUserInteraction()
    }

    private fun hideProgress() {
        databinding.mainMapView.progressBar.visibility =
                ProgressBar.GONE
        enableUserInteraction()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
        const val EXTRA_BOOKMARK_ID =
                "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    class PlaceInfo(val place: Place? = null,
                    val image: Bitmap? = null)

}