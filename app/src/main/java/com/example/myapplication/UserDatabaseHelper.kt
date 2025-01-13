package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.security.MessageDigest

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """CREATE TABLE $TABLE_USERS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_EMAIL TEXT,
            $COLUMN_PASSWORD TEXT
        )"""
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addUser(name: String, email: String, password: String): Long {
        val db = writableDatabase ?: return -1L
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NAME, name)
                put(COLUMN_EMAIL, email)
                put(COLUMN_PASSWORD, hashPassword(password))
            }
            db.insert(TABLE_USERS, null, values)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        } finally {
            db.close()
        }
    }



    fun getUserByEmail(email: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        var user: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            user = mapOf(
                "id" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString(),
                "name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "password" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
            Log.d("getUserByEmail", "User found: $user")  // Log pentru verificare
        } else {
            Log.d("getUserByEmail", "No user found with email: $email")
        }

        cursor.close()
        return user
    }



    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun authenticateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val hashedPassword = hashPassword(password)
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, hashedPassword)
        )
        val isAuthenticated = cursor.moveToFirst()
        cursor.close()
        return isAuthenticated
    }


    fun updateUsername(email: String, newUsername: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, newUsername)
        }
        val rowsAffected = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
        return rowsAffected > 0
    }

    fun updateEmail(oldEmail: String, newEmail: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, newEmail)
        }
        val rowsAffected = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(oldEmail))
        db.close()
        return rowsAffected > 0
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val db = writableDatabase
        val hashedPassword = hashPassword(newPassword)
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, hashedPassword)
        }
        val rowsAffected = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
        return rowsAffected > 0
    }

}
