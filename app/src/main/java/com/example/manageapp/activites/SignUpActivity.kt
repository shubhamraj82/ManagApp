package com.example.manageapp.activites

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.manageapp.R
import com.example.manageapp.firebase.FireStoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.manageapp.models.User

class SignUpActivity : BaseActivity() {

    // Declare the EditText fields and Button
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Initialize views
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignUp = findViewById(R.id.btn_sign_up)

        setupActionBar()

        // Click event for sign-up button.
        btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to register a user to our app using Firebase.
     */
    private fun registerUser() {
        // Get the text from EditText fields and trim the spaces
        val name: String = etName.text.toString().trim { it <= ' ' }
        val email: String = etEmail.text.toString().trim { it <= ' ' }
        val password: String = etPassword.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        val user = User(firebaseUser.uid, name, registeredEmail)

                        FireStoreClass().registerUser(this@SignUpActivity, user)
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to be called when the user is registered successfully and entry is made in the Firestore database.
     */


    fun userRegisteredSuccesss(){
        Toast.makeText(
            this@SignUpActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()

        finish()
    }
}
