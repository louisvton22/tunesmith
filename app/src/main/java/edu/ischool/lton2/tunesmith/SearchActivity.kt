package edu.ischool.lton2.tunesmith

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchActivity : AppCompatActivity() {
    lateinit var bottomNav : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val homeIntent = Intent(this, HomeActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.nav_search -> {
                    val searchIntent = Intent(this, SearchActivity::class.java)
                    startActivity(searchIntent)
                    true
                }
                else -> {true}
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu with items using MenuInflator
        val inflater = menuInflater
        inflater.inflate(R.menu.top_menu, menu)

        // Initialise menu item search bar
        // with id and take its object
        val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView

        // attach setOnQueryTextListener
        // to search view defined above

//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            // Override onQueryTextSubmit method which is call when submit query is searched
//            override fun onQueryTextSubmit(query: String): Boolean {
//                // If the list contains the search query than filter the adapter
//                // using the filter method with the query as its argument
//                if (mylist.contains(query)) {
//                    adapter.filter.filter(query)
//                } else {
//                    // Search query not found in List View
//                    Toast.makeText(this@MainActivity, "Not found", Toast.LENGTH_LONG).show()
//                }
//                return false
//            }
//
//            // This method is overridden to filter the adapter according
//            // to a search query when the user is typing search
//            override fun onQueryTextChange(newText: String): Boolean {
//                adapter.filter.filter(newText)
//                return false
//            }
//        })
        return super.onCreateOptionsMenu(menu)
    }
}