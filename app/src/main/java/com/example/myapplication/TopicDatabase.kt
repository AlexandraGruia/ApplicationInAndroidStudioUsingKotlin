package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TopicDatabase (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "app_database.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TOPICS = "topics"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_PEOPLE_COUNT = "people_count"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TOPICS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_PEOPLE_COUNT INTEGER,
                $COLUMN_TIMESTAMP LONG
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TOPICS")
        onCreate(db)
    }

    fun insertTopic(topic: Topic): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, topic.id)
        values.put(COLUMN_NAME, topic.name)
        values.put(COLUMN_CATEGORY, topic.category)
        values.put(COLUMN_PEOPLE_COUNT, topic.peopleCount)
        values.put(COLUMN_TIMESTAMP, topic.timestamp)

        val result = db.insert(TABLE_TOPICS, null, values)
        db.close()
        return result
    }

    fun getAllTopics(): List<Topic> {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_TOPICS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val topics = mutableListOf<Topic>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val peopleCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PEOPLE_COUNT))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))

                val topic = Topic(id, name, category, peopleCount, timestamp)
                topics.add(topic)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return topics
    }

    fun getTopicsByCategory(category: String): List<Topic> {
        val db = readableDatabase

        val cursor: Cursor = if (category == "Most Recent") {
            db.query(
                TABLE_TOPICS,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_TIMESTAMP DESC"
            )
        } else if (category == "Most Popular") {
            db.query(
                TABLE_TOPICS,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_PEOPLE_COUNT DESC"
            )
        } else {
            db.query(
                TABLE_TOPICS,
                null,
                "$COLUMN_CATEGORY = ?",
                arrayOf(category),
                null,
                null,
                null
            )
        }

        val topics = mutableListOf<Topic>()
        if (cursor.moveToFirst()) {
            do {
                try {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                    val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                    val peopleCount =
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PEOPLE_COUNT))
                    val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))

                    val topic = Topic(id, name, category, peopleCount, timestamp)
                    topics.add(topic)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return topics
    }
}