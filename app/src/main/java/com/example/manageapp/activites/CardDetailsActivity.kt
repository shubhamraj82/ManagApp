package com.example.manageapp.activites

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manageapp.R
import com.example.manageapp.adapters.CardMemberListItemsAdapter
import com.example.manageapp.dialogs.LabelColorListDialog
import com.example.manageapp.dialogs.MembersListDialog
import com.example.manageapp.firebase.FireStoreClass
import com.example.manageapp.models.Board
import com.example.manageapp.models.Card
import com.example.manageapp.models.SelectedMembers
import com.example.manageapp.models.Task
import com.example.manageapp.models.User
import com.example.manageapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds : Long=0

    private lateinit var etNameCardDetails: EditText
    private lateinit var btnUpdateCardDetails: Button
    private lateinit var tvSelectLabelColor: TextView
    private lateinit var tvSelectMembers: TextView
    private lateinit var tvSelectDueDate: TextView
    private lateinit var rvSelectedMembersList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        etNameCardDetails = findViewById(R.id.et_name_card_details)
        btnUpdateCardDetails= findViewById(R.id.btn_update_card_details)
        tvSelectLabelColor = findViewById(R.id.tv_select_label_color)
        tvSelectMembers = findViewById(R.id.tv_select_members)
        tvSelectDueDate = findViewById(R.id.tv_select_due_date)
        rvSelectedMembersList=findViewById(R.id.rv_selected_members_list)

        getIntentData()
        setupActionBar()

        etNameCardDetails.setText(mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].name)
        etNameCardDetails.setSelection(etNameCardDetails.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        btnUpdateCardDetails.setOnClickListener {
            if(etNameCardDetails.text.toString().isNotEmpty())
                updateCardDetails()
            else{
                Toast.makeText(this@CardDetailsActivity,
                    "Enter card name", Toast.LENGTH_SHORT).show()
            }
        }

        tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if(mSelectedDueDateMilliSeconds > 0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tvSelectDueDate.text = selectedDate
        }
        tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }

    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_card_details_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun colorsList() : ArrayList<String>{
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColor(){
        tvSelectLabelColor.text=""
        tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard(mBoardDetails
                    .taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition=intent.getIntExtra(
                Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition=intent.getIntExtra(
            Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList=intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun membersListDialog(){
        var cardAssignedMembersList = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size>0){
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected=true
                    }
                }
            }
        }else{
            for(i in mMembersDetailList.indices){
                mMembersDetailList[i].selected=false
            }
        }

        val listDialog = object: MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
               if(action== Constants.SELECT){
                   if(!mBoardDetails
                           .taskList[mTaskListPosition].cards[mCardPosition]
                           .assignedTo.contains(user.id)){
                       mBoardDetails
                           .taskList[mTaskListPosition].cards[mCardPosition]
                           .assignedTo.add(user.id)
                   }
               }
               else{
                   mBoardDetails
                       .taskList[mTaskListPosition].cards[mCardPosition]
                       .assignedTo.remove(user.id)

                   for(i in mMembersDetailList.indices){
                       if(mMembersDetailList[i].id == user.id){
                           mMembersDetailList[i].selected=false
                       }
                   }
               }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun updateCardDetails(){
        val card= Card(
            etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]=card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails
            .taskList[mTaskListPosition].cards

        cardsList.removeAt(mCardPosition)

        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            // TODO (Step 8: Call the function to delete the card.)
            // START
            deleteCard()
            // END
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun labelColorsListDialog(){
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object: LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMemberList =mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for(i in mMembersDetailList.indices){
            for(j in cardAssignedMemberList){
                if(mMembersDetailList[i].id == j){
                    val selectedMembers=SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMembers)
                }
            }
        }
        if(selectedMembersList.size>0){
            selectedMembersList.add(SelectedMembers("",""))
            tvSelectMembers.visibility=View.GONE
            rvSelectedMembersList.visibility=View.VISIBLE

            rvSelectedMembersList.layoutManager = GridLayoutManager(
                this, 6
            )
            val  adapter= CardMemberListItemsAdapter(
                this,selectedMembersList,true)
            rvSelectedMembersList.adapter= adapter
            adapter.setOnClickListener(
                object: CardMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        }else{
            tvSelectMembers.visibility=View.VISIBLE
            rvSelectedMembersList.visibility=View.GONE
        }
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                tvSelectDueDate.text = selectedDate
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }

}