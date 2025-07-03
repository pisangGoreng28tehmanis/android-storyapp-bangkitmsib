package test.dapuk.dicodingstory.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.data.adapter.ListStoriesAdapter
import test.dapuk.dicodingstory.data.adapter.LoadingStateAdapter
import test.dapuk.dicodingstory.data.local.room.StoryDatabase
import test.dapuk.dicodingstory.data.local.sharedpref.SharedPreferenceManager
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.retrofit.ApiConfig
import test.dapuk.dicodingstory.databinding.ActivityMainBinding
import test.dapuk.dicodingstory.ui.ViewModelFactory
import test.dapuk.dicodingstory.ui.addstory.AddStoryActivity
import test.dapuk.dicodingstory.ui.login.LoginActivity
import test.dapuk.dicodingstory.ui.maps.MapsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferencesManager: SharedPreferenceManager
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storyDatabase: StoryDatabase
    private val listStoriesAdapter = ListStoriesAdapter()
    private val addStoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            listStoriesAdapter.refresh()
            binding.rvStories.smoothScrollToPosition(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        sharedPreferences = getSharedPreferences("SESSION", Context.MODE_PRIVATE)
        val apiService = ApiConfig.getApiService()
        storyDatabase = StoryDatabase.getInstance(this)
        val storyRepository = StoryRepository(apiService, sharedPreferences, storyDatabase)
        val viewModelFactory = ViewModelFactory(storyRepository)
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferencesManager = SharedPreferenceManager(this)
        binding.btnLogout.setOnClickListener {
            sharedPreferencesManager.clearSession()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        mainViewModel.isErr.observe(this) {

            if (sharedPreferencesManager.getSession() != null) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        mainViewModel.isLoading.observe(this) {
            loading(it)
        }
        binding.btnMaps.setOnClickListener {
            startActivity(Intent(this,MapsActivity::class.java))
        }
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = listStoriesAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                listStoriesAdapter.retry()
            })
        binding.floatingActionButton.setOnClickListener {
            addStoryLauncher.launch(Intent(this@MainActivity, AddStoryActivity::class.java))
        }
        mainViewModel.story.observe(this, {
            listStoriesAdapter.submitData(lifecycle, it)
        })

        binding.btnRetry.setOnClickListener {  listStoriesAdapter.retry() }

        listStoriesAdapter.addLoadStateListener { loadState ->
            binding.tvErr.visibility= View.GONE
            binding.btnRetry.visibility = View.GONE
            if (loadState.refresh is LoadState.Loading) {
                loading(true)
            } else {
                loading(false)
            }

            if (loadState.refresh is LoadState.Error) {
                binding.tvErr.text = "Cek koneksi dan coba lagi"
                binding.tvErr.visibility= View.VISIBLE
                binding.btnRetry.visibility = View.VISIBLE
            }
        }
        setContentView(binding.root)

    }

    fun loading(isLoading: Boolean) {
        if (isLoading != false) {
            binding.progressBar2.visibility = View.VISIBLE
        } else {
            binding.progressBar2.visibility = View.GONE
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
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

}
