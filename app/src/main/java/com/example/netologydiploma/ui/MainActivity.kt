package com.example.netologydiploma.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.ActivityMainBinding
import com.example.netologydiploma.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()


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

        // redraw menu when authState changes
        viewModel.authState.observe(this) {
            invalidateOptionsMenu()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.auth_app_bar_menu, menu)

        menu?.setGroupVisible(R.id.group_sign_in, !viewModel.isAuthenticated)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_in -> {
                findNavController(R.id.nav_host_fragment_container).navigate(R.id.logInFragment)
                true
            }
            else -> false
        }
    }
}