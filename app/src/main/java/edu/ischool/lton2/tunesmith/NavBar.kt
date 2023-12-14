package edu.ischool.lton2.tunesmith

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

interface NavBar {
    fun setupNav(activity: Activity, checkedItemValue: Int) {
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNav.selectedItemId = checkedItemValue
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val homeIntent = Intent(activity, HomeActivity::class.java)
                    activity.startActivity(homeIntent)
                    true
                }
                R.id.nav_search -> {
                    val searchIntent = Intent(activity, SearchActivity::class.java)
                    activity.startActivity(searchIntent)
                    true
                }
                else -> {true}
            }
        }
    }
}