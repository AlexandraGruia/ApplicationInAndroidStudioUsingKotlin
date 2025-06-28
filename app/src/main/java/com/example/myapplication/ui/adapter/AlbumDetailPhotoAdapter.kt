package com.example.myapplication

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.models.Post

class AlbumDetailPhotoAdapter(
    private val context: Context,
    private var postList: List<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<AlbumDetailPhotoAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_photo, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val imagePath = post.imagePath

        if (!imagePath.isNullOrEmpty()) {
            if (imagePath.startsWith("data:image")) {
                try {
                    val base64String = imagePath.substringAfter("base64,")
                    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    holder.imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    holder.imageView.setImageResource(R.drawable.default_image)
                }
            } else if (imagePath.startsWith("http")) {
                Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .centerCrop()
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.default_image)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.default_image)
        }

        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updateData(newPosts: List<Post>) {
        postList = newPosts
        notifyDataSetChanged()
    }
}
