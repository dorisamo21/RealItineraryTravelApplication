package edu.ccsu.ritaapp.ui.friends

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.R
import kotlinx.android.synthetic.main.fragment_friends.*
import org.json.JSONArray
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import org.json.JSONObject

class FriendsFragment : Fragment() {

    private val authenticationKey =
        cookieManager.cookieStore.get(uri).toString().replace("[", "").replace("]", "")
            .replace("Authorization=", "").replace("\"", "")


    private lateinit var viewModel: FriendsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_friends, container, false)


        viewModel = ViewModelProviders.of(this).get(FriendsViewModel::class.java)

        val friendRecycler: RecyclerView = root.findViewById(R.id.friendRecycler)
        friendRecycler.layoutManager = LinearLayoutManager(this.context)

        val addFriends: Button = root.findViewById(R.id.add_friends)

        root.findViewById<Button>(R.id.my_friends).isEnabled = false

        createFriendList()

        //Friends list
        root.findViewById<Button>(R.id.my_friends).setOnClickListener {

            root.findViewById<Button>(R.id.my_friends).isEnabled = false
            root.findViewById<Button>(R.id.requests).isEnabled = true
            addFriends.isEnabled = true

            val reqFrag = FriendsListFragment()
            val fragment_transaction = fragmentManager!!.beginTransaction()
            fragment_transaction.replace(R.id.fragment_friends, reqFrag)
            fragment_transaction.addToBackStack(null)
            fragment_transaction.commit()
            Log.d("msg", "my friends button clicked")
        }


        //Requested Friends list
        root.findViewById<Button>(R.id.requests).setOnClickListener {
            friendRecycler.visibility = View.GONE
            root.findViewById<Button>(R.id.requests).isEnabled = false
            root.findViewById<Button>(R.id.my_friends).isEnabled = true
            addFriends.isEnabled = true


            val reqFrag = RequestsFragment()
            val fragment_transaction = fragmentManager!!.beginTransaction()
            fragment_transaction.replace(R.id.fragment_friends, reqFrag)
            fragment_transaction.addToBackStack(null)
            fragment_transaction.commit()
            Log.d("msg", "request button clicked")
        }

        //Add Friends page
        addFriends.setOnClickListener {
            friendRecycler.visibility = View.GONE
            addFriends.isEnabled = false
            root.findViewById<Button>(R.id.my_friends).isEnabled = true
            root.findViewById<Button>(R.id.requests).isEnabled = true


            val reqFrag = AddFriendsFragment()
            val fragment_transaction = fragmentManager!!.beginTransaction()
            fragment_transaction.replace(R.id.fragment_friends, reqFrag)
            fragment_transaction.addToBackStack(null)
            fragment_transaction.commit()
            Log.d("msg", "add friends button clicked")
        }

        return root
    }

    fun createFriendList() {
        Log.d("Get Friends API", "Requests the friends")
        val url = "https://rita-server.herokuapp.com/user/user-friends"

        val stringRequest = object: StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val jResponse = JSONObject(response)

                val genericResponse = GenericResponse<List<User>>(jResponse)

                if(genericResponse.status==200) {
                    try {


                        val friendList = genericResponse.obj.getJSONArray("body")
                        val fList1 = userToFriendCard(friendList)
                        val friendListAdapter = FriendAdapter(fList1)

                        friendListAdapter.setOnItemClickedListener(object: FriendAdapter.OnItemClickListener{
                            override fun onItemClick(position: Int) {
                                // alert dialog
                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(context)

                                // set message of alert dialog
                                dialogBuilder.setMessage("Do you want to delete "+fList1.get(position).friend_email+" ?")
                                    // if the dialog is cancelable
                                    .setCancelable(false)
                                    // positive button text and action
                                    .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                                            dialog, id ->

                                val url = baseUrl+"/user/remove-friend?friend-email="+fList1.get(position).friend_email

                                val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                                        response ->
                                    Log.d("jResponse",response.toString())
                                    val jResponse = GenericResponse<String>(JSONObject(response))

                                    if(jResponse.status==200){
                                        Toast.makeText(context, "Deleted "+fList1.get(position).friend_email+" from friend list", Toast.LENGTH_SHORT).show()
                                        fList1.removeAt(position)
                                        friendListAdapter.notifyItemRemoved(position)

                                    } else {
                                        Toast.makeText(context, "Could not delete "+fList1.get(position).friend_email+" from friend list", Toast.LENGTH_SHORT).show()
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
                                        Log.d("param2 value",authenticationKey)
                                        params2.put("Authorization",authenticationKey)
                                        return params2
                                    }

                                }

                                context?.let {
                                    APIRequestSingleton.getInstance(it)
                                        .addToRequestQueue(mStringRequest)
                                } })
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
                                Log.d("removeFriendEmail",fList1.get(position).friend_email)

                            }
                        })
                        friendRecycler.adapter = friendListAdapter

                    } catch (e: Exception) {
                        Log.d("Exception", e.toString())
                    }
                }

                else{
                    Log.d("Friend list", "is Null")
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

    fun userToFriendCard(itemList: JSONArray): ArrayList<FriendCard> {
        val friendCardList = ArrayList<FriendCard>()
        var i = 0
        val numIterations = itemList.length()

        while (i < numIterations) {
            val friendObject = itemList.getJSONObject(i)
            val item = FriendCard()
            Log.d("friendObject is", friendObject.toString())
            item.friend_name = friendObject.getString("username")
            item.friend_email = friendObject.getString("email")
            friendCardList.add(item)
            i++
        }
        Log.d("Card List:", friendCardList.toString())
        return friendCardList
    }
}
