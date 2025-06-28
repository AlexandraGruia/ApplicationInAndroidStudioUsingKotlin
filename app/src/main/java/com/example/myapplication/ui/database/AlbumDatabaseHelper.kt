
package com.example.myapplication.ui.database

import android.util.Log
import com.example.myapplication.ui.models.Album
import com.example.myapplication.ui.models.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await


class AlbumDatabaseHelper private constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "AlbumFirebase"
        val instance: AlbumDatabaseHelper by lazy { AlbumDatabaseHelper() }
    }

    suspend fun addAlbum(name: String, userId: String): Album? {
        return try {
            val albumRef = firestore.collection("albums").document()
            val album = Album(
                id = albumRef.id,
                name = name,
                photoPaths = emptyList(),
                userId = userId
            )
            albumRef.set(album).await()
            album
        } catch (e: Exception) {
            Log.e(TAG, "Error adding album: ${e.message}")
            null
        }
    }

    suspend fun getAlbums(userId: String): List<Album> {
        return try {
            firestore.collection("albums")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Album::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            Log.e("AlbumDB", "Error getting albums: ${e.message}")
            emptyList()
        }
    }



    suspend fun addPostToAlbum(post: Post, albumId: String): Boolean {
        return try {
            firestore.collection("posts")
                .document(post.id)
                .update("albumId", albumId)
                .await()

            val imagePath = post.imagePath
            if (imagePath.isNotEmpty()) {
                val albumRef = firestore.collection("albums").document(albumId)
                albumRef.update("photoPaths", FieldValue.arrayUnion(imagePath)).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding post to album: ${e.message}")
            false
        }
    }


    suspend fun getPostsForAlbum(albumId: String): List<Post> {
        return try {
            FirebaseFirestore.getInstance()
                .collection("posts")
                .whereEqualTo("albumId", albumId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Post::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            Log.e("PostDB", "Error fetching posts for album: ${e.message}")
            emptyList()
        }
    }


    suspend fun removePostFromAlbum(post: Post): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.id)
                .update("albumId", null)
                .await()
            true
        } catch (e: Exception) {
            Log.e("AlbumDB", "Error removing albumId from post: ${e.message}")
            false
        }
    }


    suspend fun isFavorite(userId: String, postId: String): Boolean = try {
        val doc = firestore.collection("favorites")
            .document(userId)
            .collection("posts")
            .document(postId)
            .get()
            .await()
        doc.exists()
    } catch (e: Exception) {
        Log.e(TAG, "Error checking favorite: ${e.message}")
        false
    }

    suspend fun setFavoriteForUser(userId: String, postId: String, isFav: Boolean) {
        try {
            val docRef = firestore.collection("favorites")
                .document(userId)
                .collection("posts")
                .document(postId)
            if (isFav) {
                docRef.set(mapOf("favoritedAt" to Timestamp.now())).await()
            } else {
                docRef.delete().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite: ${e.message}")
        }
    }

    suspend fun getFavoritesForUser(userId: String): List<String> = try {
        val snapshot = firestore.collection("favorites")
            .document(userId)
            .collection("posts")
            .get()
            .await()
        snapshot.documents.map { it.id }
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching favorites: ${e.message}")
        emptyList()
    }

    suspend fun deleteAlbum(albumId: String): Boolean {
        return try {
            firestore.collection("albums").document(albumId).delete().await()

            val postsSnapshot = firestore.collection("posts")
                .whereEqualTo("albumId", albumId)
                .get()
                .await()

            postsSnapshot.documents.forEach { doc ->
                doc.reference.update("albumId", null).await()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting album: ${e.message}")
            false
        }
    }


}


