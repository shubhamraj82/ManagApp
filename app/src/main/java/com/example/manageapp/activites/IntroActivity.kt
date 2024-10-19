package com.example.manageapp.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.manageapp.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val signUpButton:Button=findViewById(R.id.btn_sign_up_intro)
        signUpButton.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))

            val signInButton:Button=findViewById(R.id.btn_sign_in_intro)
            signInButton.setOnClickListener {
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
    }
}