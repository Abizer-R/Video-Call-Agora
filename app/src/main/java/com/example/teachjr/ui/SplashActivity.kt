package com.example.teachjr.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.example.teachjr.databinding.ActivitySplashBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.main.MainActivity
import com.example.teachjr.utils.Permissions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val TAG = SplashActivity::class.java.simpleName
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "SplashTesting: Splash Activity Created")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Permissions.requestPermission(this@SplashActivity)
        Permissions.requestStoragePermissions(this@SplashActivity)

        val mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}