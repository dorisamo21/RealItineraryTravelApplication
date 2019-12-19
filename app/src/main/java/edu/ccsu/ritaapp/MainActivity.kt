package edu.ccsu.ritaapp

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import edu.ccsu.ritaapp.R
import com.google.android.material.navigation.NavigationView
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.baseUrl
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.cookieManager
import edu.ccsu.ritaapp.ui.home.HomeFragment.Companion.uri

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navView: NavigationView
    var login = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navView = findViewById(R.id.nav_view)
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navController = findNavController(R.id.nav_host_fragment)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_maps,
                    R.id.nav_itinerary_init,
                    R.id.nav_profile,
                    R.id.nav_friends
                ), drawerLayout
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        login = !cookieManager.cookieStore.get(uri).isEmpty()
        displayLogInOut(login)
    }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menuInflater.inflate(R.menu.main, menu)
            return true
        }

        override fun onSupportNavigateUp(): Boolean {
            val navController = findNavController(R.id.nav_host_fragment)
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }

    private fun displayLogInOut(login : Boolean){
        val nav_menu: Menu = navView.getMenu()
        if(login){
            nav_menu.findItem(R.id.nav_login).setVisible(false)
            nav_menu.findItem(R.id.nav_signup).setVisible(false)
            nav_menu.findItem(R.id.nav_logout).setVisible(true)
            nav_menu.findItem(R.id.edit_profile).setVisible(true)
            nav_menu.findItem(R.id.nav_friends).setVisible(true)
            nav_menu.findItem(R.id.edit_itinerary).setVisible(true)
            nav_menu.findItem(R.id.nav_add_friend).setVisible(false)
            nav_menu.findItem(R.id.nav_friend_list).setVisible(false)

        }else {
            nav_menu.findItem(R.id.nav_login).setVisible(true)
            nav_menu.findItem(R.id.nav_signup).setVisible(true)
            nav_menu.findItem(R.id.nav_logout).setVisible(false)
            nav_menu.findItem(R.id.edit_profile).setVisible(false)
            nav_menu.findItem(R.id.nav_friends).setVisible(false)
            nav_menu.findItem(R.id.edit_itinerary).setVisible(false)
            nav_menu.findItem(R.id.nav_add_friend).setVisible(false)
            nav_menu.findItem(R.id.nav_friend_list).setVisible(false)
        }
    }
}
