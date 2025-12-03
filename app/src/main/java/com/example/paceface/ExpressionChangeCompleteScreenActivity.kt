package com.example.paceface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ExpressionChangeCompleteScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expression_change_complete_screen)

        val btnOk = findViewById<Button>(R.id.btn_ok)

        // OK ボタン押下時の処理
        btnOk.setOnClickListener {
            // この画面を閉じる
            finish()
        }
    }
}
