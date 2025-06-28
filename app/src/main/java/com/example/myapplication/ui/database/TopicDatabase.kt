package com.example.myapplication.ui.database

import android.util.Log
import com.example.myapplication.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class TopicDatabase private constructor() {

    val firestore = FirebaseFirestore.getInstance()
    private val topicsCollection = firestore.collection("topics")

    companion object {
        private const val TAG = "TopicDatabase"
        val instance: TopicDatabase by lazy { TopicDatabase() }
    }

    suspend fun insertTopic(topic: Topic): Boolean {
        return try {
            topicsCollection.document(topic.id).set(topic).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Insert topic failed: ${e.message}")
            false
        }
    }

    suspend fun getAllTopics(): List<Topic> {
        return try {
            val snapshot = topicsCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject<Topic>() }
        } catch (e: Exception) {
            Log.e(TAG, "Get all topics failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteTopicById(topicId: String): Boolean {
        return try {
            topicsCollection.document(topicId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Delete topic failed: ${e.message}")
            false
        }
    }

    suspend fun getTopicsByCategory(category: String): List<Topic> {
        return try {
            val query = when (category) {
                "Most Recent" -> topicsCollection.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                "Most Popular" -> topicsCollection.orderBy("messageCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                "Favorites" -> topicsCollection.whereEqualTo("isFavorite", true)
                else -> topicsCollection.whereEqualTo("category", category)
            }
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { it.toObject<Topic>() }
        } catch (e: Exception) {
            Log.e(TAG, "Get by category failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun incrementMessageCount(topicId: String) {
        try {
            val docRef = topicsCollection.document(topicId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val current = snapshot.getLong("messageCount") ?: 0
                transaction.update(docRef, "messageCount", current + 1)
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Increment failed: ${e.message}")
        }
    }

    suspend fun decrementMessageCount(topicId: String) {
        try {
            val docRef = topicsCollection.document(topicId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val current = snapshot.getLong("messageCount") ?: 0
                transaction.update(docRef, "messageCount", maxOf(current - 1, 0))
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Decrement failed: ${e.message}")
        }
    }
    suspend fun setFavoriteForUser(email: String, topicId: String, favorite: Boolean) {
        try {
            val userFavorites = firestore.collection("users")
                .document(email.trim().lowercase())
                .collection("favorites")
                .document(topicId)
            if (favorite) {
                userFavorites.set(mapOf("favoritedAt" to System.currentTimeMillis())).await()
            } else {
                userFavorites.delete().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "setFavoriteForUser failed: ${e.message}")
        }
    }

    suspend fun getFavoriteTopicIdsForUser(email: String): List<String> {
        return try {
            val snapshot = firestore.collection("users")
                .document(email.trim().lowercase())
                .collection("favorites")
                .get()
                .await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteTopicIdsForUser failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTopicsByIds(ids: List<String>): List<Topic> {
        if (ids.isEmpty()) return emptyList()

        val batches = ids.chunked(10)
        val results = mutableListOf<Topic>()
        try {
            for (batch in batches) {
                val snapshot = topicsCollection
                    .whereIn("id", batch)
                    .get()
                    .await()
                results.addAll(snapshot.documents.mapNotNull { it.toObject<Topic>() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopicsByIds failed: ${e.message}")
        }
        return results
    }




}
