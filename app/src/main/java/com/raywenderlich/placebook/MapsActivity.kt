package com.raywenderlich.placebook

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.raywenderlich.placebook.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient:
            FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupPlacesClient()
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
        getCurrentLocation()
        map.setOnPoiClickListener {displayPoi(it)
        }
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
            REQUEST_LOCATION)
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
        //retrieving placeId, which identifies place of interest
        val placeId = pointOfInterest.placeId
        //creating field mask containing attributes of place
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        //creating fetch request
        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()
        //fetching place details
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                //adding SuccessListener, retrieving place details,
                //& displaying place details
                val place = response.place
                Toast.makeText(this, "${place.name}, " +
                            "${place.phoneNumber}", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { exception ->
                //adding failure listener to check if API error occurred,
                //logging status code & message for debugging
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG, "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode)
                }
            }
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

}