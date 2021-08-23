package com.example.netologydiploma.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.ActivityMainBinding
import com.example.netologydiploma.viewModel.AuthViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    fun setActionBarTitle(title: String) {
        binding.mainToolbar.title = title
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseMessaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    this.localClassName,
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            checkGoogleApiAvailability()
        }


        // initialize navController
        navController = findNavController(R.id.nav_host_fragment_container)

        val toolbar = binding.mainToolbar
        setSupportActionBar(toolbar)


        val bottomNavView = binding.bottomNavView.apply {
            // removes all the unnecessary shadows when bottomNavView is positioned above the
            // bottomAppBar
            background = null
            menu.findItem(R.id.blank_item).isEnabled = false
        }

        // hides top back arrow from these destinations
        val topLevelDestinations = setOf(
            R.id.nav_posts_fragment,
            R.id.nav_events_fragment,
            R.id.nav_profile_fragment,
            R.id.nav_users_fragment
        )

        appBarConfiguration = AppBarConfiguration.Builder(topLevelDestinations).build()
        NavigationUI.setupActionBarWithNavController(
            this, navController,
            appBarConfiguration
        )


        // we don't want to show appBar during registration / authentication
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {

                R.id.logInFragment -> {
                    toolbar.visibility = View.GONE
                    bottomNavView.visibility = View.GONE
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabCreateLayout.visibility = View.GONE
                }
                R.id.registrationFragment -> {
                    toolbar.visibility = View.GONE
                    binding.bottomAppBar.visibility = View.GONE
                    bottomNavView.visibility = View.GONE
                    binding.fabCreateLayout.visibility = View.GONE
                }
                R.id.createEditPostFragment -> {
                    bottomNavView.visibility = View.GONE
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabCreateLayout.visibility = View.GONE
                }
                R.id.createEventFragment -> {
                    bottomNavView.visibility = View.GONE
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabCreateLayout.visibility = View.GONE
                }

                else -> {
                    toolbar.visibility = View.VISIBLE
                    binding.bottomAppBar.visibility = View.VISIBLE
                    bottomNavView.visibility = View.VISIBLE
                    binding.fabCreateLayout.visibility = View.VISIBLE
                }

            }
        }

        bottomNavView.setupWithNavController(navController)

        binding.fabAddPost.setOnClickListener {
            navController.navigate(R.id.createEditPostFragment)
        }

        binding.fabAddEvent.setOnClickListener {
            navController.navigate(R.id.createEventFragment)
        }

        // redraw menu when authState changes
        viewModel.authState.observe(this)
        { user ->

            // ensure we automatically show login fragment only once at app
            // launch and not every time the activity is recreated
            if (!viewModel.checkIfAskedToLogin && user.id == 0L) {
                navController.navigate(R.id.logInFragment)
                viewModel.setCheckIfAskedLoginTrue()
            }

            if (user.id == 0L) {
                invalidateOptionsMenu()
                binding.bottomNavView.menu.findItem(R.id.nav_profile_fragment).isEnabled = false
                binding.expandableFab.visibility = View.GONE
            } else {
                invalidateOptionsMenu()
                binding.bottomNavView.menu.findItem(R.id.nav_profile_fragment).isEnabled = true
                binding.expandableFab.visibility = View.VISIBLE
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.auth_app_bar_menu, menu)
        menu?.setGroupVisible(R.id.group_sign_in, !viewModel.isAuthenticated)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_in -> {
                navController.navigate(R.id.logInFragment)
                true
            }
            else -> false
        }
    }


    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@MainActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@MainActivity, code, 9000).show()
                return
            }
            Toast.makeText(this@MainActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
            googleApiAvailability.makeGooglePlayServicesAvailable(this@MainActivity)
        }
    }
}