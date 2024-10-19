package com.example.manageapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.manageapp.R
import com.example.manageapp.models.Board
import de.hdodenhof.circleimageview.CircleImageView

// Adapter class for the RecyclerView displaying boards
open class BoardItemAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    // Inflate the view for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Corrected the inflate method
        val view = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false)
        return MyViewHolder(view)
    }

    // Get the size of the list
    override fun getItemCount(): Int {
        return list.size
    }

    // Bind data to the views
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            // Load the image using Glide
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.boardImage)

            // Set board name and creator's name
            holder.boardName.text = model.name
            holder.boardCreatedBy.text = "Created by: ${model.createdBy}"

            // Handle item click
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    // Define the click listener interface
    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    // Allow external setting of the click listener
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    // ViewHolder class to hold the item views
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val boardImage: CircleImageView = view.findViewById(R.id.iv_board_image)
        val boardName: TextView = view.findViewById(R.id.tv_name)
        val boardCreatedBy: TextView = view.findViewById(R.id.tv_created_by)
    }
}
