package edu.ccsu.ritaapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_login.view.*
import android.app.Activity
import android.text.TextUtils
import android.text.util.Linkify
import android.widget.*
import edu.ccsu.ritaapp.MainActivity
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.regex.Pattern
import android.text.style.URLSpan
import android.text.SpannableString
import android.text.Html
import android.text.Spanned
import android.text.Spannable
import android.R
import android.net.NetworkRequest
import android.text.method.LinkMovementMethod
import android.webkit.WebViewClient
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.SignupActivity
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import org.json.JSONObject
import java.net.HttpCookie


class LoginFragment : Fragment(){


    private lateinit var loginViewModel: LoginViewModel
    private lateinit var _emailText: EditText
    private lateinit var _passwordText: EditText
    private lateinit var _loginButton: Button
    private lateinit var _signupButton: Button
    private lateinit var intent : Intent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        loginViewModel =
            ViewModelProviders.of(this).get(LoginViewModel::class.java)

        val view = inflater.inflate(edu.ccsu.ritaapp.R.layout.fragment_login, container, false)

        //get reference to all views
        _loginButton = view!!.btn_login as Button
        _passwordText = view!!.input_password as EditText
        _emailText = view!!.input_email as EditText
        _signupButton = view!!.btn_signup as Button


        _loginButton.setOnClickListener {
            login()
        }

        _signupButton!!.setOnClickListener {
            Log.d("test","ok")
            intent = Intent(super.getContext(), SignupActivity::class.java)
            startActivity(intent)

        }

        return view

    }


    fun login() {

        if (!validate()) {
            onLoginFailed()
            return
        }

        _loginButton!!.isEnabled = false

        val email = _emailText!!.text.toString()
        val password = _passwordText!!.text.toString()

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            val url = baseUrl+"/user/login"
            Log.d("url",url)

            val mStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener{
                response ->
                Log.d("jResponse",response.toString())
                val jResponse = GenericResponse<String>(JSONObject(response))

                if(jResponse.status==200){
                    Toast.makeText(this.context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                    intent = Intent(super.getContext(),MainActivity::class.java)
                    startActivity(intent)
                } else if(jResponse.status==304){
                    Toast.makeText(this.context, "Password is not correct", Toast.LENGTH_SHORT).show()
                    _loginButton!!.isEnabled = true
                } else if(jResponse.status ==404){
                    Toast.makeText(this.context, "We cannot find the email", Toast.LENGTH_SHORT).show()
                    _loginButton!!.isEnabled = true
                } else {
                    Toast.makeText(this.context, "We have a problem with login", Toast.LENGTH_SHORT).show()
                    _loginButton!!.isEnabled = true
                }
//                    Log.d(jResponse.toString(), "JSON Response")

            }, Response.ErrorListener{
                error->
                Log.d("Error :" , error.toString())
                Toast.makeText(this.context, "Cannot find matching password and username", Toast.LENGTH_SHORT).show()
                _loginButton!!.isEnabled = true

            }){
                override fun parseNetworkResponse(response: NetworkResponse): Response<String>? {

                    if(!response.headers.get("Authorization").toString().equals("null")) {
                        setCookie(response.headers.get("Authorization").toString())
                        Log.d("authorization",response.headers.get("Authorization").toString())
                    }
                   //TODO : SAVE key
                    return super.parseNetworkResponse(response)
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    val params2 = HashMap<String, String>()
                    params2.put("email",email )
                    params2.put("password", password)
                    return JSONObject(params2).toString().toByteArray()
                }
            }

            APIRequestSingleton.getInstance(super.getContext()!!)
                .addToRequestQueue(mStringRequest)
        } else {
            Toast.makeText(this.context,"Enter all details",Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateUI(status: String):View? {
        if(status.equals("loginSuccess")){
            Toast.makeText(this.context,"Login : success",Toast.LENGTH_SHORT).show()
            intent = Intent(super.getContext(),MainActivity::class.java)
            startActivity(intent)
        } else if(status.equals("signUp")){
            intent = Intent(super.getContext(), SignupActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun logout(){
//        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this.context,"Logging out... success",Toast.LENGTH_SHORT).show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == Activity.RESULT_OK) {
            }
        }
    }

    fun onLoginSuccess() {
        _loginButton!!.isEnabled = true
        Log.d("status","Login success")
        Toast.makeText(this.context,"Authentication succeeded",Toast.LENGTH_SHORT).show()
        updateUI("loginSuccess")
    }

    fun onLoginFailed() {
        Toast.makeText(this.context,"Authentication Failed.",Toast.LENGTH_SHORT).show()
        Log.d("status","Login failed")
        _loginButton!!.isEnabled = true

    }


    fun validate(): Boolean {
        var valid = true

        val email = _emailText!!.text.toString()
        val password = _passwordText!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText!!.error = "enter a valid email address"
            valid = false
        } else {
            _emailText!!.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            _passwordText!!.error = "over 8 alphanumeric characters"
            valid = false
        } else {
            _passwordText!!.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "LoginActivity"
        private val REQUEST_SIGNUP = 0
    }

    fun setCookie(_authorization_key : String){
        val httpCookie = HttpCookie("Authorization",_authorization_key)
        cookieManager.cookieStore.add(uri,httpCookie)
        Log.d("Cookie!!",cookieManager.cookieStore.get(uri).toString())
    }


}