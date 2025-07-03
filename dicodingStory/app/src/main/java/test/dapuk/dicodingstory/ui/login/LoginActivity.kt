package test.dapuk.dicodingstory.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.data.local.sharedpref.SharedPreferenceManager
import test.dapuk.dicodingstory.databinding.ActivityLoginBinding
import test.dapuk.dicodingstory.ui.main.MainActivity
import test.dapuk.dicodingstory.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        sharedPreferenceManager = SharedPreferenceManager(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playAnimation()

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            var email = binding.edLoginEmail.text.toString()
            var password = binding.edLoginPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()

                loginViewModel.login(email, password)

            } else {
                when {
                    email.isEmpty() && password.isEmpty() -> Toast.makeText(
                        this,
                        "Email & Password tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()

                    email.isEmpty() -> Toast.makeText(
                        this,
                        "Email tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()

                    password.isEmpty() -> Toast.makeText(
                        this,
                        "Password tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        loginViewModel.isLoading.observe(this) { isLoading ->
            loginLoading(isLoading)
        }

        loginViewModel.loginUserId.observe(this) { userId ->
            val name = loginViewModel.loginName.value
            val token = loginViewModel.loginToken.value
            Log.d("viewmodel response", "UserId: $userId, Name: $name, Token: $token")

            if (userId != null && name != null && token != null) {
                saveSessionPref(userId, name, token)
            }
        }

        loginViewModel.loginSuccess.observe(this) {
            successLoginIntent(it)
        }

        loginViewModel.loginErr.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }


    }

    fun saveSessionPref(userId: String, name: String, token: String) {
        sharedPreferenceManager.saveSession(userId, name, token)
        Log.d("SessionSave", "Session saved: userId=$userId, name=$name, token=$token")
    }

    fun successLoginIntent(success: Boolean) {
        if (success.equals(true)) {
            Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    fun loginLoading(isLoading: Boolean) {
        if (isLoading != false) {
            binding.pbLoading.visibility = View.VISIBLE
            binding.ivLogin.visibility = View.GONE
            binding.btnLogin.visibility = View.GONE
            binding.emailLayout.visibility = View.GONE
            binding.passwordLayout.visibility = View.GONE
            binding.tvRegister.visibility = View.GONE
        } else {
            binding.pbLoading.visibility = View.GONE
            binding.ivLogin.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.emailLayout.visibility = View.VISIBLE
            binding.passwordLayout.visibility = View.VISIBLE
            binding.tvRegister.visibility = View.VISIBLE
        }
    }

    private fun playAnimation() {
        // Create a horizontal bouncing animation for the login icon
        ObjectAnimator.ofFloat(binding.ivLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        // Fade-in animations for the UI components
        val loginTitle = ObjectAnimator.ofFloat(binding.tvLoginTitle, View.ALPHA, 0f, 1f).setDuration(500)
        val emailField = ObjectAnimator.ofFloat(binding.emailLayout, View.ALPHA, 0f, 1f).setDuration(500)
        val passwordField = ObjectAnimator.ofFloat(binding.passwordLayout, View.ALPHA, 0f, 1f).setDuration(500)
        val loginButton = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 0f, 1f).setDuration(500)
        val registerText = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 0f, 1f).setDuration(500)
        val appName = ObjectAnimator.ofFloat(binding.tvDicodingStoryApp, View.ALPHA, 0f, 1f).setDuration(500)
        val appImage = ObjectAnimator.ofFloat(binding.ivLogin, View.ALPHA, 0f, 1f).setDuration(500)
        val appedEmail = ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 0f, 1f).setDuration(500)
        val appedpassword = ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 0f, 1f).setDuration(500)



        // AnimatorSet for sequential animations
        AnimatorSet().apply {
            playSequentially(
                loginTitle,
                appedEmail,
                appedpassword,
                appImage,
                emailField,
                passwordField,
                loginButton,
                registerText,
                appName
            )
            startDelay = 500
            start()
        }
    }


}

/*
*
    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showErrorDialog("Email dan Password harus diisi!")
            } else {
                lifecycleScope.launch {
                    val userRepository = UserRepository.getInstance(UserPreference.getInstance(applicationContext.dataStore))
                    userRepository.login(applicationContext, email, password).collect { result ->
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            is Result.Success -> {
                                showLoading(false)

                                // Ambil data login dari API
                                val loginResult = result.data
                                val userModel = UserModel(
                                    email = loginResult.email,
                                    token = loginResult.token,
                                    name = loginResult.name,   // Ambil nama dari API
                                    isLogin = true
                                )

                                saveUserToPreferences(userModel)  // Simpan data ke SharedPreferences (opsional)
                                navigateToDashboard(userModel)
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showErrorDialog(result.error)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun saveUserToPreferences(userModel: UserModel) {
        val sharedPref = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("email", userModel.email)
            putString("token", userModel.token)
            putString("name", userModel.name)  // Simpan nama
            putBoolean("isLogin", userModel.isLogin)
            apply()
        }
    }

    private fun navigateToDashboard(userModel: UserModel) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            putExtra("USER_MODEL", userModel)
        }
        startActivity(intent)
        finish() // Close LoginActivity after navigating to DashboardActivity
    }


    private fun playAnimation() {

        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_Y, -20f, 20f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        // Animasi untuk elemen-elemen dalam halaman login
        val fadeInEmailText = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 0f, 1f).setDuration(400)
        val fadeInEmailInput = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f).setDuration(400)
        val fadeInPasswordText = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 0f, 1f).setDuration(400)
        val fadeInPasswordInput = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f).setDuration(400)
        val fadeInLoginButton = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 0f, 1f).setDuration(400)
        val fadeInImage = ObjectAnimator.ofFloat(binding.imageView, View.ALPHA, 0f, 1f).setDuration(400)
        val messageTextView = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 0f, 1f).setDuration(400)
        val titleTextView = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).setDuration(400)

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            fadeInImage,
            titleTextView,
            messageTextView,
            fadeInEmailText,
            fadeInEmailInput,
            fadeInPasswordText,
            fadeInPasswordInput,
            fadeInLoginButton
        )
        animatorSet.startDelay = 50
        animatorSet.start()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton("OK", null)
            create()
            show()
        }
* */