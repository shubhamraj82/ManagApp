package com.example.manageapp.activites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.manageapp.R
import com.google.firebase.auth.FirebaseAuth
import com.example.manageapp.models.User


class SignInActivity : BaseActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        etEmail = findViewById(R.id.et_email_signIn)
        etPassword = findViewById(R.id.et_password_signIn)
        btnSignIn = findViewById(R.id.btn_sign_in)

        // Set button click listener
        btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }

        setUpActionBar()
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
    private fun setUpActionBar() {
        // Find the toolbar by its ID and set it up
        val toolbar: Toolbar = findViewById(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbar)

        // Get the action bar and configure it
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun signInRegisteredUser() {
        val email: String = etEmail.text.toString().trim()
        val password: String = etPassword.text.toString().trim()

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignIn", "signInWithEmail:success")
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Optionally call finish() to close the SignInActivity
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignIn", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showErrorSnackBar("PLEASE ENTER THE EMAIL ADDRESS")
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar("PLEASE ENTER PASSWORD")
                false
            }
            else -> {
                true
            }
        }
    }
}
