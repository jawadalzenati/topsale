package com.aelzohry.topsaleqatar.ui.messages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.ui.messages.NotWrapper

class NotificationsAdapter(var wrappers: ArrayList<NotWrapper>, val clickListener: (NotWrapper) -> Unit): RecyclerView.Adapter<NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int = wrappers.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val wrapper = wrappers[position]
        holder.fillFrom(wrapper)
        holder.itemView.setOnClickListener {
            clickListener(wrapper)
        }
    }

    fun sort() {
        wrappers.sortByDescending { it.createdAt }
        notifyDataSetChanged()
    }

}