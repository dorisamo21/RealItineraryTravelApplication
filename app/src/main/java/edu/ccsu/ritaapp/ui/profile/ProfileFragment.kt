package edu.ccsu.ritaapp.ui.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.MainActivity
import edu.ccsu.ritaapp.R
import android.widget.RadioGroup
import edu.ccsu.ritaapp.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_profile.view.*
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import org.json.JSONObject
import androidx.fragment.app.FragmentActivity
import edu.ccsu.ritaapp.JWTUtils

class ProfileFragment : Fragment() {

    private lateinit var _emailText: EditText
    private lateinit var _passwordText: EditText
    private lateinit var _usernameText: EditText
    private lateinit var _updateButton : Button
    private lateinit var viewModel: ProfileViewModel
    private lateinit var sharedPref : SharedPreferences
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var email : String
    private lateinit var preference : String
    private val value2 = cookieManager.cookieStore.get(uri).toString().replace("[","").replace("]","").replace("Authorization=","").replace("\"","")
    val dummy  = JSONObject("""{"email":"", "username":"","preferences":"1111111111111111","friends":null}""")
    private companion object {
        private lateinit var user: User
    }
//    private lateinit var editor : SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.activity!!.getPreferences(Context.MODE_PRIVATE) ?: return

        with (sharedPref.edit()){
            putString("PreferenceString", sharedPref.getString("PreferenceString","1111111111111111"))
            commit()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        _passwordText = view!!.input_profile_password as EditText
        _emailText = view!!.input_profile_email as EditText
        _usernameText = view!!.input_name as EditText
        _updateButton = view!!.btn_update as Button
        _emailText.setInputType(0)

        if(!cookieManager.cookieStore.get(uri).isEmpty()) {
            getUserInfo()            //Getting data from back end
            setUpPreferences(view, user.preferences)
        }


        _updateButton.setOnClickListener {
            //TODO : alertdialog
            password = _passwordText!!.text.toString()
            username = _usernameText!!.text.toString()
            email    = _emailText.text.toString()

            updateProfile()
        }
        return view
    }

