package test.dapuk.dicodingstory.ui.addstory

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.data.local.room.StoryDatabase
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.RegisterResponse
import test.dapuk.dicodingstory.data.remote.retrofit.ApiConfig
import test.dapuk.dicodingstory.data.local.sharedpref.SharedPreferenceManager
import test.dapuk.dicodingstory.databinding.ActivityAddStoryBinding
import test.dapuk.dicodingstory.ui.camera.CameraActivity
import test.dapuk.dicodingstory.ui.camera.CameraActivity.Companion.CAMERAX_RESULT
import test.dapuk.dicodingstory.ui.ViewModelFactory
import test.dapuk.dicodingstory.ui.login.LoginActivity
import test.dapuk.dicodingstory.ui.main.MainActivity
import test.dapuk.dicodingstory.utils.reduceFileImage
import test.dapuk.dicodingstory.utils.uriToFile
import java.util.concurrent.TimeUnit

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var storyDatabase: StoryDatabase
    private var currentImageUri: Uri? = null
    private lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat: Double? = null
    private var lon: Double? = null
    private lateinit var sharedPreferencesManager: SharedPreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("SESSION", Context.MODE_PRIVATE)
        val apiService = ApiConfig.getApiService()
        storyDatabase = StoryDatabase.getInstance(this)
        val storyRepository = StoryRepository(apiService, sharedPreferences, storyDatabase)
        val viewModelFactory = ViewModelFactory(storyRepository)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        addStoryViewModel = ViewModelProvider(this).get(AddStoryViewModel::class.java)
        setContentView(R.layout.activity_add_story)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        addStoryViewModel.currentImageUri.observe(this, { uri ->
            if (uri != null) {
                showImage(uri)
            }
        })

        binding.ivStoryimage.setImageResource(R.drawable.failed_load_img)
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnCamera.setOnClickListener { startCamera() }
        binding.buttonAdd.setOnClickListener {
                uploadImage()
        }
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLastLocation()
            }
        }
        setContentView(binding.root)
    }


    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                addStoryViewModel.setCurrentImageUri(uri)
                showImage(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private fun startGallery() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage(uri: Uri) {
        Log.d("Image URI", "showImage: $uri")
        binding.ivStoryimage.setImageURI(uri)
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            addStoryViewModel.setCurrentImageUri(currentImageUri)
            currentImageUri?.let { it1 -> showImage(it1) }
        }
    }

    private fun uploadImage() {
        if (addStoryViewModel.currentImageUri.value == null) {
            Toast.makeText(this, "Gambar belum dipilih", Toast.LENGTH_SHORT).show()
            return
        }
        addStoryViewModel.currentImageUri.observe(this) { uri ->
            val imageFile = uriToFile(uri!!, this).reduceFileImage()
            val description = binding.edAddDescription.text.toString()
            Log.d("image file", "showImage: ${imageFile.path}")
            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            if (description.isEmpty()) {
                Toast.makeText(this, "Deskripsi belum diisi", Toast.LENGTH_SHORT).show()
            } else lifecycleScope.launch {
                try {
                    binding.progressBar3.visibility = View.VISIBLE
                    val apiService = ApiConfig.getApiService()
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("SESSION", Context.MODE_PRIVATE)
                    val storyRepository =
                        StoryRepository(apiService, sharedPreferences, storyDatabase)
                    val response = if (lat != null && lon != null) {
                        storyRepository.addStoryLocation(multipartBody, requestBody, lat!!, lon!!)
                    } else {
                        storyRepository.addStory(multipartBody, requestBody)
                    }
                    if (response?.error == false) {
                        Log.d("Upload Success", response.message)
                        Toast.makeText(this@AddStoryActivity, "Upload Success", Toast.LENGTH_SHORT)
                            .show()
                        lifecycleScope.launch {
                            delay(500L)
                            setResult(Activity.RESULT_OK)
                            val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    } else {
                        Log.e("Upload Failed", response?.message ?: "Unknown error")
                        Toast.makeText(this@AddStoryActivity, "Upload Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                    Toast.makeText(this@AddStoryActivity, "Upload Error", Toast.LENGTH_SHORT).show()
                    Log.e("Upload Error", errorResponse.message)
                } catch (e: Exception) {
                    Toast.makeText(this@AddStoryActivity, "No Internet", Toast.LENGTH_SHORT).show()
                    Log.e("Upload Error", "${e.message}")
                } finally {
                    binding.progressBar3.visibility = View.GONE
                }
            }
        }
    }


    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            else -> {
                Toast.makeText(this, "Anda belum mengizinkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getMyLastLocation() {

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

            if (isGPSEnabled()) {

                val handler = Handler(Looper.getMainLooper())
                val timeoutRunnable = Runnable {
                    Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                }
                handler.postDelayed(timeoutRunnable, 3000)

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    handler.removeCallbacks(timeoutRunnable)
                    if (location != null) {
                        lat = location.latitude
                        lon = location.longitude
                    } else {
                        requestLocationUpdate()
                    }
                }.addOnFailureListener {
                    handler.removeCallbacks(timeoutRunnable)
                    Toast.makeText(this, "Gagal mendapatkan lokasi: ${it.message}", Toast.LENGTH_SHORT).show()

                }
            } else {
                Toast.makeText(this, "Gagal mendapatkan lokasi: GPS belum diaktifkan", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun requestLocationUpdate() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            lat = location.latitude
                            lon = location.longitude
                        } else {
                            Toast.makeText(this@AddStoryActivity, "Gagal mendapatkan lokasi. Pastikan GPS aktif.", Toast.LENGTH_SHORT).show()
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                },
                this.mainLooper
            )
        } else {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
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
            startActivity(Intent(this@AddStoryActivity, LoginActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}