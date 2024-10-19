package com.example.manageapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.manageapp.R

class LabelColorListItemAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Declare custom OnItemClickListener
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_label_color, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if (holder is MyViewHolder) {
            // Set the background color of the view
            holder.viewMain.setBackgroundColor(Color.parseColor(item))

            // Show the selected color icon if the color matches the selected color
            if (item == mSelectedColor) {
                holder.ivSelectedColor.visibility = View.VISIBLE
            } else {
                holder.ivSelectedColor.visibility = View.GONE
            }

            // Set click listener for the item
            holder.itemView.setOnClickListener {
                onItemClickListener?.onClick(position, item)
            }
        }
    }

    // ViewHolder class to bind the views
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewMain: View = view.findViewById(R.id.view_main)
        val ivSelectedColor: ImageView = view.findViewById(R.id.iv_selected_color)
    }

    // Custom interface for handling item clicks
    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}
