package edu.ccsu.ritaapp.ui.itinerary

import ItemCard
import ItemRecyclerAdapter
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.ItemEntity
import edu.ccsu.ritaapp.APIModels.ItineraryEntity
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.ui.home.HomeFragment
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import kotlinx.android.synthetic.main.fragment_itinerary_init.*
import kotlinx.android.synthetic.main.fragment_itinerary_init.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class ItineraryFragment : Fragment() {

    var login = !cookieManager.cookieStore.get(uri).isEmpty()
    private val authenticationKey =
        cookieManager.cookieStore.get(uri).toString().replace("[", "").replace("]", "")
            .replace("Authorization=", "").replace("\"", "")
    private lateinit var itineraryViewModel: ItineraryViewModel
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var curDate: String =  current.format(formatter)
    var itemEntityList: ArrayList<ItemEntity> = ArrayList(1)

    companion object{
        var latitude : Double? = null
        var longitude : Double? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val root = inflater.inflate(R.layout.fragment_itinerary_init, container, false)
        itineraryViewModel =
            ViewModelProviders.of(this).get(ItineraryViewModel::class.java)
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        val saveItnBtn = root.saveItnBtn

        recyclerView.layoutManager = LinearLayoutManager(this.context)

        val preferenceSetup =
            PreferenceSetup(this.context!!, this.activity!!, root)
        preferenceSetup.setUpPreferences()

        root.findViewById<Button>(R.id.auto_button).setOnClickListener {
            if(latitude != null && longitude != null) {
                apiCall(
                    recyclerView,
                    preferenceSetup.getStartDay(),
                    preferenceSetup.getStartTime(),
                    preferenceSetup.getEndDay(),
                    preferenceSetup.getEndTime(),
                    preferenceSetup.getPrefParamString()
                )
            } else {
                Toast.makeText(this.context,"Please enter the place",Toast.LENGTH_SHORT).show()
            }
        }

        root.findViewById<Button>(R.id.manual_button).setOnClickListener{
            val manFrag = ManualItineraryFragment()
            val bundle = Bundle()
            bundle.putString("start_date", preferenceSetup.getStartDay())
            bundle.putString("start_time", preferenceSetup.getStartTime())
            bundle.putString("end_date", preferenceSetup.getEndDay())
            bundle.putString("end_time", preferenceSetup.getEndTime())
            bundle.putString("preference", preferenceSetup.getPrefParamString())

            if(latitude != null && longitude != null) {
                latitude?.let { it1 -> bundle.putDouble("latitude", it1) }
                longitude?.let { it2 -> bundle.putDouble("longitude", it2) }

                manFrag.arguments = bundle

                val fragment_transaction = fragmentManager!!.beginTransaction()
                fragment_transaction.replace(R.id.nav_itinerary_init, manFrag)
                fragment_transaction.addToBackStack(null)

                layout1.setVisibility(View.GONE)
                layout2.setVisibility(View.GONE)
                fragment_transaction.commit()
            } else {
                Toast.makeText(this.context,"Please enter the place",Toast.LENGTH_SHORT).show()
            }
        }



        saveItnBtn.setOnClickListener {
            val url = "https://rita-server.herokuapp.com/user/save-itinerary"
            saveItnBtn.isEnabled = false

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


        initializePlacesSearch()
        return root
    }

    /**
     * adds the ability to search for places
     * using google API
     */
    private fun initializePlacesSearch() {
        // Initialize Places.
        Places.initialize(super.getContext()!!, resources.getString(R.string.google_maps_key))
        // Create a new Places client instance.
        Places.createClient(super.getContext()!!)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        val placeListener = object : PlaceSelectionListener {
            val TAG = "placeautocomplete"
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.name + ", " + place.getId())
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        }

        autocompleteFragment.setOnPlaceSelectedListener(placeListener)
    }

    fun apiCall(
        recyclerView: RecyclerView,
        start_date: String,
        start_time: String,
        end_date: String,
        end_time: String,
        pref: String
    ) {
        val url = buildUrl(
            latitude,
            longitude, start_date, start_time, end_date,end_time, pref)

        layout1.setVisibility(View.GONE)
        layout2.setVisibility(View.GONE)

        if(login){

            itinerarybuttons.setVisibility(View.VISIBLE)
            saveItnBtn.isEnabled=false
            Run.after(4000) {
                saveItnBtn.isEnabled=true
            }

        } else{
            itinerarybuttons.setVisibility(View.GONE)
        }

        Toast.makeText(this.context,"Calculating Itinerary",Toast.LENGTH_SHORT).show()

        lateinit var mAdapter: ItemRecyclerAdapter


        val stringRequest = object:StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONArray(response)


//                val ie = ItineraryEntity(jResponse.get(0) as JSONObject)
                val ie =
                    ItineraryEntity(jResponse.getJSONObject(0))    //Getting only the first itinerary
                Log.d(jResponse.toString(), "JSON Response")
                Log.d("items length",ie.items.length().toString())
                val _items = ie.items
                for(i in 0.._items.length()-1){
                    Log.d(_items.get(i).toString(), "Item Mapping")
                    val entity =
                        ItemEntity(_items.get(i) as JSONObject)
                    Log.d(entity.toString(), "Item Entity")
                    itemEntityList.add(entity)
                }

                Log.d("itemEntitySize",itemEntityList.size.toString())
                val eList = createItemCardList(itemEntityList)
                mAdapter = ItemRecyclerAdapter(eList)
                recyclerView.adapter = mAdapter
            },
            Response.ErrorListener { error ->
                Log.d("Response Listener : Failed", error.toString())
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
        val numIterations = itemList.size

        while (i < numIterations) {
            val itemObject: ItemEntity = itemList[i]
            val item = ItemCard()
            item.item_name = itemObject.title
            item.item_date = itemObject.startTime.toString()
            item.latLng = itemObject.latLng
            itemCardList.add(item)
            i++
        }
        return itemCardList
    }

    private fun buildUrl(lat : Double?, lng : Double?, start_date : String, start_time : String, end_date : String, end_time:String, preference : String): String {

        val baseUrl = "https://rita-server.herokuapp.com/itineraries/both"
        val radius = 10000

        var startdate = start_date
        var enddate = end_date
        if(start_date.equals("")) startdate = curDate
        if(end_date.equals("")) enddate = curDate

        var starttime = start_time
        var endtime = end_time
        if(start_date.equals("")) startdate = curDate
        if(end_date.equals("")) enddate = curDate
        starttime += ":00"
        endtime += ":00"

        val url = baseUrl + "?latitude="+lat+
                "&longitude="+lng+
                "&radius="+radius+
                "&unit=KM&start-date="+startdate+
                "T"+starttime+"Z"+
                "&end-date="+enddate+
                "T"+endtime+"Z"+
                "&categories="+preference+"&meals=true&transport-mode=WALK"

        return url
    }
}

class Run {
    companion object {
        fun after(delay: Long, process: () -> Unit) {
            Handler().postDelayed({
                process()
            }, delay)
        }
    }
}
