package com.example.manageapp.activites

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manageapp.R
import com.example.manageapp.adapters.BoardItemAdapter
import com.example.manageapp.firebase.FireStoreClass
import com.example.manageapp.models.Board
import com.example.manageapp.models.User
import com.example.manageapp.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navUserImage: CircleImageView
    private lateinit var tvUsername: TextView
    private lateinit var fabCreateBtn: FloatingActionButton  // Changed from Button to FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        fabCreateBtn = findViewById(R.id.fab_create_board)  // Ensure fab_create_board exists in activity_main.xml

        setupActionBar()

        // Initialize the NavigationView and set up the listener
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Access the header view of the NavigationView
        val headerView = navView.getHeaderView(0)
        navUserImage = headerView.findViewById(R.id.iv_user_image)
        tvUsername = headerView.findViewById(R.id.tv_username)

        // Initialize Firestore and sign in the user
        FireStoreClass().loadUserData(this,true)

        // Set up click listener for FloatingActionButton
        fabCreateBtn.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){
        val rvBoardsList: RecyclerView = findViewById(R.id.rv_board_list)
        val tvNoBoardsAvailable: TextView = findViewById(R.id.tv_no_boards_available)

        hideProgressDialog()
        if(boardsList.size>0) {
            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvailable.visibility = View.GONE

            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemAdapter(this, boardsList)
            rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object: BoardItemAdapter.OnClickListener{
                override fun onClick(position:Int,model:Board){
                    val intent=Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentID)
                    startActivity(intent)
                }
            })
        }else{
            rvBoardsList.visibility=View.GONE
            tvNoBoardsAvailable.visibility = View.VISIBLE
        }


    }

    // Setup the action bar with the navigation drawer toggle
    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    // Toggle the navigation drawer
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Update the navigation header details with user information
    fun updateNavigationUserDetails(user: User, readBoardsList:Boolean) {
        mUserName = user.name
        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navUserImage)
        tvUsername.text = user.name
        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FireStoreClass().loadUserData(this)
        }else if(resultCode==Activity.RESULT_OK
            && requestCode== CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardsList(this)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    // Handle navigation item selections
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
