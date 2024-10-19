package com.example.manageapp.adapters
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manageapp.R
import com.example.manageapp.activites.TaskListActivity
import com.example.manageapp.models.Card
import com.example.manageapp.models.SelectedMembers

// TODO (Step 3: Create an adapter class for cards list.)
// START
open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if(model.labelColor.isNotEmpty()){
                holder.viewLabelColor.visibility=View.VISIBLE
                holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.viewLabelColor.visibility=View.GONE
            }

            holder.tvCardName.text = model.name

            if((context as TaskListActivity).mAssignedMemberDetailList.size>0){
                val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

                for(i in context.mAssignedMemberDetailList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMemberDetailList[i].id==j){
                            val selectedMembers=SelectedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMembersList.size>0){
                    if(selectedMembersList.size==1 && selectedMembersList[0].id==model.createdBy){
                        holder.rvcardselectedMembersList.visibility=View.GONE
                    }else{
                        holder.rvcardselectedMembersList.visibility=View.VISIBLE

                        holder.rvcardselectedMembersList.layoutManager=
                            GridLayoutManager(context,4)
                        val adapter= CardMemberListItemsAdapter(context,selectedMembersList,false)
                        holder.rvcardselectedMembersList.adapter=adapter
                        adapter.setOnClickListener(
                            object : CardMemberListItemsAdapter.OnClickListener{
                                override fun onClick() {
                                    val updatedPosition = holder.adapterPosition
                                    if (updatedPosition != RecyclerView.NO_POSITION) {
                                        onClickListener?.onClick(updatedPosition)
                                  }
                                }
                            })
                    }
                }else{
                    holder.rvcardselectedMembersList.visibility=View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                val updatedPosition = holder.adapterPosition
                if (updatedPosition != RecyclerView.NO_POSITION) {
                    onClickListener?.onClick(updatedPosition)
                }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(position: Int)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val viewLabelColor = view.findViewById<View>(R.id.view_label_color)
        val tvCardName = view.findViewById<TextView>(R.id.tv_card_name)
        val tvMembersName = view.findViewById<TextView>(R.id.tv_members_name)
        val rvcardselectedMembersList=view.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
    }
}
// END