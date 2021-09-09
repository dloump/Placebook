package com.raywenderlich.placebook

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.raywenderlich.placebook.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var locationRequest: LocationRequest? = null
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
            if (locationRequest == null) {
                locationRequest = LocationRequest.create()
                locationRequest?.let { locationRequest ->
                    //setting guid to how accurate locations should be
                    locationRequest.priority =
                        LocationRequest.PRIORITY_HIGH_ACCURACY
                    //specifying desired interval in milliseconds to return updates
                    locationRequest.interval = 5000
                    //setting shortest interval app is capable of handling
                    locationRequest.fastestInterval = 1000
                    //updating map to center on new location
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult:
                                                      LocationResult?) {
                            getCurrentLocation()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback, null)
                }
            }
            //requesting to be notified when location is ready
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    //if location is not null, creating object for map location
                    val latLng = LatLng(location.latitude,
                        location.longitude)
                    //removing previous marker
                    map.clear()
                    //creating marker to mark map location
                    map.addMarker(MarkerOptions().position(latLng)
                        .title("You are here!"))
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

                companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

}