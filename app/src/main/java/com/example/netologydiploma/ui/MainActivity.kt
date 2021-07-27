package com.example.netologydiploma.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize navController
        navController = findNavController(R.id.nav_host_fragment_container)
        // Connect Drawer layout to the navigation graph
        NavigationUI.setupWithNavController(binding.drawerNavView, navController)
        binding.drawerNavView.setupWithNavController(navController)
        // automatically changes appBar title according to fragment label in nav_graph
        NavigationUI.setupActionBarWithNavController(this, navController, binding.mainDrawerLayout)

        // navigate to user profile once header is clicked
        binding.drawerNavView.getHeaderView(0).setOnClickListener{
            navController.navigate(R.id.nav_profile_fragment)
            if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.mainDrawerLayout)
    }
}