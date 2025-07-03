package test.dapuk.dicodingstory.ui.detail

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.data.local.room.StoryDatabase
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.Story
import test.dapuk.dicodingstory.data.remote.retrofit.ApiConfig
import test.dapuk.dicodingstory.data.local.sharedpref.SharedPreferenceManager
import test.dapuk.dicodingstory.databinding.ActivityDetailBinding
import test.dapuk.dicodingstory.ui.ViewModelFactory
import test.dapuk.dicodingstory.ui.login.LoginActivity

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var sharedPreferencesManager: SharedPreferenceManager
    private lateinit var storyDatabase: StoryDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("SESSION", Context.MODE_PRIVATE)
        val apiService = ApiConfig.getApiService()
        storyDatabase = StoryDatabase.getInstance(this)
        val storyRepository = StoryRepository(apiService, sharedPreferences, storyDatabase)
        val viewModelFactory = ViewModelFactory(storyRepository)
        val detailViewModel =
            ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getStringExtra("detailId")
        if (id != null) {
            detailViewModel.setDetailId(id)
        }
        detailViewModel.detailStory.observe(this) { detailStory ->
            setDetail(detailStory)
        }
        detailViewModel.isLoading.observe(this) { isLoading ->
            loading(isLoading)
        }
        detailViewModel.isSuccess.observe(this) {
            showItem(it)
        }
        detailViewModel.isErr.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        setContentView(binding.root)
    }

    private fun setDetail(story: Story) {
        Glide.with(this)
            .load(story.photoUrl)
            .placeholder(R.drawable.failed_load_img)
            .error(R.drawable.failed_load_img)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(binding.ivDetailPhoto)
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description

    }

    fun showItem(isSuccess: Boolean) {
        if (isSuccess == false) {
            binding.tvDetailName.visibility = View.GONE
            binding.tvDetailDescription.visibility = View.GONE
        } else {
            binding.tvDetailName.visibility = View.VISIBLE
            binding.tvDetailDescription.visibility = View.VISIBLE
        }

    }

    fun loading(isLoading: Boolean) {
        if (isLoading != false) {
            binding.progressBar.visibility = View.VISIBLE

        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        sharedPreferencesManager = SharedPreferenceManager(this)
        val session = sharedPreferencesManager.getSession()
        if (session != null) {
            val userId = session.userId
            val name = session.name
            val token = session.token
            Log.d("viewmodel response", "UserId: $userId, Name: $name, Token: $token")
        } else {
            Log.d("session:", "null")
            startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
            finish()
        }
    }
}