package com.example.manageapp.activites

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.manageapp.R
import com.example.manageapp.firebase.FireStoreClass

class SplashScreen : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Use Handler with Looper.getMainLooper() to avoid using deprecated constructor
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUserID = FireStoreClass().getCurrentuserId()
            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 3000)
    }
}
