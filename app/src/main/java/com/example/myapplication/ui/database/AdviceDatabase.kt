package com.example.myapplication.ui.database

import android.util.Log
import com.example.myapplication.ui.models.Advice
import com.example.myapplication.ui.models.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class AdviceDatabase private constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val adviceCollection = firestore.collection("advices")

    companion object {
        private const val TAG = "AdviceFirestoreDatabase"
        val instance: AdviceDatabase by lazy { AdviceDatabase() }
    }

    suspend fun insertOrUpdateAdvice(advice: Advice): Boolean {
        return try {
            adviceCollection.document(advice.id).set(advice).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Insert/Update Advice failed: ${e.message}")
            false
        }
    }

    suspend fun getAllAdvices(): List<Advice> {
        return try {
            val snapshot = adviceCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject<Advice>() }
        } catch (e: Exception) {
            Log.e(TAG, "Get all advices failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAdvicesByCategory(category: String): List<Advice> {
        return try {
            val snapshot = adviceCollection.whereEqualTo("category", category).get().await()
            snapshot.documents.mapNotNull { it.toObject<Advice>() }
        } catch (e: Exception) {
            Log.e(TAG, "Get advices by category failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteAdvice(id: String): Boolean {
        return try {
            adviceCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Delete advice failed: ${e.message}")
            false
        }
    }

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean): Boolean {
        return try {
            adviceCollection.document(id).update("isFavorite", isFavorite).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Update favorite status failed: ${e.message}")
            false
        }
    }

    suspend fun addComment(adviceId: String, comment: Comment): Boolean {
        return try {
            val commentsCollection = adviceCollection.document(adviceId).collection("comments")
            commentsCollection.document(comment.id).set(comment).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Add comment failed: ${e.message}")
            false
        }
    }

    suspend fun getCommentsForAdvice(adviceId: String): List<Comment> {
        return try {
            val commentsCollection = adviceCollection.document(adviceId).collection("comments")
            val snapshot = commentsCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject<Comment>() }
        } catch (e: Exception) {
            Log.e(TAG, "Get comments failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteComment(adviceId: String, commentId: String): Boolean {
        return try {
            val commentsCollection = adviceCollection.document(adviceId).collection("comments")
            commentsCollection.document(commentId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Delete comment failed: ${e.message}")
            false
        }
    }

    suspend fun getAdviceById(id: String): Advice? {
        return try {
            val document = adviceCollection.document(id).get().await()
            document.toObject<Advice>()
        } catch (e: Exception) {
            Log.e(TAG, "Get advice by id failed: ${e.message}")
            null
        }
    }

    suspend fun checkIfFavorite(userId: String, adviceId: String): Boolean {
        return try {
            val favDoc = firestore.collection("users")
                .document(userId.trim().lowercase())
                .collection("favorites")
                .document(adviceId)
                .get()
                .await()
            favDoc.exists()
        } catch (e: Exception) {
            Log.e(TAG, "checkIfFavorite error", e)
            false
        }
    }

    suspend fun saveFavoriteStatus(userId: String, adviceId: String, isFav: Boolean) {
        try {
            val favRef = firestore.collection("users")
                .document(userId.trim().lowercase())
                .collection("favorites")
                .document(adviceId)

            if (isFav) {
                favRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
            } else {
                favRef.delete().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveFavoriteStatus error", e)
        }
    }
    suspend fun getFavoriteAdvicesForUser(userId: String): List<Advice> {
        return try {
            val favDocs = firestore.collection("users")
                .document(userId.trim().lowercase())
                .collection("favorites")
                .get()
                .await()
            val favoriteIds = favDocs.documents.map { it.id }
            if (favoriteIds.isEmpty()) return emptyList()

            val snapshot = adviceCollection.whereIn("id", favoriteIds).get().await()
            snapshot.documents.mapNotNull { it.toObject<Advice>() }
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteAdvicesForUser failed: ${e.message}")
            emptyList()
        }
    }


}
