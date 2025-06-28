package com.example.myapplication.ui.utils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdviceActivity
import com.example.myapplication.ChatActivity
import com.example.myapplication.R
import com.example.myapplication.ui.adapter.UsersAdapter
import com.example.myapplication.ui.database.UserDatabaseHelper
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private val dbHelper = UserDatabaseHelper.instance

    private lateinit var totalUsersText: TextView
    private lateinit var adminUsersText: TextView
    private lateinit var chatButton: Button
    private lateinit var adviceButton: Button
    private lateinit var refreshButton: Button

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        initializeViews()
        setupListeners()
        setupRecyclerView()
        loadStatistics()
        loadUsersList()
    }

    private fun initializeViews() {
        totalUsersText = findViewById(R.id.totalUsersText)
        adminUsersText = findViewById(R.id.adminUsersText)
        chatButton = findViewById(R.id.chatButton)
        adviceButton = findViewById(R.id.adviceButton)
        refreshButton = findViewById(R.id.refreshButton)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
    }

    private fun setupListeners() {
        chatButton.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        adviceButton.setOnClickListener {
            startActivity(Intent(this, AdviceActivity::class.java))
        }

        refreshButton.setOnClickListener {
            loadStatistics()
            loadUsersList()
            Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersAdapter = UsersAdapter(emptyList())
        usersRecyclerView.adapter = usersAdapter
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                val stats = dbHelper.getUserStatistics()
                val totalUsers = stats["totalUsers"] ?: 0
                val adminUsers = stats["adminUsers"] ?: 0

                totalUsersText.text = "Total users: $totalUsers"
                adminUsersText.text = "Administrators: $adminUsers"
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatisticsActivity, "Error loading statistics.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadUsersList() {
        lifecycleScope.launch {
            try {
                val usersList = dbHelper.getAllUsers()
                usersAdapter.updateUsers(usersList)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatisticsActivity, "Error loading users list.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
