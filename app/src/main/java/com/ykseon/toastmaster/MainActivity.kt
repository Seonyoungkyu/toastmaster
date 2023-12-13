package com.ykseon.toastmaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.ykseon.toastmaster.common.SharedState
import com.ykseon.toastmaster.common.SortOption
import com.ykseon.toastmaster.databinding.ActivityMainBinding
import com.ykseon.toastmaster.ui.record.RecordActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sharedState: SharedState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_meeting, R.id.nav_timer, R.id.nav_vote, R.id.nav_roles
            ) /*, drawerLayout*/
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        checkPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_show_record ) {
            ContextCompat.startActivity(
                this,
                Intent(this, RecordActivity::class.java),
                null
            )
        }
        else if(item.itemId == R.id.action_test_mode ) {
            sharedState.testMode.value = !sharedState.testMode.value
            if(sharedState.testMode.value) {
                item.title = resources.getString(R.string.action_normal_mode)
            }
            else {
                item.title = resources.getString(R.string.action_test_mode)
            }
        }
        else if (item.itemId == R.id.action_sort_name) {
            sharedState.sortOption.value = SortOption.ALPHABETICAL
        }
        else if (item.itemId == R.id.action_sort_role) {
            sharedState.sortOption.value = SortOption.ROLE
        }
        else if (item.itemId == R.id.action_sort_creation) {
            sharedState.sortOption.value = SortOption.CREATION
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkPermission() {
        if (!Settings.canDrawOverlays(this)) {

            val snackbar = Snackbar.make(
                this.window.decorView,
                "This application need 'appear on top' permission",
                Snackbar.LENGTH_INDEFINITE
            )

            snackbar.setAction("OK") {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }

            snackbar.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                checkPermission()
            } else {
                // Do as per your logic
            }
        }
    }

    companion object {
        const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469
    }
}