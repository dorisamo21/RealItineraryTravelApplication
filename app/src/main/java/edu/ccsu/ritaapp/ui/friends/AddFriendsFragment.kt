package edu.ccsu.ritaapp.ui.friends

import RecyclerAdapter
import android.content.Intent
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
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.APIModels.APIEntity
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.MainActivity
import edu.ccsu.ritaapp.R
import kotlinx.android.synthetic.main.fragment_friends.*
import org.json.JSONArray
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import kotlinx.android.synthetic.main.fragment_add_friends.view.*
import org.json.JSONObject
import org.w3c.dom.Text

class AddFriendsFragment : Fragment() {

    private val authenticationKey = cookieManager.cookieStore.get(uri).toString().replace("[","").replace("]","").replace("Authorization=","").replace("\"","")
    private lateinit var _searchButton : Button
    private lateinit var _emailText : EditText
    private lateinit var _result_message : TextView
    private lateinit var _result_frame : LinearLayout
    private lateinit var _result_email : TextView
    private lateinit var _result_username : TextView
    private lateinit var _request_friend_btn : Button

    private val value2 = cookieManager.cookieStore.get(uri).toString().replace("[","").replace("]","").replace("Authorization=","").replace("\"","")
    private lateinit var friend : User



    private lateinit var viewModel: FriendsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_add_friends, container, false)

        _searchButton =  view!!.search_btn as Button
        _emailText  = view!!.emailSearch as EditText
        _result_frame = view!!.result as LinearLayout
        _result_message = view!!.result_message as TextView
        _result_email = view!!.result_email as TextView
        _result_username = view!!.result_username as TextView
        _result_frame.setVisibility(View.GONE)
        _result_message.setVisibility(View.GONE)
        _request_friend_btn = view!!.request_friend_btn as Button

        _searchButton!!.setOnClickListener {

                val url = baseUrl+"/user/user-search?friend-query="+_emailText.text.toString()
                val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                        response ->
                    val genericResponse = GenericResponse<List<User>>(JSONObject(response))

                    if(genericResponse.status==200){
                            try {
                                val user = User(genericResponse.obj.getJSONArray("body").get(0) as JSONObject)

                                _searchButton!!.isEnabled = true
                                _result_message.text = "Found the friend"
                                _result_email.text = user.email
                                _result_username.text = user.username
                                _result_frame.setVisibility(View.VISIBLE)
                                _result_message.setVisibility(View.VISIBLE)
                            } catch(e: Exception){
                                Log.d("exception",e.toString())
                            }

                    } else if(genericResponse.status==404) {
                        _result_message.text = "Could not find the user "+_emailText.text.toString()
                        _result_message.setVisibility(View.VISIBLE)
                        Toast.makeText(this.context, "No such a user", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this.context, "Bad Response", Toast.LENGTH_SHORT).show()
//                        _searchButton!!.isEnabled = true
                    }

                }, Response.ErrorListener{
                        error->
                    Log.d("Error :" , error.toString())
                    Toast.makeText(this.context, "Server Error ", Toast.LENGTH_SHORT).show()
 //                   _searchButton!!.isEnabled = true

                }){
                    override fun getBodyContentType(): String {
                        return "application/json"
                    }

                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): MutableMap<String, String>? {
                        val params2 = HashMap<String, String>()
                        Log.d("param2 value",value2)
                        params2.put("Authorization",value2)
                        return params2
                    }

                }

                APIRequestSingleton.getInstance(super.getContext()!!)
                    .addToRequestQueue(mStringRequest)

        }


        _request_friend_btn!!.setOnClickListener {

            val url = baseUrl+"/user/request-friend?friend-email="+_emailText.text.toString()
            val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                    response ->
                      Toast.makeText(this.context, "Friend request has been sent", Toast.LENGTH_SHORT).show()
                      _result_frame.setVisibility(View.GONE)
                      _result_message.setVisibility(View.GONE)
                      _emailText.text = null


            }, Response.ErrorListener{
                    error->
                Log.d("Error :" , error.toString())
                Toast.makeText(this.context, "Server Error ", Toast.LENGTH_SHORT).show()
                _searchButton!!.isEnabled = true

            }){
                override fun getBodyContentType(): String {
                    return "application/json"
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): MutableMap<String, String>? {
                    val params2 = HashMap<String, String>()
                    Log.d("param2 value",value2)
                    params2.put("Authorization",value2)
                    return params2
                }

            }

            APIRequestSingleton.getInstance(super.getContext()!!)
                .addToRequestQueue(mStringRequest)

        }

        return view
    }

    private fun validate():Boolean{
        var valid = true
        val email = _emailText.toString()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText!!.error = "enter a valid email address"
            valid = false
        } else {
            _emailText!!.error = null
        }

        return valid

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FriendsViewModel::class.java)
    }

}
