package edu.ccsu.ritaapp.ui.itinerary

import ItemCard
import ItemRecyclerAdapter
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
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
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import kotlinx.android.synthetic.main.fragment_itinerary_init.*
import kotlinx.android.synthetic.main.fragment_itinerary_init.view.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class SavedItineraryFragment : Fragment() {

    var login = !cookieManager.cookieStore.get(uri).isEmpty()
    private val authenticationKey =
        cookieManager.cookieStore.get(uri).toString().replace("[", "").replace("]", "")
            .replace("Authorization=", "").replace("\"", "")
    private lateinit var itineraryViewModel: ItineraryViewModel
    var itemEntityList: ArrayList<ItemEntity> = ArrayList(1)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val root = inflater.inflate(R.layout.fragment_saved_itinerary, container, false)
        itineraryViewModel =
            ViewModelProviders.of(this).get(ItineraryViewModel::class.java)

        val recyclerView: RecyclerView = root.findViewById(R.id.savedRecyclerView)
        val saveItnBtn = root.saveItnBtn

        recyclerView.layoutManager = LinearLayoutManager(this.context)



        return root
    }


    fun apiCall(
        recyclerView: RecyclerView,
        itineraryId:Long
    ) {
        val url = baseUrl + "/user/get-saved-itinerary?itineraryId="+itineraryId


        lateinit var mAdapter: ItemRecyclerAdapter

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONArray(response)

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
        )
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


}

