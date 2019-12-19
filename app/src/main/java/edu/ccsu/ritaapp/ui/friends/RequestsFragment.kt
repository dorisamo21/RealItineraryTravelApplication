package edu.ccsu.ritaapp.ui.friends

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import edu.ccsu.ritaapp.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_friendrequests.*
import org.json.JSONArray
import org.json.JSONObject

class RequestsFragment : Fragment() {

    var authenticationKey = HomeFragment.cookieManager.cookieStore.get(HomeFragment.uri).toString().replace("[", "").replace("]", "")
        .replace("Authorization=", "").replace("\"", "")

    private lateinit var viewModel: RequestsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Cookie at Requests!!", authenticationKey)

        val root = inflater.inflate(R.layout.fragment_friendrequests, container, false)

        viewModel = ViewModelProviders.of(this).get(RequestsViewModel::class.java)

        val requestRecycler: RecyclerView = root.findViewById(R.id.requestRecycler)
        requestRecycler.layoutManager = LinearLayoutManager(this.context)

        createRequestList()

        return root
    }

    fun createRequestList() {
        Log.d("Get Requests API", "See requests")
        val url = "https://rita-server.herokuapp.com/user/friend-request-search"

        val stringRequest = object: StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONObject(response)

                val genericResponse = GenericResponse<List<User>>(jResponse)

                if(genericResponse.status==200) {
                    try {
                        if(genericResponse.body==null)
                        {
                            Log.d("Request List", "Is Null")
                        }
                        val requestList = genericResponse.obj.getJSONArray("body")
                        val rList = userToRequestCard(requestList)
                        val requestListAdapter = RequestAdapter(rList)
                        Log.d("Called adapter", "Successful")
                        requestRecycler.adapter = requestListAdapter


                        //accept friend request
                        requestListAdapter.setOnItemClickedListener(object: RequestAdapter.OnItemClickListener{
                            override fun onItemClick(position: Int) {
                                // alert dialog
                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(context)

                                // set message of alert dialog
                                dialogBuilder.setMessage("Do you want to accept "+rList.get(position).request_email+" ?")
                                    // if the dialog is cancelable
                                    .setCancelable(false)
                                    // positive button text and action
                                    .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                                            dialog, id ->

                                        val url = HomeFragment.baseUrl +"/user/accept-friend?friend-email="+rList.get(position).request_email

                                        val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                                                response ->
                                            Log.d("jResponse",response.toString())
                                            val jResponse = GenericResponse<String>(JSONObject(response))

                                            if(jResponse.status==200){
                                                Toast.makeText(context, "Accepted "+rList.get(position).request_email+" to friend list", Toast.LENGTH_SHORT).show()
                                                rList.removeAt(position)
                                                requestListAdapter.notifyItemRemoved(position)

                                            } else {
                                                Toast.makeText(context, "Could not accept "+rList.get(position).request_email, Toast.LENGTH_SHORT).show()
                                            }

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
                                alert.setTitle("Accept Friend Alert")
                                // show alert dialog
                                alert.show()
                            }
                        })
                        requestRecycler.adapter = requestListAdapter


                        ///////////////////////////
                        // reject friend request
                        ///////////////////////////
                        requestListAdapter.setOnDeleteClickedListener(object: RequestAdapter.OnDeleteClickListener{
                            override fun onDeleteClick(position: Int) {
                                // alert dialog
                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(context)

                                // set message of alert dialog
                                dialogBuilder.setMessage("Do you want to reject "+rList.get(position).request_email+" ?")
                                    // if the dialog is cancelable
                                    .setCancelable(false)
                                    // positive button text and action
                                    .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                                            dialog, id ->

                                        val url = HomeFragment.baseUrl +"/user/remove-friend?friend-email="+rList.get(position).request_email

                                        val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                                                response ->
                                            Log.d("jResponse",response.toString())
                                            val jResponse = GenericResponse<String>(JSONObject(response))

                                            if(jResponse.status==200){
                                                Toast.makeText(context, "Rejected "+rList.get(position).request_email, Toast.LENGTH_SHORT).show()
                                                rList.removeAt(position)
                                                requestListAdapter.notifyItemRemoved(position)

                                            } else {
                                                Toast.makeText(context, "Could not reject "+rList.get(position).request_email, Toast.LENGTH_SHORT).show()
                                            }

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
                                alert.setTitle("Reject Friend Alert")
                                // show alert dialog
                                alert.show()
                            }
                        })
                        requestRecycler.adapter = requestListAdapter

                    } catch (e: Exception) {
                        Log.d("Exception", e.toString())
                    }
                }

                else{
                    Log.d("Request list", "is Null")
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
        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(stringRequest)
    }

    fun userToRequestCard(itemList: JSONArray): ArrayList<RequestCard> {
        val requestCardList = ArrayList<RequestCard>()
        var i = 0
        val numIterations = itemList.length()

        while (i < numIterations) {
            val requestObject = itemList.getJSONObject(i)
            val item = RequestCard()
            Log.d("requestObject is", requestObject.toString())
            item.request_name = requestObject.getString("username")
            item.request_email = requestObject.getString("email")
            requestCardList.add(item)
            i++
        }
        Log.d("Card List:", requestCardList.toString())
        return requestCardList
    }
}