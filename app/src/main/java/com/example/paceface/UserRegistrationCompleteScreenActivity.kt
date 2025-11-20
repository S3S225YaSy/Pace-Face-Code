package com.example.paceface

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paceface.databinding.UserRegistrationCompleteScreenBinding

class UserRegistrationCompleteScreenActivity : AppCompatActivity() {

    private lateinit var binding: UserRegistrationCompleteScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserRegistrationCompleteScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 「OK」ボタンがクリックされた時の処理
        binding.btnOk.setOnClickListener {
            // ログイン画面に戻る
            val intent = Intent(this, LoginActivity::class.java).apply {
                // これまでのアクティビティスタックをクリアして、ホーム画面に遷移する(予定。今はログイン画面いれてる)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }
}
