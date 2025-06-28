package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ui.adapter.CommentAdapter
import com.example.myapplication.ui.database.AdviceDatabase
import com.example.myapplication.ui.models.Advice
import com.example.myapplication.ui.models.Comment
import com.example.myapplication.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import java.util.*

class AdviceDetailActivity : ComponentActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var addCommentButton: Button
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsList: MutableList<Comment>

    private lateinit var favoriteButton: ImageButton
    private var isFavorite = false
    private var adviceId: String? = null

    private val adviceDatabase = AdviceDatabase.instance
    private lateinit var currentAdvice: Advice

    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice_detail)

        currentUserId = getSharedPreferences("user_data", MODE_PRIVATE)
            .getString("email", "") ?: ""

        setupBottomNavigation()
        initViews()
        setupBottomSheet()
        loadAdviceAndComments()
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            selectedItemId = R.id.bottom_advice
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.bottom_advice -> true
                    R.id.bottom_home -> navigateTo(HomeActivity::class.java)
                    R.id.bottom_newnote -> navigateTo(NewNoteActivity::class.java)
                    R.id.bottom_album -> navigateTo(AlbumActivity::class.java)
                    R.id.bottom_chat -> navigateTo(ChatActivity::class.java)
                    else -> false
                }
            }
        }
    }

    private fun navigateTo(clazz: Class<*>) = run {
        startActivity(Intent(this, clazz))
        finish()
        true
    }

    private fun initViews() {
        favoriteButton = findViewById(R.id.favoriteButton)
        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        favoriteButton.setOnClickListener {
            adviceId?.let { id ->
                isFavorite = !isFavorite
                updateFavoriteIcon()
                lifecycleScope.launch {
                    adviceDatabase.saveFavoriteStatus(currentUserId, id, isFavorite)
                    Toast.makeText(
                        this@AdviceDetailActivity,
                        if (isFavorite) "Adăugat la favorite" else "Eliminat din favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupBottomSheet() {
        try {
            val bottomSheet = findViewById<LinearLayout>(R.id.commentsBottomSheet)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_COLLAPSED
                peekHeight = 48
            }

            findViewById<View>(R.id.drag_handle).setOnClickListener {
                toggleBottomSheet()
            }

        } catch (e: Exception) {
            Log.e("AdviceDetailActivity", "Eroare la setupBottomSheet: ${e.message}", e)
            Toast.makeText(this, "Eroare la afișarea comentariilor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBottomSheet() {
        bottomSheetBehavior.state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
    }

    private fun loadAdviceAndComments() {
        adviceId = intent.getStringExtra("advice_id")
        if (adviceId.isNullOrEmpty()) {
            showFallbackContent()
            return
        }

        lifecycleScope.launch {
            val allAdvices = adviceDatabase.getAllAdvices()
            val advice = allAdvices.find { it.id == adviceId }
            if (advice != null) {
                currentAdvice = advice

                isFavorite = adviceDatabase.checkIfFavorite(currentUserId, adviceId!!)
                updateFavoriteIcon()

                findViewById<TextView>(R.id.adviceDetailTitle).text = advice.title
                findViewById<TextView>(R.id.adviceDetailDescription).text = advice.description
                loadImage(advice.imageUri)

                initCommentsSection()
                reloadComments()
            } else {
                showFallbackContent()
            }
        }
    }

    private fun loadImage(imageUriStr: String?) {
        val imageView = findViewById<ImageView>(R.id.adviceDetailImage)
        if (!imageUriStr.isNullOrEmpty()) {
            try {
                if (imageUriStr.startsWith("http") || imageUriStr.startsWith("https")) {
                    Glide.with(this)
                        .load(imageUriStr)
                        .placeholder(R.drawable.default_advice_image)
                        .error(R.drawable.default_advice_image)
                        .into(imageView)
                } else {
                    val uri = Uri.parse(imageUriStr)
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.default_advice_image)
                        .error(R.drawable.default_advice_image)
                        .into(imageView)
                }
            } catch (e: Exception) {
                Log.e("AdviceDetailActivity", "Eroare la încărcarea imaginii: ${e.message}", e)
                imageView.setImageResource(R.drawable.default_advice_image)
            }
        } else {
            imageView.setImageResource(R.drawable.default_advice_image)
        }
        imageView.visibility = View.VISIBLE
    }

    private fun showFallbackContent() {
        findViewById<ImageView>(R.id.adviceDetailImage).setImageResource(R.drawable.default_advice_image)
        findViewById<TextView>(R.id.adviceDetailTitle).text = getString(R.string.no_advice_found)
        findViewById<TextView>(R.id.adviceDetailDescription).text = ""
        favoriteButton.isEnabled = false
    }

    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_red else R.drawable.ic_favorite
        )
    }

    private fun initCommentsSection() {
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentInput = findViewById(R.id.commentInput)
        addCommentButton = findViewById(R.id.addCommentButton)
        val noCommentsText = findViewById<TextView>(R.id.noCommentsText)

        commentsList = mutableListOf()
        commentAdapter = CommentAdapter(commentsList)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        noCommentsText.visibility = View.VISIBLE

        addCommentButton.setOnClickListener {
            val text = commentInput.text.toString().trim()
            if (text.isNotEmpty() && adviceId != null) {
                val newComment = Comment(
                    id = UUID.randomUUID().toString(),
                    adviceId = adviceId!!,
                    userId = null,
                    username = "Anonim",
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                lifecycleScope.launch {
                    val success = adviceDatabase.addComment(adviceId!!, newComment)
                    if (success) {
                        commentInput.setText("")
                        reloadComments()
                    } else {
                        Toast.makeText(this@AdviceDetailActivity, "Eroare la adăugarea comentariului.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Comentariul nu poate fi gol.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reloadComments() {
        lifecycleScope.launch {
            val updatedComments = adviceDatabase.getCommentsForAdvice(adviceId!!)
            commentsList.clear()
            commentsList.addAll(updatedComments)
            commentAdapter.notifyDataSetChanged()

            val noCommentsText = findViewById<TextView>(R.id.noCommentsText)
            noCommentsText.visibility = if (commentsList.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
