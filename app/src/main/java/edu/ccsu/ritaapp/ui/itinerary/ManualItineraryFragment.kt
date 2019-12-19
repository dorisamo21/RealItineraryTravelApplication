package edu.ccsu.ritaapp.ui.itinerary

import ItemCard
import ItemRecyclerAdapter
import RecyclerAdapter
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.util.Log.d
import android.view.*
import android.widget.*
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
import edu.ccsu.ritaapp.APIModels.ItemEntity
import edu.ccsu.ritaapp.APIModels.ItineraryEntity
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import edu.ccsu.ritaapp.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_manitinerary.*
import kotlinx.android.synthetic.main.fragment_maps.view.*
import kotlinx.android.synthetic.main.popup.view.*
import org.json.JSONArray
import org.json.JSONObject

class ManualItineraryFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    var login = !HomeFragment.cookieManager.cookieStore.get(HomeFragment.uri).isEmpty()

    private lateinit var manItineraryViewModel: MItineraryViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
        val userChosenEventList = arrayListOf<UserChosenEvent>()

    }
    private lateinit var hashMap : HashMap<String, String>
    private var latitude : Double = 0.0
    private var longitude :Double = 0.0
    private val authenticationKey =
        HomeFragment.cookieManager.cookieStore.get(HomeFragment.uri).toString().replace("[", "").replace("]", "")
            .replace("Authorization=", "").replace("\"", "")

    private lateinit var start_date : String
    private lateinit var start_time : String
    private lateinit var end_date : String
    private lateinit var end_time : String
    private lateinit var preference : String
    private lateinit var mapWrapperLayout : MapWrapperLayout
    private lateinit var mapFragment : SupportMapFragment
    private lateinit var gobackBtn: Button
    private lateinit var saveItnBtn:Button

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private val apiEntityList: ArrayList<APIEntity> = ArrayList(1)

    var itemEntityList: ArrayList<ItemEntity> = ArrayList(1)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.getActivity()!!.menuInflater.inflate(R.menu.explorer, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        manItineraryViewModel =
            ViewModelProviders.of(this).get(MItineraryViewModel::class.java)

        start_date  = arguments!!.getString("start_date")
        start_time  = arguments!!.getString("start_time")+":00"
        end_date    = arguments!!.getString("end_date")
        end_time    = arguments!!.getString("end_time")+":00"
        preference  = arguments!!.getString("preference")
        latitude    = arguments!!.getDouble("latitude")
        longitude   = arguments!!.getDouble("longitude")
        userChosenEventList.clear()


        val view: View = inflater.inflate(R.layout.fragment_manitinerary, container, false)
        setHasOptionsMenu(false)

        saveItnBtn = view.findViewById(R.id.saveItnBtn)
        //RecyclerView for the itinerary card
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        mapWrapperLayout = view.findViewById(R.id.map_relative_layout) as MapWrapperLayout
        mapFragment = childFragmentManager.findFragmentById(R.id.map1) as SupportMapFragment

        mapFragment.getMapAsync(this)

        //Initialize the places SDK
        Places.initialize(super.getContext()!!, resources.getString(R.string.google_maps_key))


        view.searchButton.setOnClickListener { _ ->

            //autocomplete search
            val fields: List<Place.Field> =  listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent: Intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.CITIES)
                .setHint("Enter Location")
                .build((super.getContext()!!)
                )
            startActivityForResult(intent,
                AUTOCOMPLETE_REQUEST_CODE
            )

        }


        val _startOver : Button = view.findViewById(R.id.start_over)
        val _checkEvents : Button = view.findViewById(R.id.check_events)
        val _getItinerary : Button = view.findViewById(R.id.get_itinerary)

        //Initialize the places SDK
        // TODO: HIDE THE KEY
        Places.initialize(super.getContext()!!, "AIzaSyBmeQUxf__MjOdPyKSPRyOxs2xhV2z6xaw" )
        val placesClient: PlacesClient = Places.createClient(super.getContext()!!)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(super.getContext()!!)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                lastLocation.latitude = latitude
                lastLocation.longitude = longitude

                placeMarkerOnMap(LatLng(latitude, longitude))//TODO : show several places from itinerary
            }
        }
        createLocationRequest()

        _startOver.setOnClickListener{
            val itineraryFragment = ItineraryFragment()
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.nav_itinerary_manual, itineraryFragment)
            fragmentTransaction.addToBackStack(null)
            start_over.visibility = View.GONE
            check_events.visibility = View.GONE
            get_itinerary.visibility = View.GONE
            fragment_manitinerary.visibility = View.GONE
            fragmentTransaction.commit()
        }
        _checkEvents.setOnClickListener{
            val popupView = layoutInflater.inflate(R.layout.popup, null)
            // Get the widgets reference from custom view
            val popupWindow = PopupWindow(
                popupView, // Custom view to show in popup window
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }
            // If API level 23 or higher then execute the code
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }
            val buttonPopup: Button = popupView.findViewById(R.id.button_popup)
            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

            //RecyclerView for selected Item list popup
            val eventListRecyclerView : RecyclerView = popupView.event_list_recycler
            eventListRecyclerView.layoutManager = LinearLayoutManager(this.context)

            val eventList = createEventCardList(userChosenEventList)
            val eventlistAdapter = RecyclerAdapter(eventList)
            eventListRecyclerView.adapter = eventlistAdapter

            TransitionManager.beginDelayedTransition(nav_itinerary_manual)
            popupWindow.showAtLocation(
                nav_itinerary_manual, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }

        _getItinerary.setOnClickListener{
            //TODO : Send API call + go to page displaying itinerary
            // extract (id,service)
            var idAndServiceList = arrayListOf<String>()
            for(i in 0 until userChosenEventList.size) {
                idAndServiceList.add(userChosenEventList[i].title)
                idAndServiceList.add(userChosenEventList[i].service)
            }

            apiCall(recyclerView, idAndServiceList)

        }


        saveItnBtn.setOnClickListener{
            saveItnBtn.isEnabled=false

            val url = "https://rita-server.herokuapp.com/user/save-itinerary"

            val stringRequest = object: StringRequest(
                Request.Method.GET, url,
                Response.Listener { response ->

                },
                Response.ErrorListener { error ->
                    Log.d("Response Listener", "Failed")
                    Log.d("Error", error.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    var params: HashMap<String, String> = HashMap()
                    params.put("Authorization", authenticationKey)
                    Log.d("Key", authenticationKey)
                    return params
                }
            }
            APIRequestSingleton.getInstance(super.getContext()!!)
                .addToRequestQueue(stringRequest)

            Toast.makeText(this.context,"Itinerary was saved",Toast.LENGTH_SHORT).show()

        }

        return view
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
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap()
        mMap.setOnCameraIdleListener {
            getMarkers()
        }
        mMap.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener{
            override fun onInfoWindowClick(marker : Marker) {
                //Add the event info here
                if(marker.snippet != null) {
//                    idAndServiceList.add(marker.snippet)   //Added to service
                    val newUserChosenEvent =
                        UserChosenEvent(
                            marker.title,
                            marker.snippet.split(",")[0],
                            marker.snippet.split(",")[1]
                        )
                    if(!userChosenEventList.contains(newUserChosenEvent)) {
                        userChosenEventList.add(newUserChosenEvent)
                        Log.d(userChosenEventList.toString(), "UserChosenEventList")
                    }
                }
                marker.hideInfoWindow()
            }
        });
    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        mMap.clear()
        if (ActivityCompat.checkSelfPermission(super.getContext()!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(super.getActivity()!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }


        val currentLatLng = LatLng(latitude,longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 11f))


    }

    private fun placeMarkerOnMap(location: LatLng, title: String, service : String) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions
            .title(title)
            .snippet(service)

        mMap.addMarker(markerOptions)
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        mMap.addMarker(markerOptions)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(super.getContext()!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(super.getActivity()!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
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
            startLocationUpdates()
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
                d("places", place.toString())

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 10f))
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status: Status = Autocomplete . getStatusFromIntent (data!!)
                d("autocomplete error", status.statusMessage!!)
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

    private fun getMarkers(){

        val url = buildUrl()
        Log.d("getMarkersUrl",url)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONArray(response)
                hashMap = HashMap<String, String> ()
                //d("fulljson", jResponse.toString())
                for (i in 0 until jResponse.length()){
                    val tme = APIEntity(jResponse.getJSONObject(i))
                    if (!apiEntityList.contains(tme)) {
                        apiEntityList.add(tme)
                        d("testJson", jResponse.getJSONObject(i).toString())
                        placeMarkerOnMap(tme.latLng, tme.title!!,tme.service.toString()+","+tme.id)
                        //Adding the window when user click the marker
                        var infoWindow = layoutInflater.inflate(R.layout.rv_event_on_map, null)
                        var infoTitle  = infoWindow.findViewById(R.id.title) as TextView
                        var infoSnippet = infoWindow.findViewById(R.id.snippet) as TextView

                        var addToListButton = infoWindow.findViewById(R.id.button1) as Button

                        var infoOnClick =  object: View.OnClickListener {
                            override fun onClick(v: View?) {

                            }
                        }
                        addToListButton.setOnClickListener(infoOnClick)

                        var infoWindowAdapter = object:InfoWindowAdapter {
                            override fun getInfoWindow(marker: Marker): View? {
                                return null
                            }

                            override fun getInfoContents(marker: Marker): View? {
                                infoTitle.setText(marker.title)
                                infoSnippet.setText(marker.snippet) //Give the events id and service info

                                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow)
                                return infoWindow
                            }
                        }

                        mMap.setInfoWindowAdapter(infoWindowAdapter)


                    }
                }
            },
            Response.ErrorListener { _ ->
                d("test", "Failed")
            }
        )
        APIRequestSingleton.getInstance(super.getContext()!!).addToRequestQueue(stringRequest)
    }

    //build url to get markers
    fun buildUrl(): String {

        var baseUrl = "https://rita-server.herokuapp.com/activities/events-and-places.json"
        val radius = 10000

        var url = baseUrl + "?latitude="+latitude+
                "&longitude="+longitude+
                "&radius="+radius+
                "&unit=KM&start-date="+start_date+
                "T"+start_time+"Z"+
                "&end-date="+end_date+
                "T"+end_time+"Z"+
                "&categories="+preference+"&meals=true&transport-mode=WALK"

        return url
    }

    //build url to get itinerary
    fun buildUrl(lat : Double?, lng : Double?, start_date : String, start_time : String, end_date : String,end_time:String, preference : String, chosen_events : String): String {

        var baseUrl = "https://rita-server.herokuapp.com/itineraries/both"
        val radius = 10000

        var url = baseUrl + "?latitude="+lat+
                "&longitude="+lng+
                "&radius="+radius+
                "&unit=KM&start-date="+start_date+
                "T"+start_time+"Z"+
                "&end-date="+end_date+
                "T"+end_time+"Z"+
                "&categories="+preference+"&meals=true&transport-mode=WALK"+
                "&manual-events="+chosen_events

        return url
    }

    fun apiCall(recyclerView:RecyclerView, idAndServiceList: ArrayList<String>){
        var events_str = idAndServiceList.joinToString(separator = ",")
        var url = buildUrl(latitude,longitude,start_date, start_time, end_date, end_time, preference,events_str)
        fragment_manitinerary.setVisibility(View.GONE)
        buttons.setVisibility(View.GONE)

        Toast.makeText(this.context,"Calculating Itinerary",Toast.LENGTH_SHORT).show()

        lateinit var mAdapter: ItemRecyclerAdapter

        val stringRequest = object : StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                if(login){
                    Run.after(4000) {
                        itinerarybuttons.setVisibility(View.VISIBLE)
                    }
                } else{
                    itinerarybuttons.setVisibility(View.GONE)
                }
                val jResponse = JSONArray(response)

                val ie =
                    ItineraryEntity(jResponse.getJSONObject(0))    //Getting only the first itinerary
                Log.d("items length",ie.items.length().toString())
                val _items = ie.items
                for(i in 0.._items.length()-1){
                    itemEntityList.add(ItemEntity(_items.get(i) as JSONObject))
                }

                Log.d("itemEntitySize",itemEntityList.size.toString())
                val eList = createItemCardList(itemEntityList)
                mAdapter = ItemRecyclerAdapter(eList)
                recyclerView.adapter = mAdapter



            },
            Response.ErrorListener { error ->
                Log.d("Response Listener", "Failed")
            }
        ){

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String>? {
                val params2 = HashMap<String, String>()
                params2.put("Authorization",authenticationKey)
                Log.d("value2",authenticationKey)
                return params2
            }
        }
        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(stringRequest)


    }

    fun createItemCardList(itemList: ArrayList<ItemEntity>): ArrayList<ItemCard> {
        val itemCardList = ArrayList<ItemCard>()
        var i = 0
        var numIterations = itemList.size

        while (i < numIterations) {
            val itemObject: ItemEntity = itemList[i]
            var item = ItemCard()
            item.item_name = itemObject.title
            item.item_date = itemObject.startTime.toString()
            item.latLng = itemObject.latLng
            itemCardList.add(item)
            i++
        }
        return itemCardList
    }

    data class UserChosenEvent (val title: String, val id: String, val service: String)


    fun createEventCardList(itemList: ArrayList<UserChosenEvent>): ArrayList<EventListCard> {
        val eventCardList = ArrayList<EventListCard>()
        var i = 0
        var numIterations = itemList.size

        while (i < numIterations) {
            val itemObject: UserChosenEvent = itemList[i]
            var item = EventListCard()
            item.item_title = itemObject.title
            item.item_snippet = itemObject.id+","+itemObject.service
            eventCardList.add(item)
            i++
        }
        return eventCardList
    }
}
