package test.dapuk.dicodingstory.ui.maps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import test.dapuk.dicodingstory.data.adapter.ListStoriesAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.data.local.room.StoryDatabase
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
import test.dapuk.dicodingstory.data.remote.retrofit.ApiConfig
import test.dapuk.dicodingstory.data.local.sharedpref.SharedPreferenceManager
import test.dapuk.dicodingstory.databinding.ActivityMapsBinding
import test.dapuk.dicodingstory.ui.ViewModelFactory
import test.dapuk.dicodingstory.ui.login.LoginActivity

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var sharedPreferencesManager: SharedPreferenceManager
    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storyDatabase: StoryDatabase
    private val listStoriesAdapter = ListStoriesAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("SESSION", Context.MODE_PRIVATE)
        val apiService = ApiConfig.getApiService()
        storyDatabase = StoryDatabase.getInstance(this)
        val storyRepository = StoryRepository(apiService, sharedPreferences, storyDatabase)
        val viewModelFactory = ViewModelFactory(storyRepository)
        mapsViewModel = ViewModelProvider(this, viewModelFactory).get(MapsViewModel::class.java)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapsViewModel.listStories.observe(this) {
            setStoriesList(it)
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        setMapStyle()
    }

    private fun setMapStyle(){
        try{
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))
            if (!success){
                Log.e("Map styling","Failed")
            }
        } catch (e: Resources.NotFoundException){
            Log.e("Map styling","Cannot find style")
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
            startActivity(Intent(this@MapsActivity, LoginActivity::class.java))
            finish()
        }
    }

    private val boundsBuilder = LatLngBounds.Builder()
    private fun setStoriesList(storiesList: List<ListStoryItem>) {
        storiesList.forEach{
            val lat = it.lat.toString()
            val lon = it.lon.toString()
            val latLng = LatLng(lat.toDouble(), lon.toDouble())
            mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
            boundsBuilder.include(latLng)
        }
        val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
       )
    }
}
