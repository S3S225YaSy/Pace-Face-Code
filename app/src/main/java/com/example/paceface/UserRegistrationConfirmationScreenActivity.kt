package com.example.paceface

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paceface.databinding.UserRegistrationConfirmationScreenBinding

class UserRegistrationConfirmationScreenActivity : AppCompatActivity() {

    private lateinit var binding: UserRegistrationConfirmationScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserRegistrationConfirmationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ここに戻るボタンや登録ボタンの処理を記述していきます
    }
}
