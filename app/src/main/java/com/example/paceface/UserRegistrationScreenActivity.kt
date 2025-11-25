package com.example.paceface

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.paceface.databinding.UserRegistrationScreenBinding
import kotlinx.coroutines.launch

class UserRegistrationScreenActivity : AppCompatActivity() {

    private lateinit var binding: UserRegistrationScreenBinding
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // データベースインスタンスを取得
        appDatabase = AppDatabase.getDatabase(this)

        setupPasswordVisibilityToggle()

        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                // This doesn't seem right, we go to confirmation screen, not register user directly.
                // Let's go to confirmation screen with the data
                val name = binding.etUsername.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()

                val intent = Intent(this@UserRegistrationScreenActivity, UserRegistrationConfirmationScreenActivity::class.java).apply {
                    putExtra("USER_NAME", name)
                    putExtra("USER_EMAIL", email)
                    putExtra("USER_PASSWORD", password)
                }
                startActivity(intent)
                // We should not finish here, so user can go back.
            }
        }
    }

    private fun setupPasswordVisibilityToggle() {
        binding.btnPasswordEye.setOnClickListener {
            // Toggle password visibility
            val editText = binding.etPassword
            if (editText.transformationMethod == null) {
                // Hide password
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                // Show password
                editText.transformationMethod = null
            }
            // Move cursor to the end
            editText.setSelection(editText.text.length)
        }

        binding.btnPassword2Eye.setOnClickListener {
            // Toggle password confirmation visibility
            val editText = binding.etPassword2
            if (editText.transformationMethod == null) {
                // Hide password
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                // Show password
                editText.transformationMethod = null
            }
            // Move cursor to the end
            editText.setSelection(editText.text.length)
        }
    }

    private fun validateInputs(): Boolean {
        // Reset errors
        binding.tvUserNameError.visibility = View.GONE
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        binding.tvPassword2Error.visibility = View.GONE

        var isValid = true

        if (binding.etUsername.text.toString().trim().isEmpty()) {
            binding.tvUserNameError.visibility = View.VISIBLE
            isValid = false
        }

        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmailError.visibility = View.VISIBLE
            isValid = false
        }

        val password = binding.etPassword.text.toString()
        if (password.length < 8) {
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }

        val password2 = binding.etPassword2.text.toString()
        if (password != password2) {
            binding.tvPassword2Error.visibility = View.VISIBLE
            isValid = false
        }

        return isValid
    }

    // This function is no longer called from btnRegister, but let's keep it for now
    // as it will be used from the confirmation screen.
    private fun registerUser() {
        val name = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // TODO: パスワードのハッシュ化をここに実装します

        val user = User(
            name = name,
            email = email,
            password = password // 注意: 現在は平文のままです
        )

        lifecycleScope.launch {
            try {
                appDatabase.userDao().insert(user)
                Toast.makeText(this@UserRegistrationScreenActivity, "登録が完了しました", Toast.LENGTH_SHORT).show()

                // 登録完了画面へ遷移
                val intent = Intent(this@UserRegistrationScreenActivity, UserRegistrationCompleteScreenActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // 登録画面を終了
            } catch (e: Exception) {
                // Handle potential conflicts (e.g., email already exists)
                if (e is android.database.sqlite.SQLiteConstraintException) {
                     binding.tvEmailError.text = "※このメールアドレスは既に使用されています"
                     binding.tvEmailError.visibility = View.VISIBLE
                } else {
                     Toast.makeText(this@UserRegistrationScreenActivity, "登録に失敗しました: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
