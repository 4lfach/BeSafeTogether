package com.tutor.noviolence

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_map.*

class GoogleMapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapFragment"
        private const val DEFAULT_ZOOM = 16F
        private val PERMISSION_ID = 42
        private const val API_KEY = "AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g"
        private val defaultCurrentLocation: LatLng = LatLng(51.132065, 71.406377)
    }

    lateinit var mMap: GoogleMap
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var placesClient: PlacesClient

    lateinit var selectedPlace : Place

    private var myCurrentLocation: LatLng = LatLng(51.132065, 71.406377)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initializePlacesAutoComplete()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        btnShowDangerSheet.setOnClickListener {
            showSetDangerDialog()
        }

        btnMyLocation.setOnClickListener {
            getLastLocation()
        }
    }

    private fun showSetDangerDialog() {
        if (selectedPlace == null){
            Toast.makeText(requireContext(), "You didn't select the place in search bar.", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.bottomSheetDialogTheme)

        val bottomSheetView = LayoutInflater.from(activity?.applicationContext).inflate(
            R.layout.bottom_sheet_map,
            view?.findViewById(R.id.llBottomSheetMap) as LinearLayout?
        )

        bottomSheetView.findViewById<View>(R.id.btnCreateDanger).setOnClickListener {
            Toast.makeText(requireContext(), "Saving...", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<TextView>(R.id.tvPlaceName).text = selectedPlace.name
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun initializePlacesAutoComplete() {
        //Initialize SDK
        Places.initialize(activity?.applicationContext, "AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g")
        //Create a new Place client instance
        placesClient = Places.createClient(requireContext())
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        //Specify that user will write address to set danger spot
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS)

        //Set bounds where search should work
        //LatLng(51.079139, 71.250038), LatLng(51.225105, 71.633094) is Astana bounds
        autocompleteFragment.setLocationBias(
            RectangularBounds.newInstance(
                LatLng(51.079139, 71.250038),
                LatLng(51.225105, 71.633094)
            )
        )
        autocompleteFragment.setCountries("KZ")
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }

            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
                selectedPlace = place

                val placeLatLng = place.latLng
                val marker = MarkerOptions().position(placeLatLng)
                marker.visible(true)
                mMap.addMarker(marker)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        placeLatLng, DEFAULT_ZOOM
                    )
                )
            }
        })


    }

    // Get current location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions())
            if (mMap.isMyLocationEnabled) {
                mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        myCurrentLocation = LatLng(location.latitude, location.longitude)
                        mMap.clear()
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                defaultCurrentLocation,
                                DEFAULT_ZOOM
                            )
                        )
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Turn on the GPS", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
    }

    // Get current location, if shifted
    // from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            myCurrentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    // Check if location permissions are
    // granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            return true
        }
        return false
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mMap = map
        if (checkPermissions()) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        } else {
            Toast.makeText(requireContext(), "Enable GPS to use this function", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GoogleMapFragment.PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
}