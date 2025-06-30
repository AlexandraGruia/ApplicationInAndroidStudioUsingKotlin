package com.example.myapplication.ui.database

import android.net.Uri
import android.util.Log
import com.example.myapplication.ui.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class PostDatabaseHelper private constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "PostDBFirebase"
        val instance: PostDatabaseHelper by lazy { PostDatabaseHelper() }
    }

    suspend fun getPostsForUser(userId: String): List<Post> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<Post>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting posts for user: ${e.message}")
            emptyList()
        }
    }

    suspend fun addPost(
        text: String,
        date: String,
        imageUrl: String = "",
        albumId: String? = null,
        userId: String
    ): Boolean {
        val id = UUID.randomUUID().toString()
        val post = Post(
            id = id,
            text = text,
            date = date,
            imagePath = imageUrl,
            albumId = albumId,
            userId = userId.trim().lowercase()
        )


        return try {
            postsCollection.document(id).set(post).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding post: ${e.message}")
            false
        }
    }

    suspend fun getAllPosts(): List<Post> {
        return try {
            val snapshot = postsCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject<Post>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting posts: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPostsForAlbum(albumId: String): List<Post> {
        return try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("albumId", albumId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<Post>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting posts for album: ${e.message}")
            emptyList()
        }
    }


    suspend fun getPostById(postId: String): Post? {
        return try {
            val snapshot = postsCollection.document(postId).get().await()
            snapshot.toObject<Post>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting post by ID: ${e.message}")
            null
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            val docRef = postsCollection.document(postId)
            val docSnapshot = docRef.get().await()

            val imagePath = docSnapshot.getString("imagePath") ?: ""

            if (imagePath.startsWith("https://firebasestorage")) {
                val index = imagePath.indexOf("posts/")
                if (index != -1) {
                    val firebasePath = imagePath.substring(index)
                    storage.reference.child(firebasePath).delete().await()
                }
            }

            docRef.delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post: ${e.message}")
            false
        }
    }



}


