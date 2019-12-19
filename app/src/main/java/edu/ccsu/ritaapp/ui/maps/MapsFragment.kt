package edu.ccsu.ritaapp.ui.maps

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log.d
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.APIEntity
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.fragment_maps.view.*
import org.json.JSONArray

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
    }
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private val apiEntityList: ArrayList<APIEntity> = ArrayList(1)


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.getActivity()!!.menuInflater.inflate(R.menu.explorer, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapsViewModel =
            ViewModelProviders.of(this).get(MapsViewModel::class.java)
        val view: View = inflater.inflate(R.layout.fragment_maps, container, false)
        setHasOptionsMenu(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        //Initialize the places SDK
        Places.initialize(super.getContext()!!, resources.getString(R.string.google_maps_key))

        view.searchButton.setOnClickListener { _ ->

            //autocomplete search
            val fields: List<Place.Field> =  listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent: Intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.CITIES)
                .setHint("Search City")
                .build((super.getContext()!!)
                )
            startActivityForResult(intent,
                AUTOCOMPLETE_REQUEST_CODE
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(super.getContext()!!)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                //placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        createLocationRequest()
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
            apiCall()
        }
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

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(super.getActivity()!!) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 11f))
            }
        }
    }


    private fun placeMarkerOnMap(location: LatLng, title: String) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title(title)
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


    private fun apiCall(){
        val center = mMap.cameraPosition.target
        val corner = mMap.projection.visibleRegion.farLeft
        val centerLoc = Location("center")
        centerLoc.latitude = center.latitude
        centerLoc.longitude = center.longitude
        val cornerLoc = Location("corner")
        cornerLoc.latitude = corner.latitude
        cornerLoc.longitude = corner.longitude
        val radius = (centerLoc.distanceTo(cornerLoc)/1000).toInt()
        d("distance", radius.toString())

        val url = "https://rita-server.herokuapp.com/activities/events.json?latitude=" + center.latitude.toString() + "&longitude=" + center.longitude.toString() + "&radius=" + radius.toString() + "&unit=KM&start-date=2019-10-01T10:30:00Z"
        //val url = "https://rita-server.herokuapp.com/activities/places.json?latitude=" + center.latitude.toString() + "&longitude=" + center.longitude.toString() + "&radius=" + radius.toInt().toString() + "&unit=KM&start-date=2019-10-01T10:30:00Z"
        //val url = "https://rita-server.herokuapp.com/activities/events-and-places.json?latitude=" + center.latitude.toString() + "&longitude=" + center.longitude.toString() + "&radius=" + radius.toInt().toString() + "&unit=KM&start-date=2019-10-01T10:30:00Z"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONArray(response)
                //d("fulljson", jResponse.toString())
                for (i in 0 until jResponse.length()){
                    val tme = APIEntity(jResponse.getJSONObject(i))
                    if (!apiEntityList.contains(tme)) {
                        apiEntityList.add(tme)
                        d("testJson", jResponse.getJSONObject(i).toString())
                        placeMarkerOnMap(tme.latLng, tme.title!!)
                    }
                    //localEvents.add(tme)
                    d("testview", tme.toString())
                }
            },
            Response.ErrorListener { _ ->
                d("test", "Failed")
            }
        )
        APIRequestSingleton.getInstance(super.getContext()!!).addToRequestQueue(stringRequest)
    }

}
