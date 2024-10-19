package com.example.manageapp.activites

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.manageapp.R
import com.example.manageapp.firebase.FireStoreClass
import com.example.manageapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.manageapp.models.User
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2

    }

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileImageURL: String = ""

    private lateinit var navUserImage: CircleImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMobile: EditText
    private lateinit var btnUpdate: Button



    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        navUserImage = findViewById(R.id.iv_profile_user_image)
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etMobile = findViewById(R.id.et_mobile)
        btnUpdate = findViewById(R.id.btn_update)


        setupActionBar()
        FireStoreClass().loadUserData(this)


        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mSelectedImageFileUri = result.data?.data
                try {
                    Glide.with(this@MyProfileActivity)
                        .load(mSelectedImageFileUri ?: R.drawable.ic_user_place_holder)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(navUserImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        navUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data?.data != null) {
            mSelectedImageFileUri = data.data
            try {
                Glide.with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri ?: R.drawable.ic_user_place_holder)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(navUserImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "My Profile"
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user

        Glide.with(this@MyProfileActivity)
            .load(user.image )
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)

        etName.setText(user.name)
        etEmail.setText(user.email)
        if (user.mobile != 0L) {
            etMobile.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = etName.text.toString()
        }

        if (etMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = etMobile.text.toString().toLongOrNull() ?: 0L
        }

        Log.i("FirestoreData", "Updating data: $userHashMap")  // Log the hashmap

        FireStoreClass().updateUserProfileData(this, userHashMap)
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        mSelectedImageFileUri?.let { uri ->
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE_" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,uri)
            )

            sRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        mProfileImageURL = uri.toString()
                        Log.i("Profile Image URL", "Image URL: $mProfileImageURL")
                        updateUserProfileData()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        } ?: run {
            updateUserProfileData()
        }
    }


    fun profileUpdateSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
