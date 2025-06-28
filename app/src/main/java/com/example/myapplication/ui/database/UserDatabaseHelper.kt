package com.example.myapplication.ui.database

import android.content.ContentValues.TAG
import android.util.Log
import com.example.myapplication.ui.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class UserDatabaseHelper private constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    companion object {
        private const val TAG = "UserDatabaseHelper"
        val instance: UserDatabaseHelper by lazy { UserDatabaseHelper() }
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Hashing error: ${e.message}")
            ""
        }
    }

    suspend fun addUser(
        name: String,
        email: String,
        password: String,
        dateOfBirth: String,
        isAdmin: Boolean = false
    ): Boolean {
        val normalizedEmail = email.trim().lowercase()

        if (getUserByEmail(normalizedEmail) != null) {
            Log.w(TAG, "Email already exists: $normalizedEmail")
            return false
        }

        val user = User(
            name = name,
            email = normalizedEmail,
            password = hashPassword(password),
            dateOfBirth = dateOfBirth,
            isAdmin = isAdmin
        )

        return try {
            usersCollection.document(user.email).set(user).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Add user failed: ${e.message}")
            false
        }
    }


    suspend fun getUserByEmail(email: String): User? {
        return try {
            val doc = usersCollection.document(email.trim().lowercase()).get().await()
            if (doc.exists()) doc.toObject<User>() else null
        } catch (e: Exception) {
            Log.e(TAG, "Get user failed: ${e.message}")
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject<User>() }
        } catch (e: Exception) {
            Log.e(TAG, "Get all users failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun authenticateUser(email: String, password: String): Boolean {
        val user = getUserByEmail(email)
        return user?.password == hashPassword(password)
    }

    suspend fun getUserStatistics(): Map<String, Int> {
        return try {
            val users = getAllUsers()
            val total = users.size
            val admins = users.count { it.isAdmin }
            mapOf("totalUsers" to total, "adminUsers" to admins)
        } catch (e: Exception) {
            Log.e(TAG, "Stats error: ${e.message}")
            mapOf("totalUsers" to 0, "adminUsers" to 0)
        }
    }

    suspend fun updateUsername(email: String, newName: String): Boolean {
        return updateField(email, "name", newName)
    }

    suspend fun updatePassword(email: String, newPassword: String): Boolean {
        return updateField(email, "password", hashPassword(newPassword))
    }

    suspend fun updateEmail(oldEmail: String, newEmail: String): Boolean {
        val user = getUserByEmail(oldEmail) ?: return false
        val newUser = user.copy(email = newEmail.trim().lowercase())
        return try {
            usersCollection.document(newUser.email).set(newUser).await()
            usersCollection.document(oldEmail.trim().lowercase()).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Email update failed: ${e.message}")
            false
        }
    }

    suspend fun addGoogleUser(name: String, email: String, isAdmin: Boolean = false): Boolean {
        val user = User(
            name = name,
            email = email.trim().lowercase(),
            password = "",
            isAdmin = isAdmin
        )
        return try {
            usersCollection.document(user.email).set(user).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Add Google user failed: ${e.message}")
            false
        }
    }

    suspend fun updateField(email: String, field: String, value: Any): Boolean {
        return try {
            usersCollection.document(email.trim().lowercase()).update(field, value).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Update field failed: ${e.message}")
            false
        }
    }

    suspend fun addAdminIfNotExists() {
        val email = "admin@gmail.com"
        if (getUserByEmail(email) == null) {
            val added = addUser(
                name = "Administrator",
                email = email,
                password = "admin1234",
                dateOfBirth = "01/01/1970",
                isAdmin = true
            )
            if (added) {
                Log.i(TAG, "Admin added")
            } else {
                Log.e(TAG, "Failed to add admin")
            }
        }
    }

    suspend fun getSudokuCompletedCount(email: String): Int {
        return try {
            val user = getUserByEmail(email)
            user?.sudokuCompleted ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Get Sudoku count failed: ${e.message}")
            0
        }
    }

    suspend fun incrementSudokuCompleted(email: String): Boolean {
        return try {
            val user = getUserByEmail(email)
            val newCount = (user?.sudokuCompleted ?: 0) + 1
            updateField(email, "sudokuCompleted", newCount)
        } catch (e: Exception) {
            Log.e(TAG, "Increment Sudoku failed: ${e.message}")
            false
        }
    }
}



