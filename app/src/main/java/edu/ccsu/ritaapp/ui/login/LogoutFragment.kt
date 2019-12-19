package edu.ccsu.ritaapp.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.ccsu.ritaapp.MainActivity
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri
import java.net.HttpCookie

class LogoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        delCookie()
        Toast.makeText(this.context,"Logging out...", Toast.LENGTH_SHORT).show()
        val intent = Intent(super.getContext(), MainActivity::class.java)
        startActivity(intent)
        return null
    }
    fun delCookie(){

        val cookieList = cookieManager.cookieStore.get(uri)

        val cookie = HttpCookie("Authorization",cookieManager.cookieStore.get(uri).toString())
        cookieManager.cookieStore.remove(uri,cookie)
    }
}