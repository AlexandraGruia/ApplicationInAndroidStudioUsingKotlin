package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.database.AlbumDatabaseHelper
import com.example.myapplication.ui.database.PostDatabaseHelper
import com.example.myapplication.ui.models.Album
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumAdapter(
    private val context: Context,
    private var albumList: List<Album>,
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.albumImage)
        val name: TextView = itemView.findViewById(R.id.albumName)
        val count: TextView = itemView.findViewById(R.id.albumCount)
        val deleteButton: Button = itemView.findViewById(R.id.deleteAlbumButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albumList[position]

        holder.name.text = album.name
        holder.count.text = "${album.photoPaths.size} Items"

        CoroutineScope(Dispatchers.Main).launch {
            val lastImageUrl = withContext(Dispatchers.IO) {
                getLastPhotoUrlFromAlbum(album.id)
            }

            if (!lastImageUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(lastImageUrl)
                    .placeholder(R.drawable.album_photo)
                    .error(R.drawable.album_photo)
                    .into(holder.image)
            } else {
                holder.image.setImageResource(R.drawable.album_photo)
            }
        }
        holder.itemView.setOnClickListener {
            onAlbumClick(album)
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete the album '${album.name}'?")
                .setPositiveButton("Delete") { _, _ ->
                    if (context is AlbumActivity) {
                        context.deleteAlbum(album.id, album.name)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private suspend fun getLastPhotoUrlFromAlbum(albumId: String): String? {
        val posts = PostDatabaseHelper.instance.getPostsForAlbum(albumId)
        val lastPostWithImage = posts
            .filter { !it.imagePath.isNullOrEmpty() }
            .maxByOrNull { it.date }
        return lastPostWithImage?.imagePath
    }

    override fun getItemCount(): Int = albumList.size

    fun updateData(newAlbums: List<Album>) {
        albumList = newAlbums
        notifyDataSetChanged()
    }
}