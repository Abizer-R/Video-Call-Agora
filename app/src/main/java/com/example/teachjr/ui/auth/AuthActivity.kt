package com.example.teachjr.ui.auth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.teachjr.R
import com.example.teachjr.databinding.ActivityAuthBinding
import com.example.teachjr.ui.student.StudentActivity
import com.example.teachjr.utils.ConnectionLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val TAG = AuthActivity::class.java.simpleName
    private lateinit var binding: ActivityAuthBinding

    private lateinit var connectionLiveData: ConnectionLiveData
    private var connectionStatus = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * We cant use Network callback and connectivity manager until onCreate is called
         */
        connectionLiveData = ConnectionLiveData(this)

        connectionLiveData.observe(this) { isConnected ->
            /** we need to check if it is not already true.
             * Otherwise, this will get triggered on activity's launch as well.
             */
            if(isConnected && connectionStatus == false) {
                Log.i(TAG, "AuthTesting: Internet Available")
                connectionStatus = true
                updateConnectionTextView(true)

            } else if(!isConnected){
                Log.i(TAG, "AuthTesting: Internet NOT Available")
                connectionStatus = false
                updateConnectionTextView(false)
            }
        }

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