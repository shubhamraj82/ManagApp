package com.example.manageapp.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manageapp.R
import com.example.manageapp.adapters.LabelColorListItemAdapter


// TODO (Step 5: Create an dialogs package and a class for showing the label color list dialog.)
// START
abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private var mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val rvList: RecyclerView = view.findViewById(R.id.rvList)
        tvTitle.text = title
        rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemAdapter(context, list, mSelectedColor)
        rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemAdapter.OnItemClickListener {

            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}
