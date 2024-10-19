package com.example.manageapp.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.manageapp.activites.CardDetailsActivity
import com.example.manageapp.activites.CreateBoardActivity
import com.example.manageapp.activites.MainActivity
import com.example.manageapp.activites.MembersActivity
import com.example.manageapp.activites.MyProfileActivity
import com.example.manageapp.activites.SignInActivity
import com.example.manageapp.activites.SignUpActivity
import com.example.manageapp.activites.TaskListActivity
import com.example.manageapp.models.Board
import com.example.manageapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.manageapp.models.User



class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

        //Register a new user to FireStore
    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentuserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccesss()
            }
            .addOnFailureListener {exception->
            Log.e(activity.javaClass.simpleName,"Error registering user",exception)
                Toast.makeText(activity,"Failed to register user:${exception.message}",Toast.LENGTH_SHORT).show()

            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId : String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName,document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentID=document.id
                activity.boardDetails(board)
            }.addOnFailureListener { e->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"error while creating board",e)
            }
    }

    fun createdBoard(activity: CreateBoardActivity,board:Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Board created successfully")
                Toast.makeText(activity,
                    "Board created successfully.", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    exception
                )
            }
    }

    fun getBoardsList(activity:MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentuserId())
            .get()
            .addOnSuccessListener {
                document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board=i.toObject(Board::class.java)!!
                    board.documentID=i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener { e->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"error while creating board",e)
            }
    }

    fun addUpdateTaskList(activity: Activity,board:Board){
        val taskListHashMap=HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST]=board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Tasklist updated Successfully")
                if(activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }.addOnFailureListener {
                exception->
                if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"error while creating the board",exception)
            }
    }

//update the user profile data in Firestore
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap:HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentuserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile data Updated Successfully")
                Toast.makeText(activity,"Profile updated successfully",Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener {exception->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error Updating Profile",exception)
                Toast.makeText(activity, "Error updating profile: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Load the user data from FireStore
    fun loadUserData(activity:Activity, readBoardsList:Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentuserId())
            .get()
            .addOnSuccessListener {document->
                val loggedInUser=document.toObject(User::class.java)
                when (activity) {
                    is SignInActivity -> {
                        loggedInUser?.let {
                            activity.signInSuccess(it)
                        }
                    }
                    is MainActivity -> {
                        loggedInUser?.let {
                            activity.updateNavigationUserDetails(it,readBoardsList)
                        }
                    }
                    is MyProfileActivity -> {
                        loggedInUser?.let {
                            activity.setUserDataInUI(it)
                        }
                    }
                }
            }
            .addOnFailureListener {exception->
            when(activity){
                    is SignInActivity->{
                            activity.hideProgressDialog()
                    }
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error loading user data", exception)
                Toast.makeText(activity, "Error loading user data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
    // get the current looged in user's Id
    fun getCurrentuserId():String{
        val currentUser=FirebaseAuth.getInstance().currentUser
        return currentUser?.uid?:""
    }

    fun getAssignedMembersListDetails(
        activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList : ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setupMembersList(usersList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(usersList)
            }.addOnFailureListener { e ->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board",
                    e
                )
            }
    }

fun getMemberDetails(activity: MembersActivity, email: String){
    mFireStore.collection(Constants.USERS)
        .whereEqualTo(Constants.EMAIL, email)
        .get()
        .addOnSuccessListener {
            document ->
            if(document.documents.size > 0){
                val user = document.documents[0].toObject(User::class.java)!!
                activity.memberDetails(user)
            }else{
                activity.hideProgressDialog()
                activity.showErrorSnackBar("no such member found")
            }
        }
        .addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(
                activity.javaClass.simpleName,
                "Error while getting user details",
                e
            )
        }
}
    fun assignMemberToBoard(activity: MembersActivity, board:Board,user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"error while creating a board",e)
            }
    }

}