package com.example.manageapp.activites

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.manageapp.R
import com.example.manageapp.firebase.FireStoreClass
import com.example.manageapp.models.Board

import com.example.manageapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var ivProfileUserImage: ImageView
    private var mUserName: String = ""
    private var mBoardImageURL: String = ""  // Corrected typo: mBoardImageURL
    private lateinit var etBoardName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        // Retrieve username from the Intent and handle nullable case
        mUserName = intent.getStringExtra(Constants.NAME) ?: ""

        ivProfileUserImage = findViewById(R.id.iv_board_image)
        etBoardName = findViewById(R.id.et_board_name)

        // Set up click listener for image picking
        ivProfileUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        // Button click event for creating the board
        findViewById<Button>(R.id.btn_create).setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()  // Upload the board image before creating board
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()  // Directly create board if no image is selected
            }
        }
    }

    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        getCurrentUserID()?.let { assignedUsersArrayList.add(it) }

        // Create board with non-nullable values
        val board = Board(
            etBoardName.text.toString(), // Board name
            mBoardImageURL,              // Board image URL
            mUserName,                   // Username
            assignedUsersArrayList       // Assigned users as ArrayList<String>
        )

        FireStoreClass().createdBoard(this, board)  // Call Firestore method to create the board
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE_" + System.currentTimeMillis() + "." +
                    Constants.getFileExtension(this, mSelectedImageFileUri)
        )

        mSelectedImageFileUri?.let {
            sRef.putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        mBoardImageURL = uri.toString()
                        Log.i("FIREBASE BOARD Image URL", "Image URL: $mBoardImageURL")
                        createBoard()  // Once the image is uploaded, create the board
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            title = resources.getString(R.string.create_board_title)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
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
}
