package com.example.manageapp.activites

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manageapp.R
import com.example.manageapp.adapters.MemberListItemsAdapter
import com.example.manageapp.firebase.FireStoreClass
import com.example.manageapp.models.Board
import com.example.manageapp.models.User
import com.example.manageapp.utils.Constants

class MembersActivity : BaseActivity() {


    private lateinit var toolbarMembersActivity: Toolbar
    private lateinit var rvMembersList: RecyclerView

    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedMembersList : ArrayList<User>
    private var anyChangesMade : Boolean =false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        toolbarMembersActivity = findViewById(R.id.toolbar_members_activity)
        rvMembersList = findViewById(R.id.rv_members_list)

        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun setupMembersList(list: ArrayList<User>){
        mAssignedMembersList = list
        hideProgressDialog()

        rvMembersList.layoutManager=LinearLayoutManager(this)
        rvMembersList.setHasFixedSize(true)

        val adapter=MemberListItemsAdapter(this,list)
        rvMembersList.adapter=adapter
    }

     fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
         FireStoreClass().assignMemberToBoard(this,mBoardDetails,user)
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member ->{
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog=Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)

        val etEmailSearchMember: EditText = dialog.findViewById(R.id.et_email_search_member)
        val tvAdd: TextView = dialog.findViewById(R.id.tv_add)
        val tvCancel: TextView = dialog.findViewById(R.id.tv_cancel)

        tvAdd.setOnClickListener{
            val email=etEmailSearchMember.text.toString()

            if(email.isNotEmpty()){
                dialog.dismiss()
               showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this,email)
            }else{
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter members email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMembersList.add(user)

        anyChangesMade=true

        setupMembersList(mAssignedMembersList)
    }
}