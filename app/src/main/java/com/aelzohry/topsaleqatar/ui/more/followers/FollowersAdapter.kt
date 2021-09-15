package com.aelzohry.topsaleqatar.ui.more.followers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.model.User

class FollowersAdapter(var users: ArrayList<User>, val tapHandler: (user: User)->Unit): RecyclerView.Adapter<FollowerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_follower, parent, false)
        return FollowerViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val user = users[position]
        holder.fillFrom(user)
        holder.itemView.setOnClickListener { tapHandler(user) }
    }
}