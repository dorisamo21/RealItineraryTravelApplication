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
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIModels.ItemEntity
import edu.ccsu.ritaapp.APIModels.ItineraryEntity
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class EditItineraryFragment : Fragment() {

    var login = !cookieManager.cookieStore.get(uri).isEmpty()
    private val authenticationKey =
        cookieManager.cookieStore.get(uri).toString().replace("[", "").replace("]", "")
            .replace("Authorization=", "").replace("\"", "")
    private lateinit var itineraryViewModel: ItineraryViewModel
    lateinit var gobackBtn : Button

    var itemEntityList: ArrayList<ItemEntity> = ArrayList(1)
    var itineraryList: ArrayList<Long> = ArrayList(1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val root = inflater.inflate(R.layout.fragment_edit_itinerary, container, false)
        itineraryViewModel =
            ViewModelProviders.of(this).get(ItineraryViewModel::class.java)
        val recyclerView: RecyclerView = root.findViewById(R.id.itnListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        gobackBtn = root.findViewById(R.id.gobackBtn)
        gobackBtn.setVisibility(View.GONE)

        gobackBtn!!.setOnClickListener {
            gobackBtn.setVisibility(View.GONE)

            apiCall(recyclerView)

        }


        apiCall(recyclerView)
        return root
    }


    fun apiCall(recyclerView:RecyclerView){

        var url = baseUrl + "/user/get-available-itineraries"

        lateinit var mAdapter: ItnAdapter

        val stringRequest = object: StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONObject(response)

                val body = jResponse.getJSONArray("body")

                val itnList = resToItineraryCard(body)
                mAdapter = ItnAdapter(itnList)

                mAdapter.setOnItemClickedListener(object: ItnAdapter.OnItemClickListener{
                    override fun onItemClick(position: Int) {
                        // alert dialog
                        // build alert dialog
                        val dialogBuilder = AlertDialog.Builder(context)

                        // set message of alert dialog
                        dialogBuilder.setMessage("Do you want to delete itinerary "+itnList.get(position).id+" ?")
                            // if the dialog is cancelable
                            .setCancelable(false)
                            // positive button text and action
                            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                                    dialog, id ->

                                val url = baseUrl+"/user/delete-itinerary?itineraryId="+itnList.get(position).id

                                val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                                        response ->
                                        Toast.makeText(context, "Deleted itinerary "+itnList.get(position).id, Toast.LENGTH_SHORT).show()
                                        itnList.removeAt(position)
                                        mAdapter.notifyItemRemoved(position)

                                }, Response.ErrorListener{
                                        error->
                                    Log.d("Error :" , error.toString())

                                }){
                                    override fun getBodyContentType(): String {
                                        return "application/json"
                                    }

                                    @Throws(AuthFailureError::class)
                                    override fun getHeaders(): MutableMap<String, String>? {
                                        val params2 = HashMap<String, String>()
                                        Log.d("param2 value",authenticationKey)
                                        params2.put("Authorization",authenticationKey)
                                        return params2
                                    }

                                }

                                context?.let {
                                    APIRequestSingleton.getInstance(it)
                                        .addToRequestQueue(mStringRequest)
                                }


                                //END of TODO





                            })
                            // negative button text and action
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                    dialog, id -> dialog.cancel()
                            })

                        // create dialog box
                        val alert = dialogBuilder.create()
                        // set title for alert dialog box
                        alert.setTitle("Delete Friend Alert")
                        // show alert dialog
                        alert.show()

                        // alert dialog end
                        Log.d("del itinerary id",itnList.get(position).id.toString())

                    }
                })

                mAdapter.setOnViewClickedListener(object: ItnAdapter.OnViewClickListener{
                    override fun onViewClick(position: Int) {
                        Toast.makeText(context,"Retrieving Requested Itinerary",Toast.LENGTH_SHORT).show()

                        Log.d("view itinerary id",itnList.get(position).id.toString())
                        getItemList(itnList.get(position).id)

                    }

                    lateinit var ieAdapter: ItemRecyclerAdapter

                    fun createItemCardList(itemList: ArrayList<ItemEntity>): ArrayList<ItemCard> {
                        val itemCardList = ArrayList<ItemCard>()
                        var i = 0
                        val numIterations = itemList.size

                        while (i < numIterations) {
                            val itemObject: ItemEntity = itemList[i]
                            val item = ItemCard()
                            item.item_name = itemObject.title
                            //            item.item_image_url = itemObject.imageUrl
                            item.item_date = itemObject.startTime.toString()
                            item.latLng = itemObject.latLng
                            itemCardList.add(item)
                            i++
                        }
                        return itemCardList
                    }

                    fun getItemList(itineraryId: Long) {


                        val url = baseUrl + "/user/get-saved-itinerary?itineraryId="+itineraryId
                        val stringRequest = object: StringRequest(
                            Request.Method.GET, url,
                            Response.Listener { response ->

                                val jResponse = JSONObject(response)

                                val genericResponse = GenericResponse<List<ItineraryEntity>>(jResponse)

                                if(genericResponse.status==0) {
                                    try {

                                        val ie = ItineraryEntity(genericResponse.obj.getJSONObject("body"))
                                        Log.d(jResponse.toString(), "JSON Response")
                                        val _items = ie.items
                                        for(i in 0.._items.length()-1){
                                            Log.d(_items.get(i).toString(), "Item Mapping")
                                            val entity =
                                                ItemEntity(_items.get(i) as JSONObject)
                                            Log.d(entity.toString(), "Item Entity")
                                            itemEntityList.add(entity)
                                        }
                                        val eList = createItemCardList(itemEntityList)
                                        ieAdapter = ItemRecyclerAdapter(eList)
                                        recyclerView.adapter = ieAdapter



                                    } catch (e: Exception) {
                                        Log.d("Exception", e.toString())
                                    }
                                }

                                else{
                                    Log.d("Itinerary list", "is Null")
                                }
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
                        context?.let {
                            APIRequestSingleton.getInstance(it)
                                .addToRequestQueue(stringRequest)
                        }
                        Run.after(2000) {
                            gobackBtn.setVisibility(View.VISIBLE)
                        }

                    }
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

                })
                recyclerView.adapter = mAdapter

            },
            Response.ErrorListener { error ->
                Log.d("Response Listener: Failed", error.toString())
            }
        ){
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String>? {
                val params2 = HashMap<String, String>()
                Log.d("param2 value",authenticationKey)
                params2.put("Authorization",authenticationKey)
                return params2
            }

        }
        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(stringRequest)
    }


    fun resToItineraryCard(itemList: JSONArray): ArrayList<ItnAdapter.ItineraryCard> {
        val itnCardList = ArrayList<ItnAdapter.ItineraryCard>()
        var i = 0
        val numIterations = itemList.length()

        while (i < numIterations) {
            val itineraryId = itemList.getLong(i)
            val item = ItnAdapter.ItineraryCard(itineraryId)
            Log.d("itinerary Object is", itineraryId.toString())
            itnCardList.add(item)
            i++
        }
        return itnCardList
    }

    data class ItineraryCard(
        val id:Long=0
    )

    data class UserChosenEvent (val title: String, val id: String, val service: String)

}

