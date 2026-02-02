//DeviceConnectionGuideScreenActivity.kt
package com.example.paceface

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.paceface.databinding.DeviceConnectionGuideScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DeviceConnectionGuideScreenActivity : AppCompatActivity() {

    private lateinit var binding: DeviceConnectionGuideScreenBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var appDatabase: AppDatabase

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                checkBluetoothDevice()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DeviceConnectionGuideScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        appDatabase = AppDatabase.getDatabase(this)

        // 「ホームへ」ボタンがクリックされた時の処理
        binding.btnToHome.setOnClickListener {
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 「設定へ」ボタンがクリックされた時の処理
        binding.btnToSettings.setOnClickListener {
            // Bluetooth設定画面を開くためのIntentを作成
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkBluetoothDevice()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothDevice() {
        // 接続済みのデバイスを確認
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            // デバイス名が "raspberrypi" だったらログイン処理をしてホーム画面へ遷移
            if (device.name == "raspberrypi") {
                performAutoLogin()
                return // チェックを終了
            }
        }
    }

    private fun performAutoLogin() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.d("DeviceGuide", "No Firebase user found. Navigating to SelectionScreen.")
            navigateTo(SelectionScreenActivity::class.java)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Firestoreからユーザー情報を取得
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                if (!userDoc.exists()) {
                    Log.e("DeviceGuide", "User document not found in Firestore.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DeviceConnectionGuideScreenActivity, "ユーザー情報が見つかりません。", Toast.LENGTH_SHORT).show()
                        navigateTo(SelectionScreenActivity::class.java)
                    }
                    return@launch
                }

                val name = userDoc.getString("name") ?: "No Name"
                val email = userDoc.getString("email") ?: ""
                val isEmailVerified = firebaseUser.isEmailVerified

                // 2. ローカルDB (Room) の更新または作成
                val existingUser = appDatabase.userDao().getUserByFirebaseUid(firebaseUser.uid)
                val userId = if (existingUser != null) {
                    Log.d("DeviceGuide", "Updating existing local user: ${existingUser.userId}")
                    val updatedUser = existingUser.copy(
                        name = name,
                        email = email,
                        isEmailVerified = isEmailVerified
                    )
                    appDatabase.userDao().update(updatedUser)
                    existingUser.userId
                } else {
                    Log.d("DeviceGuide", "Creating new local user record.")
                    val newUser = User(
                        firebaseUid = firebaseUser.uid,
                        name = name,
                        email = email,
                        password = "", // パスワードは不要またはハッシュ化されたものを取得できないため空
                        isEmailVerified = isEmailVerified
                    )
                    appDatabase.userDao().insert(newUser).toInt()
                }

                // 3. SharedPreferencesへの保存（ログイン状態の維持）
                val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("LOGGED_IN_FIREBASE_UID", firebaseUser.uid)
                    putInt("LOGGED_IN_USER_ID", userId)
                    apply()
                }

                Log.d("DeviceGuide", "Auto-login successful. Navigating to HomeScreen.")
                withContext(Dispatchers.Main) {
                    navigateTo(HomeScreenActivity::class.java)
                }
            } catch (e: Exception) {
                Log.e("DeviceGuide", "Auto-login failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceConnectionGuideScreenActivity, "ログイン処理に失敗しました。", Toast.LENGTH_SHORT).show()
                    navigateTo(SelectionScreenActivity::class.java)
                }
            }
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
