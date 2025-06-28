package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.database.AdviceDatabase
import com.example.myapplication.ui.models.Advice
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdviceActivity : ComponentActivity() {

    private lateinit var adviceList: List<Advice>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdviceAdapter
    private var isAdmin = false

    private val db = AdviceDatabase.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice)

        val userSharedPrefs = getSharedPreferences("user_data", MODE_PRIVATE)
        isAdmin = userSharedPrefs.getBoolean("isAdmin", false)

        setupBottomNavigation()
        setupViews()
        loadAndDisplayAdvices()
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayAdvices()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_advice
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_advice -> true
                R.id.bottom_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_newnote -> {
                    startActivity(Intent(this, NewNoteActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_album -> {
                    startActivity(Intent(this, AlbumActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerAdviceView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdviceAdapter(
            isAdmin,
            onItemClicked = { advice -> openAdviceDetails(advice) },
            onEditClicked = { advice -> openEditAdvice(advice) }
        )
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            Log.d("AdviceActivity", "Settings icon clicked")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.menuIcon).setOnClickListener {
            Log.d("AdviceActivity", "Category menu icon clicked")
            showCategoryMenu()
        }

        val buttonAddAdvice = findViewById<ImageButton>(R.id.buttonAddAdvice)
        buttonAddAdvice.visibility = if (isAdmin) ImageButton.VISIBLE else ImageButton.GONE
        buttonAddAdvice.setOnClickListener {
            Log.d("AdviceActivity", "AddAdvice button clicked")
            startActivity(Intent(this, AddAdviceActivity::class.java))
        }
    }

    private fun loadAndDisplayAdvices(categoryFilter: String? = null, favoritesOnly: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = getSharedPreferences("user_data", MODE_PRIVATE).getString("email", "") ?: ""

            val advices = try {
                when {
                    favoritesOnly -> db.getFavoriteAdvicesForUser(userId)
                    !categoryFilter.isNullOrEmpty() && categoryFilter != "All" -> db.getAdvicesByCategory(categoryFilter)
                    else -> db.getAllAdvices()
                }
            } catch (e: Exception) {
                Log.e("AdviceActivity", "Error loading advices: ${e.message}")
                emptyList()
            }

            withContext(Dispatchers.Main) {
                if (advices.isEmpty()) {
                    Toast.makeText(this@AdviceActivity, "Nu există sfaturi disponibile.", Toast.LENGTH_SHORT).show()
                }
                adviceList = advices
                adapter.submitList(adviceList)
            }
        }
    }


    private fun showCategoryMenu() {
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        val popupMenu = PopupMenu(this, menuIcon)

        Log.d("AdviceActivity", "Showing category popup menu")
        popupMenu.menu.add("All")
        popupMenu.menu.add("Favorites")

        val categories = resources.getStringArray(R.array.categoryAdvice_list)
        categories.forEach { category ->
            popupMenu.menu.add(category)
            Log.d("AdviceActivity", "Added category to menu: $category")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            Log.d("AdviceActivity", "Category menu item clicked: ${item.title}")
            when (item.title.toString()) {
                "All" -> loadAndDisplayAdvices()
                "Favorites" -> loadAndDisplayAdvices(favoritesOnly = true)
                else -> loadAndDisplayAdvices(categoryFilter = item.title.toString())
            }
            true
        }
        popupMenu.show()
    }

    private fun openAdviceDetails(advice: Advice) {
        try {
            if (advice.id.isBlank()) {
                Toast.makeText(this, "Sfatul nu are ID valid!", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(this, AdviceDetailActivity::class.java).apply {
                putExtra("advice_id", advice.id)
                putExtra("title", advice.title)
                putExtra("fullContent", advice.description)
                putExtra("imageUri", advice.imageUri ?: "")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AdviceActivity", "Error opening AdviceDetailActivity", e)
            Toast.makeText(this, "Eroare la deschiderea detaliilor.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEditAdvice(advice: Advice) {
        Log.d("AdviceActivity", "Opening edit for advice id: ${advice.id}")
        val intent = Intent(this, EditAdviceActivity::class.java).apply {
            putExtra("adviceId", advice.id)
        }
        startActivity(intent)
    }

}