    fun getUserInfo(){
        // initializing user
        user = User(dummy)
        val url = HomeFragment.baseUrl +"/user/user-details"

        val mStringRequest = object : StringRequest(Request.Method.GET, url, Response.Listener{
                response ->
            Log.d("jResponse",response.toString())
            val jResponse = GenericResponse<User>(JSONObject(response))

            if(jResponse.status==200){
                val genericResponse = GenericResponse<User>(jResponse.obj)
                try {
                    user = User(genericResponse.obj.getJSONObject("body"))
                    _emailText.setText(user.email)
                    _usernameText.setText(user.username)
                } catch(e: Exception){
                    Log.d("exception",e.toString())
                }
            } else {
                Toast.makeText(this.context, "We have a problem getting user information", Toast.LENGTH_SHORT).show()
            }
            Log.d(jResponse.toString(), "JSON Response")
            Log.d("user outside",user.toString())

        }, Response.ErrorListener{
                error->
            Log.d("Error :" , error.toString())
            Log.d("param2 value",value2)
            Toast.makeText(this.context, "Cannot find matching email", Toast.LENGTH_SHORT).show()
            _updateButton!!.isEnabled = true


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
        Log.d("user when return",user.toString())

        APIRequestSingleton.getInstance(super.getContext()!!)
            .addToRequestQueue(mStringRequest)
//        return user
    }


    fun setUpPreferences(view:View, pref : String){

        sharedPref = this.activity!!.getPreferences(Context.MODE_PRIVATE) ?: return

        var rg = view.findViewById<RadioGroup>(R.id.RG_museums)

        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view.findViewById(R.id.RG_tours)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_markets)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_family)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_landmarks)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_community)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_sports)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_shopping)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_food_n_drink)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_concerts_festivals)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_music_performances)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_theaters_shows_expos)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_nightlife_entertainment)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_natural_geographical)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_accommodation)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        rg = view!!.findViewById(R.id.RG_outdoor_parks_zoos)
        rg.check(sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId))

        setListeners(view!!)
    }


    private fun setListeners(root: View){

        var rg = root.findViewById<RadioGroup>(R.id.RG_museums)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_tours)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_markets)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_family)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_landmarks)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_community)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_sports)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_shopping)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_food_n_drink)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_concerts_festivals)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_music_performances)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_theaters_shows_expos)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_nightlife_entertainment)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_natural_geographical)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_accommodation)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}

        rg = root.findViewById(R.id.RG_outdoor_parks_zoos)
        rg.setOnCheckedChangeListener{view, i -> onRadioGroupClicked(view, i)}
    }


    private fun onRadioGroupClicked(view: View, i: Int){
        val rg = view as RadioGroup
        val rb0 = rg.getChildAt(1)
        val rb1 = rg.getChildAt(2)
        val rb2 = rg.getChildAt(3)

        val sharedPref = this.activity!!.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt(view.tag.toString(), rg.checkedRadioButtonId)

            var prefString = sharedPref.getString("PreferenceString", "2211111111111111")!!
            val position = rg.tag.toString().substring(0..1).toInt()
            when (i.toString()) {
                rb0.id.toString() -> prefString = prefString.replaceRange(position..position, "0")
                rb1.id.toString() -> prefString = prefString.replaceRange(position..position, "1")
                rb2.id.toString() -> prefString = prefString.replaceRange(position..position, "2")
            }
            putString("PreferenceString", prefString)
            commit()
        }
        Log.d("checked", sharedPref.getInt(rg.tag.toString(), rg.checkedRadioButtonId).toString())
    }

    private fun updateProfile(){
        if (!validate()) {
            onUpdateFail()
            return
        }

        _updateButton.isEnabled = false

        val url = HomeFragment.baseUrl +"/user/profile"

        val mStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener{
                response ->
            Log.d("jResponse",response.toString())
            val jResponse = GenericResponse<String>(JSONObject(response))

            if(jResponse.status==200){
                Toast.makeText(this.context, "Profile was updated successfully", Toast.LENGTH_SHORT).show()
                _updateButton!!.isEnabled = true
            } else {
                Toast.makeText(this.context, "There is a problem updating the profile", Toast.LENGTH_SHORT).show()
                _updateButton!!.isEnabled = true
            }
            Log.d(jResponse.toString(), "JSON Response")

        }, Response.ErrorListener{
                error->
            Log.d("Error :" , error.toString())
            Toast.makeText(this.context, "Profile update failed", Toast.LENGTH_SHORT).show()
            _updateButton!!.isEnabled = true

        }){
            override fun parseNetworkResponse(response: NetworkResponse): Response<String>? {
                return super.parseNetworkResponse(response)
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val params2 = HashMap<String, String>()
                params2.put("email",email)
                params2.put("username",username )
                params2.put("password", password)
                params2.put("preference",sharedPref.getString("PreferenceString","1111111111111111"))
                return JSONObject(params2).toString().toByteArray()
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

    fun validate(): Boolean {
        var valid = true

        val password = _passwordText!!.text.toString()
        val username = _usernameText!!.text.toString()

        if (username.isEmpty() || password.length < 3) {
            _usernameText!!.error = "username has to be over 3 alphanumeric characters"
            valid = false
        } else {
            _usernameText!!.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            _passwordText!!.error = "password has to be over 8 alphanumeric characters"
            valid = false
        } else {
            _passwordText!!.error = null
        }

        return valid
    }

    fun onUpdateFail() {
        Toast.makeText(this.context,"Update Failed.",Toast.LENGTH_SHORT).show()
        Log.d("status","update failed")
        _updateButton!!.isEnabled = true

    }

    private fun getEmail(){
        val my_email = JWTUtils.decoded(value2)
        Log.d("decoded",my_email)
    }
}
