package test.dapuk.dicodingstory.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import test.dapuk.dicodingstory.R
import test.dapuk.dicodingstory.databinding.ActivityRegisterBinding
import test.dapuk.dicodingstory.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playAnimation()

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        binding.btnRegister.setOnClickListener {
            var name = binding.edRegisterName.text.toString()
            var email = binding.edRegisterEmail.text.toString()
            var password = binding.edRegisterPassword.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Registering", LENGTH_SHORT).show()
                registerViewModel.register(name, email, password)
            } else {
                when {
                    name.isEmpty() && email.isEmpty() && password.isEmpty() -> Toast.makeText(
                        this,
                        "Data masih kosong",
                        LENGTH_SHORT
                    ).show()

                    name.isEmpty() && password.isEmpty() -> Toast.makeText(
                        this,
                        "Nama & Password tidak boleh kosong",
                        LENGTH_SHORT
                    ).show()

                    email.isEmpty() && password.isEmpty() -> Toast.makeText(
                        this,
                        "Email & Password tidak boleh kosong",
                        LENGTH_SHORT
                    ).show()

                    name.isEmpty() && email.isEmpty() -> Toast.makeText(
                        this,
                        "Nama & Email tidak boleh kosong",
                        LENGTH_SHORT
                    ).show()

                    name.isEmpty() -> Toast.makeText(
                        this,
                        "Nama tidak boleh kosong",
                        LENGTH_SHORT
                    ).show()

                    email.isEmpty() -> Toast.makeText(
                        this,
                        "Email tidak boleh kosong",
                        LENGTH_SHORT
                    ).show()

                    password.isEmpty() -> Toast.makeText(
                        this,
                        "Password tidak boleh kosong",
                        LENGTH_SHORT
                    ).show()
                }
            }

        }

        registerViewModel.registerSuccess.observe(this) {

            successRegisterIntent(it)
        }

        registerViewModel.registerErr.observe(this) {
            Toast.makeText(this, it, LENGTH_SHORT).show()
        }

        registerViewModel.isLoading.observe(this) {
            regisLoading(it)
        }

    }

    fun successRegisterIntent(success: Boolean) {
        if (success.equals(true)) {
            Toast.makeText(this, "Success Registering", LENGTH_SHORT).show()
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    fun regisLoading(isLoading: Boolean) {
        if (isLoading != false) {
            binding.pbLoading.visibility = View.VISIBLE
            binding.ivRegister.visibility = View.GONE
            binding.btnRegister.visibility = View.GONE
            binding.emailLayout.visibility = View.GONE
            binding.passwordLayout.visibility = View.GONE
            binding.tvLogin.visibility = View.GONE
            binding.nameLayout.visibility = View.GONE
            binding.tvLogin.visibility= View.GONE
            binding.edRegisterName.visibility= View.GONE
            binding.edRegisterEmail.visibility= View.GONE

        } else {
            binding.pbLoading.visibility = View.GONE
            binding.ivRegister.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
            binding.emailLayout.visibility = View.VISIBLE
            binding.passwordLayout.visibility = View.VISIBLE
            binding.tvLogin.visibility = View.VISIBLE
            binding.nameLayout.visibility = View.VISIBLE
            binding.tvLogin.visibility= View.VISIBLE
            binding.edRegisterName.visibility= View.VISIBLE
            binding.edRegisterEmail.visibility= View.VISIBLE

        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivRegister, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val signup = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 0f, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 0f, 1f).setDuration(1000)
        val name = ObjectAnimator.ofFloat(binding.nameLayout, View.ALPHA, 0f, 1f).setDuration(100)
        val email = ObjectAnimator.ofFloat(binding.emailLayout, View.ALPHA, 0f, 1f).setDuration(200)
        val password = ObjectAnimator.ofFloat(binding.passwordLayout, View.ALPHA, 0f, 1f).setDuration(300)
        val ivReg = ObjectAnimator.ofFloat(binding.ivRegister, View.ALPHA, 0f,1f).setDuration(500)


        // Animasi tambahan untuk edRegisterName dan edRegisterEmail
        val edName = ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 0f, 1f).setDuration(400)
        val edEmail = ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 0f, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(login)
        }

        AnimatorSet().apply {
            playSequentially(signup, edName,edEmail, ivReg,password,name, email,  together)
            start()
        }
    }
/*
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivRegister, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val signup = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(1000)
        val name = ObjectAnimator.ofFloat(binding.nameLayout, View.ALPHA, 1f).setDuration(100)
        val email = ObjectAnimator.ofFloat(binding.emailLayout, View.ALPHA, 1f).setDuration(200)
        val password =
            ObjectAnimator.ofFloat(binding.passwordLayout, View.ALPHA, 1f).setDuration(300)
        val together = AnimatorSet().apply {
            playTogether(login, signup)
        }

        AnimatorSet().apply {
            playSequentially(name, email, password, together)
            start()
        }
    }*/
}