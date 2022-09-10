package com.tutor.noviolence.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tutor.noviolence.CoroutineAsyncTask
import com.tutor.noviolence.R
import com.tutor.noviolence.db.RetrofitClient
import com.tutor.noviolence.models.CommentModel
import com.tutor.noviolence.models.adapters.CommentMapItemAdapter
import com.tutor.noviolence.models.response.CommentResponse
import com.tutor.noviolence.models.response.CommentsResponseModel
import com.tutor.noviolence.models.response.DefaultResponseModel
import com.tutor.noviolence.services.SharedPrefManager
import kotlinx.android.synthetic.main.bottom_sheet_map.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class GoogleMapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapFragment"
        private const val DEFAULT_ZOOM = 16F
        private val PERMISSION_ID = 42
        private const val API_KEY = "AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g"
    }

    private var comments: ArrayList<CommentModel> = ArrayList()
    private var placeIds : ArrayList<String> = ArrayList()
    private var places : ArrayList<Place> = ArrayList()
    private var latLngPlaces : ArrayList<LatLng> = ArrayList()
    lateinit var sharedPrefManager : SharedPrefManager
    lateinit var mMap: GoogleMap
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var placesClient: PlacesClient

     var selectedPlace : Place? = null

    private var myDest : LatLng? = null

    private lateinit var myCurrentLocation: LatLng

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefManager = SharedPrefManager.getInstance(requireContext())

        initializePlacesAutoComplete()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        btnShowDangerSheet.setOnClickListener {
            showSetDangerDialog()
        }

        btnMyLocation.setOnClickListener {
            getLastLocation()
            //drawRoute()
        }

        btnDrawSafeRoute.setOnClickListener{
            getLastLocation()
            drawRoute()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        addMarkersOnMap()
    }


    private fun drawRoute() {
        if(myDest == null){
            Toast.makeText(requireContext(), "Select place to find the optimal route", Toast.LENGTH_LONG).show()
            return
        }
        val str_origin = "origin="+myCurrentLocation.latitude + "," + myCurrentLocation.longitude
        val str_dest = "destination=" + myDest!!.latitude + "," + myDest!!.longitude
        val mode = "mode=walking"
        val parameters = "$str_origin&$str_dest&$mode"
        val output = "json"
        val url: String = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g"
        val URL: String = getDirectionURL(myCurrentLocation, myDest!!)
        val downloadTask: DownloadTask = DownloadTask()
        downloadTask.execute(url)
        //GetDirection(URL).execute()
        //Toast.makeText(requireContext(), "IT SHOULD WORK", Toast.LENGTH_SHORT).show()
    }

    fun getDirectionURL(origin:LatLng, dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=walking&key=AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g"
    }

    inner class DownloadTask :
        CoroutineAsyncTask<String?, Void?, String?>(){

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)
        }


        override fun doInBackground(vararg params: String?): String {
            // Toast.makeText(requireContext(), "IT SHOULD WORK2", Toast.LENGTH_SHORT).show()
            var data = ""
            try{
                data = downloadUrl(params[0].toString()).toString()
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }

    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String?): String? {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection!!.inputStream
            val br =
                BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    //parsing into JSON format
    inner class ParserTask :
        CoroutineAsyncTask<String?, Int?, List<List<HashMap<String, String>>>?> () {
        override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? =
                null
            try {
                jObject = JSONObject(params[0])
                val parser = DataParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }


        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            val points = ArrayList<LatLng?>()
            val lineOptions = PolylineOptions()
            for (i in result!!.indices) {
                val path =
                    result[i]
                for (j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.RED)
                lineOptions.geodesic(true)
            }

            if (points.size != 0) mMap!!.addPolyline(lineOptions)
        }
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

        mMap.setOnMarkerClickListener {marker->
            for (place in places){
                if(marker.position == place.latLng){
                    showDangerPlaceSheetDialog(place.id, place.name)
                }
            }
            true
        }
    }

    private fun showDangerPlaceSheetDialog(placeId: String, placeName : String) {
        fetchCommentsByPlaceId(placeId, placeName)

        //Fetch comments per particular place_id, populates comments array

        val bottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.bottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(requireContext()).inflate(
            R.layout.danger_spot_sheet,
            view?.findViewById(R.id.llDangerSpotSheet) as LinearLayout?
        )
        bottomSheetView.findViewById<TextView>(R.id.tvPlaceName).text = placeName



        CoroutineScope(Main).launch{
            delay(1500)
            val placeRating = calculateAverRating()

            if (placeRating == 0F)
                bottomSheetView.findViewById<TextView>(R.id.tvAverRating).text = "None"
            bottomSheetView.findViewById<TextView>(R.id.tvAverRating).text = placeRating.toString()
            bottomSheetView.findViewById<RecyclerView>(R.id.rvComments).layoutManager =
                LinearLayoutManager(requireContext())
            bottomSheetView.findViewById<RecyclerView>(R.id.rvComments).adapter =
                CommentMapItemAdapter(comments, this@GoogleMapFragment)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

    }


    private fun calculateAverRating(): Float {
        var rating = 0F
        var length = 0

        for(cmt in comments){
            rating +=cmt.rating
            length++
        }

        return rating/length
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
            //check if dangerous place is already posted by someone
            val id = sharedPrefManager.user.id
            val place_id = selectedPlace?.id
            val content = bottomSheetView.findViewById<EditText>(R.id.etPlaceComment).text.toString()
            val rating = bottomSheetView.findViewById<RatingBar>(R.id.ratingDanger).rating
            val pub_date = Instant.now().toString()

            if(place_id == null && content == null && rating < 1){
                Toast.makeText(requireContext(), "Fill everything", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val call : Call<DefaultResponseModel> = RetrofitClient.getInstance().api.createComment(id, place_id, content, rating, pub_date)
            call.enqueue(object: Callback<DefaultResponseModel>{
                override fun onResponse(
                    call: Call<DefaultResponseModel>,
                    response: Response<DefaultResponseModel>
                ) {
                    if (response.code() == 201){
                        val dr = response.body()
                        Toast.makeText(requireContext(), dr!!.getMsg(), Toast.LENGTH_LONG).show()
                        bottomSheetDialog.dismiss()
                    } else {
                        val dr = response.errorBody()
                        Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<DefaultResponseModel>, t: Throwable) {

                }

            })
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<TextView>(R.id.tvPlaceName).text = selectedPlace?.name
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun addMarkersOnMap(){
        val call : Call<ResponseBody> = RetrofitClient.getInstance().api.placeIds

        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var res = response.body()!!

                if(res != null){
                    try {
                        val jObj : JSONObject = JSONObject(res.string())
                        var jPlaceIds = jObj.getJSONArray("place_ids")

                        for (i in 0 until jPlaceIds.length()) {
                            placeIds.add(jPlaceIds.getJSONObject(i).optString("place_id"))
                        }

                        for (id in placeIds){
                            findPlaceById(id){ place->
                                latLngPlaces.add(place.latLng)
                                places.add(place)

                                mMap.addMarker(MarkerOptions().position(place.latLng).title("Danger").icon(
                                    BitmapDescriptorFactory.fromResource(R.drawable.ic_danger)))
                            }
                        }

                    }  catch (e : JSONException){
                        e.printStackTrace()
                    }

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchCommentsByPlaceId(placeId: String, placeName: String) {
        comments = ArrayList<CommentModel>()
        var commentResponseList: ArrayList<CommentResponse>

        val call : Call<CommentsResponseModel> = RetrofitClient.getInstance().api.getCommentsByPlaceId(placeId)

        call.enqueue(object: Callback<CommentsResponseModel> {
            override fun onResponse(
                call: Call<CommentsResponseModel>,
                response: Response<CommentsResponseModel>
            ) {
                commentResponseList = response.body()!!.comments
                for (cmtRes in commentResponseList){

                    val id = cmtRes.id
                    val userId = cmtRes.user_id
                    val placeId = cmtRes.place_id
                    val content = cmtRes.content
                    val rating = cmtRes.rating
                    val date = convertStringToInstant(cmtRes.pub_date)

                    findPlaceById(placeId){ place->
                        val cmt : CommentModel = CommentModel(id,userId, content, place, rating, date)
                        comments.add(cmt)

                    }
                }
            }
            override fun onFailure(call: Call<CommentsResponseModel>, t: Throwable) {
            }

        })
    }

    private fun convertStringToInstant(date: String): Instant {
        val instant = Instant.parse(date)
        return instant
    }

    private fun findPlaceById(placeId: String, myCallBack: (result: Place) -> Unit){
        Places.initialize(activity?.applicationContext, "AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g")
        //Create a new Place client instance
        placesClient = Places.createClient(requireContext())

        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.i(TAG, "Place found: ${place.name}")

                myCallBack.invoke(place)
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                }
            }
    }

    private fun showCommentsOfPlace(placeId : String){
        if (selectedPlace == null){
            Toast.makeText(requireContext(), "You didn't select the place in search bar.", Toast.LENGTH_SHORT).show()
            return
        }
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
                myDest = placeLatLng
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
    public fun getLastLocation() {
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
                                myCurrentLocation,
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