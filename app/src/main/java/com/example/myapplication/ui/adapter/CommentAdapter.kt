package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.models.Comment

class CommentAdapter(
    private var comments: List<Comment> = emptyList()
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    fun updateComments(newList: List<Comment>) {
        comments = newList
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.commentText)
        val userText: TextView = itemView.findViewById(R.id.commentUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.commentText.text = comment.text
        holder.userText.text = comment.username.ifBlank { "Anonim" }
    }

    override fun getItemCount(): Int = comments.size
}
