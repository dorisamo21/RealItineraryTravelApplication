package edu.ccsu.ritaapp.ui.signup

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import edu.ccsu.ritaapp.MainActivity

import edu.ccsu.ritaapp.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_signup.view.*
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import androidx.annotation.NonNull
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import edu.ccsu.ritaapp.APIModels.GenericResponse
import edu.ccsu.ritaapp.APIModels.User
import edu.ccsu.ritaapp.APIRequestSingleton
import edu.ccsu.ritaapp.LoginActivity
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject


class SignupFragment : Fragment() {

    private lateinit var signupViewModel: SignupViewModel

    var _nameText: EditText? = null
    var _emailText: EditText? = null
    var _passwordText: EditText? = null
    var _reEnterPasswordText: EditText? = null
    var _signupButton: Button? = null
    var _loginLink: TextView? = null
    private lateinit var intent : Intent

    //Firebase Auth
    private var mAuth: FirebaseAuth? = null
    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    val userRef = mDatabase.getReference("users")

    companion object {
        fun newInstance() = SignupFragment()
        private val TAG = "SignupActivity"
    }

    private lateinit var viewModel: SignupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        signupViewModel =
            ViewModelProviders.of(this).get(SignupViewModel::class.java)

        val view = inflater.inflate(edu.ccsu.ritaapp.R.layout.fragment_signup, container, false)


        _nameText = view!!.input_name as EditText
        _emailText = view!!.input_email as EditText
        _passwordText = view!!.input_password as EditText
        _reEnterPasswordText = view!!.input_reEnterPassword as EditText

        _signupButton = view!!.btn_signup as Button
        _loginLink = view!!.link_login as TextView

        _signupButton!!.setOnClickListener {
            signup()
        }

        _loginLink!!.setOnClickListener{
            val loginIntent = Intent(super.getContext(), LoginActivity::class.java)
            startActivity(loginIntent)
        }


        return view
    }

    private fun signup() {
        Log.d(TAG, "Signup")
        mAuth = FirebaseAuth.getInstance()

        if (!validate()) {
            onSignupFailed()
            return
        }

        _signupButton!!.isEnabled = false

        val name = _nameText!!.text.toString()
        val email = _emailText!!.text.toString()
        val password = _passwordText!!.text.toString()
        val reEnterPassword = _reEnterPasswordText!!.text.toString()

        // TODO : implement - user data insert
        //TODO : mAuth.currentUser being different from entered user info
        if(!email.isEmpty() && !name.isEmpty() && !password.isEmpty()){
            val url = baseUrl+"/user/registration"
            Log.d("registration url",url)
            val mStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener{
                    response ->
                val jResponse = GenericResponse<User>(JSONObject(response))
                    Log.d("response",jResponse.toString())
                if(jResponse.status==200){
                    //TODO : authentication
                    Toast.makeText(this.context, "Successfully registered, ", Toast.LENGTH_SHORT).show()
                    val loginIntent = Intent(super.getContext(), LoginActivity::class.java)
                    startActivity(loginIntent)

                } else if(jResponse.status==304){
                    Toast.makeText(this.context, "Email already exists ", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this.context, "We have a problem with registration", Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener{
                    error->
                Log.d("Error :" , error.toString())
                Toast.makeText(this.context, "We have a problem with registration", Toast.LENGTH_SHORT).show()
            }){
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    val params2 = HashMap<String, String>()
                    params2.put("email",email )
                    params2.put("username",name)
                    params2.put("password", password)
                    return JSONObject(params2).toString().toByteArray()
                }
            }

            APIRequestSingleton.getInstance(super.getContext()!!)
                .addToRequestQueue(mStringRequest)

        } else {
            Toast.makeText(this.context,"Please fill up the Credentials :|", Toast.LENGTH_LONG).show()
        }

    }



    fun validate(): Boolean {
        var valid = true

        val name = _nameText!!.text.toString()
        val email = _emailText!!.text.toString()
        val password = _passwordText!!.text.toString()
        val reEnterPassword = _reEnterPasswordText!!.text.toString()

        if (name.isEmpty() || name.length < 3) {
            _nameText!!.error = "at least 3 characters"
            valid = false
        } else {
            _nameText!!.error = null
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText!!.error = "enter a valid email address"
            valid = false
        } else {
            _emailText!!.error = null
        }

        if (password.isEmpty() || password.length < 4 || password.length > 10) {
            _passwordText!!.error = "between 4 and 10 alphanumeric characters"
            valid = false
        } else {
            _passwordText!!.error = null
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length < 4 || reEnterPassword.length > 10 || reEnterPassword != password) {
            _reEnterPasswordText!!.error = "Password Do not match"
            valid = false
        } else {
            _reEnterPasswordText!!.error = null
        }

        return valid
    }


    fun onSignupSuccess() {
        _signupButton!!.isEnabled = true
        Toast.makeText(this.context, "Successfully registered", Toast.LENGTH_LONG).show()

//        setResult(Activity.RESULT_OK, null)
        startActivity(Intent(super.getContext(), MainActivity::class.java))
//        finish()
    }

    fun onSignupFailed() {
        Toast.makeText(this.context, "Registration failed", Toast.LENGTH_LONG).show()

        _signupButton!!.isEnabled = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignupViewModel::class.java)
    }



}
