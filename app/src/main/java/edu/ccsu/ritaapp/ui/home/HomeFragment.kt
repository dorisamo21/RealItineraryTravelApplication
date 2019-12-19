package edu.ccsu.ritaapp.ui.home

import EventRecyclerAdapter
import EventCard
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.APIEntity
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import edu.ccsu.ritaapp.ui.itinerary.PreferenceSetup
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI


class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private lateinit var lastLocation: Location

    companion object {
        var lastLocation = Location("service provider")
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val AUTOCOMPLETE_REQUEST_CODE = 3

        val cookieManager = java.net.CookieManager()
        val baseUrl = "https://rita-server.herokuapp.com"
//        val baseUrl = "http://localhost:8080"
        val uri = URI(baseUrl)
    }
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    lateinit var mAdapter: EventRecyclerAdapter
    lateinit var recyclerView:RecyclerView
    lateinit var url : String
    lateinit var startDate:String
    lateinit var startTime:String
    lateinit var endDay:String
    lateinit var endTime:String
    var authentication_key = cookieManager.cookieStore.get(uri).toString()
    private val value2 = cookieManager.cookieStore.get(uri).toString().replace("[","").replace("]","").replace("Authorization=","").replace("\"","")


    //private lateinit var stringRequest: StringRequest
    var apiEntityList: ArrayList<APIEntity> = ArrayList<APIEntity>(1)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        lastLocation = Location("service provider")
        createLocationRequest()

        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        val preferenceSetup = PreferenceSetup(this.context!!, this.activity!!, root)
        startDate=preferenceSetup.getStartDay()
        startTime=preferenceSetup.getStartTime()
        endDay  = preferenceSetup.getEndDay()
        endTime = preferenceSetup.getEndTime()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(super.getContext()!!)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation

                if(value2.equals("")) {
                    apiCallLocal(
                        inflater,
                        container,
                        homeViewModel,
                        recyclerView
                    )
                } else {    // When Logged in
                    apiCallUser(
                        inflater,
                        container,
                        homeViewModel,
                        recyclerView
                    )
                }
            }
        }



        //TODO : check and update
        if(lastLocation.latitude==0.0 && lastLocation.longitude== 0.0) {
            apiCallLocal(
                inflater,
                container,
                homeViewModel,
                recyclerView)
        }

        return root
    }

    fun buildUrl(
        lat:String,
        lng:String,
        radius: Int,
        urlFrom:Int):String{
        //getting events and places by location
        if(urlFrom == 1) {
            url =
                baseUrl+"/activities/events-and-places.json?latitude=" + lat + "&longitude=" + lng + "&radius=" + radius + "&unit=KM&start-date=" + startDate + "T" + startTime + ":00Z&end-date=" + endDay + "T" + endTime + ":00Z&categories=1111111111111111"
            Log.d("buildurl", url)
        } else {
            val authentication_value = authentication_key.replace("[","").replace("]","")
            url = baseUrl+"/user/user-events?lat=" + lat + "&lng=" + lng + "&radius=" + radius + "&unit=KM&start-date=" + startDate + "T" + startTime + ":00Z&end-date=" + endDay + "T" + endTime + ":00Z&categories=1111111111111111"
        }
        return url
    }

    fun apiCallUser(
        inflater: LayoutInflater,
        container: ViewGroup?,
        homeViewModel:HomeViewModel,
        recyclerView:RecyclerView
    ) {

        lateinit var stringRequest : StringRequest

        apiCallLocal(inflater, container, homeViewModel, recyclerView)


        url = buildUrl(
                lastLocation.latitude.toString(),
                lastLocation.longitude.toString(),
                10000,
                2)

            Log.d("apiCall from user url",url)
            lateinit var mAdapter: EventRecyclerAdapter
            stringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener {
                    response ->

                val jResponse = JSONObject(response)

                try {
                    val body = jResponse.getJSONArray("body")

                    if(body.length()>0){
                        for (i in 0..body.length() - 1) {
                            val tme = APIEntity(body.getJSONObject(i))
                            apiEntityList.add(tme)
                        }
                    }
                } catch(e : Exception){}


                val eList = createEventCardList(apiEntityList)
                mAdapter = EventRecyclerAdapter(eList)
                recyclerView.adapter = mAdapter
            },
                Response.ErrorListener { error ->
                    Log.d("Response Listener", "Failed")
                }
            ){

                @Throws(AuthFailureError::class)
                override fun getHeaders(): MutableMap<String, String>? {
                    val params2 = HashMap<String, String>()
                    params2.put("Authorization",value2)
                    Log.d("value2",value2)
                    return params2
                }
            }


        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(stringRequest)

        Log.d("authentication_key177",authentication_key)

        apiEntityList.shuffle()
    }


    fun apiCallLocal(
        inflater: LayoutInflater,
        container: ViewGroup?,
        homeViewModel:HomeViewModel,
        recyclerView:RecyclerView
    ) {
        if(lastLocation.latitude==0.0 && lastLocation.longitude== 0.0) {
            //Setting default location (Manhattan)
            lastLocation.setLatitude(40.7831)
            lastLocation.setLongitude(-73.9712)
        }


        lateinit var stringRequest : StringRequest

            url = buildUrl(
                lastLocation.latitude.toString(),
                lastLocation.longitude.toString(),
                10000,
                1)

            Log.d("apiCall from local url",url)
            lateinit var mAdapter: EventRecyclerAdapter
            stringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener {
                    response ->

                val jResponse = JSONArray(response)

                for (i in 0..jResponse.length() - 1) {
                    val tme = APIEntity(jResponse.getJSONObject(i))
                    apiEntityList.add(tme)
                }

                val eList = createEventCardList(apiEntityList)
                mAdapter = EventRecyclerAdapter(eList)
                recyclerView.adapter = mAdapter
            },
                Response.ErrorListener { error ->
                    Log.d("Response Listener", "Failed")
                }
            ){}

        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(stringRequest)

        Log.d("authentication_key177",authentication_key)

        apiEntityList.shuffle()
    }





    fun updateEventList(){

        url = buildUrl(lastLocation.latitude.toString(),lastLocation.longitude.toString(),10000, 1)
        Log.d("update url",url)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                apiEntityList.clear()

                val jResponse = JSONArray(response)

                for (i in 0..jResponse.length() - 1) {
                    val tme = APIEntity(jResponse.getJSONObject(i))
                    apiEntityList.add(tme)
                }

                val eList = createEventCardList(apiEntityList)
                mAdapter = EventRecyclerAdapter(eList)
                recyclerView.adapter = mAdapter
                mAdapter.notifyDataSetChanged() //////
            },
            Response.ErrorListener { error ->
                Log.d("Response Listener", "Failed at update")
            }
        )
        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(stringRequest)
    }

    fun createEventCardList(eventsList: ArrayList<APIEntity>): ArrayList<EventCard> {
        val eventCardLists = ArrayList<EventCard>()
        var i = 0
        var numIterations = eventsList.size

        while (i < numIterations) {
            val eventsObject: APIEntity = eventsList.get(i)
            var events = EventCard()
            events.event_name = eventsObject.title
            events.event_image_url = eventsObject.imageUrl
            events.event_date = eventsObject.startTime.toString()
            events.event_address = eventsObject.address
            events.event_detail = eventsObject.description
            eventCardLists.add(events)
            i++
        }
        return eventCardLists
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(super.getActivity()!!)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            Log.d("listner?","219")
            startLocationUpdates()
//          updateEventList()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(super.getActivity()!!,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
            //map search result handler
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                Log.d("places", place.toString())

                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 10f))
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status: Status = Autocomplete . getStatusFromIntent (data!!)
                Log.d("autocomplete error", status.statusMessage!!)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(super.getActivity()!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.checkSelfPermission(super.getContext()!!,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //permission is not granted
            //request permission
            super.getActivity()!!.requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )


            Log.d("getActivity()",super.getActivity().toString())
            Log.d("arrayOf(Access_fine_location)",arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION).toString())
            Log.d("LOCATION_PERMISSION_REQUEST_CODE",LOCATION_PERMISSION_REQUEST_CODE.toString())
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        Log.d("onRequestPermissions","called")
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE ->{
                super.getActivity()!!.requestPermissions(   arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
                ActivityCompat.requestPermissions(super.getActivity()!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permission was","granted")

                }else{
                    Log.d("permission from","denied")
                }
                return
            }

        }
    }



    private fun permissionCheck() :Boolean {
        if (ActivityCompat.checkSelfPermission(super.getContext()!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }
}