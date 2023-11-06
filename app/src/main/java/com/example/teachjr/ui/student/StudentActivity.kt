package com.example.teachjr.ui.student

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.ActivityStudentBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.utils.ConnectionLiveData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class StudentActivity : AppCompatActivity() {

    private val TAG = StudentActivity::class.java.simpleName
    private lateinit var binding: ActivityStudentBinding

    private lateinit var connectionLiveData: ConnectionLiveData
    private var connectionStatus = true

//    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.i(TAG, "StudentTesting: Student Activity Created")

//        binding.btnLogout.setOnClickListener {
//            val firebaseAuth = FirebaseAuth.getInstance()
//            firebaseAuth.signOut()
//            Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
//
//            val intent = Intent(this, AuthActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(intent)
//            finish()
//        }

        /**
         * We cant use Network callback and connectivity manager until onCreate is called
         */
        connectionLiveData = ConnectionLiveData(this)

        connectionLiveData.observe(this) { isConnected ->
            /** we need to check if it is not already true.
             * Otherwise, this will get triggered on activity's launch as well.
             */
           if(isConnected && connectionStatus == false) {
                Log.i(TAG, "StudentTesting_ACTIVITY: Internet Available")
                connectionStatus = true
                updateConnectionTextView(true)

            } else if(!isConnected){
                Log.i(TAG, "StudentTesting_ACTIVITY: Internet NOT Available")
                connectionStatus = false
                updateConnectionTextView(false)
            }
        }

//        /**
//         * Implementing Up Navigation button
//         */
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.stdFragmentContainerView) as NavHostFragment
//        val navController = navHostFragment.navController
//        val appBarConfiguration = AppBarConfiguration
//            .Builder(
//                R.id.stdHomeFragment,
//                R.id.stdMarkAtdFragment
//            )
//            .build()
//        // Check if androidx.navigation.ui.NavigationUI.setupActionBarWithNavController is imported
//        // By default title in actionbar is used from the fragment label in navigation graph
//        // To use the app name, remove label else if you want to add customized label specify it there
//        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun updateConnectionTextView(isConnected: Boolean) {
        if(isConnected) {
            binding.tvNoConnection.setBackgroundColor(Color.parseColor("#419b45"))
            binding.tvNoConnection.text = "Back Online"
            lifecycleScope.launch(Dispatchers.IO) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    binding.tvNoConnection.visibility = View.GONE
                }
            }

        } else {
            binding.tvNoConnection.setBackgroundColor(Color.parseColor("#ED4134"))
            binding.tvNoConnection.text = "No Connection"
            binding.tvNoConnection.visibility = View.VISIBLE
        }
    }
}