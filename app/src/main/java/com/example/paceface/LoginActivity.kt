//LoginActivity.kt
package com.example.paceface

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.paceface.databinding.LoginScreenBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        auth = Firebase.auth
        appDatabase = AppDatabase.getDatabase(this)

        tokenManager.clearTokens()
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove("LOGGED_IN_USER_ID")
            remove("LOGGED_IN_FIREBASE_UID")
            apply()
        }
        Log.d("LoginActivity", "Debug: Old auth info cleared.")

        setupPasswordToggle(binding.inputPassword, binding.btnEye)

        binding.btnLogin.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked.")
            login()
        }
    }

    private fun setupPasswordToggle(editText: EditText, eyeButton: ImageButton) {
        editText.transformationMethod = PasswordTransformationMethod.getInstance()
        eyeButton.setColorFilter(Color.GRAY)

        eyeButton.setOnClickListener {
            if (editText.transformationMethod == null) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeButton.setColorFilter(Color.GRAY)
            } else {
                editText.transformationMethod = null
                eyeButton.clearColorFilter()
            }
            editText.setSelection(editText.text.length)
        }
    }

    private fun login() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        binding.errorMessage.visibility = View.INVISIBLE

        if (username.isEmpty()) {
            binding.errorMessage.text = "※ユーザー名を入力してください"
            binding.errorMessage.visibility = View.VISIBLE
            Log.d("LoginActivity", "Username empty.")
            return
        }

        if (password.isEmpty()) {
            binding.errorMessage.text = "※パスワードを入力してください"
            binding.errorMessage.visibility = View.VISIBLE
            Log.d("LoginActivity", "Password empty.")
            return
        }

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("LoginActivity", "Attempting to find email for username: $username")

                val querySnapshot = db.collection("users")
                    .whereEqualTo("name", username)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    Log.d("LoginActivity", "Username '$username' not found in Firestore (searching by 'name' field).")
                    withContext(Dispatchers.Main) {
                        binding.errorMessage.text = "※ユーザー名またはパスワードが正しくありません"
                        binding.errorMessage.visibility = View.VISIBLE
                    }
                    return@launch
                }

                val userDocument = querySnapshot.documents.first()
                val email = userDocument.getString("email")
                val isEmailVerifiedFirestore = userDocument.getBoolean("isEmailVerified") ?: false

                if (email == null) {
                    Log.e("LoginActivity", "Email not found for username '$username' in Firestore document: ${userDocument.id}")
                    withContext(Dispatchers.Main) {
                        binding.errorMessage.text = "※ユーザー情報に不備があります。管理者にお問い合わせください。"
                        binding.errorMessage.visibility = View.VISIBLE
                    }
                    return@launch
                }

                Log.d("LoginActivity", "Found email '$email' for username '$username'. Attempting Firebase sign-in.")

                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Log.i("LoginActivity", "Firebase sign-in successful for user: ${firebaseUser.uid}")

                    val newLocalUser = User(
                        firebaseUid = firebaseUser.uid,
                        name = username,
                        email = email,
                        password = User.hashPassword(password),
                        isEmailVerified = isEmailVerifiedFirestore
                    )
                    val savedUserId = appDatabase.userDao().insert(newLocalUser).toInt() // ★挿入されたuserIdを取得★
                    Log.d("LoginActivity", "Local user info saved/updated for Firebase UID: ${firebaseUser.uid}, localUserId: $savedUserId")

                    val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    with(sharedPrefs.edit()) {
                        putString("LOGGED_IN_FIREBASE_UID", firebaseUser.uid)
                        putInt("LOGGED_IN_USER_ID", savedUserId) // ★localUserIdも保存★
                        apply()
                    }

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@LoginActivity, HomeScreenActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Log.w("LoginActivity", "Firebase sign-in failed: User object is null.")
                    withContext(Dispatchers.Main) {
                        binding.errorMessage.text = "※ユーザー名またはパスワードが正しくありません"
                        binding.errorMessage.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.errorMessage.text = "ログインに失敗しました: ${e.localizedMessage ?: "不明なエラー"}"
                    binding.errorMessage.visibility = View.VISIBLE
                    Snackbar.make(binding.root, "ログインに失敗しました: ${e.localizedMessage ?: "不明なエラー"}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnLogin.isEnabled = true
                    Log.d("LoginActivity", "Login process finished. Button re-enabled.")
                }
            }
        }
    }
}