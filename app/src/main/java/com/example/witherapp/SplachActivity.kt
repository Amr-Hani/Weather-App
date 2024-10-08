package com.example.witherapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplachActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splach)
        Handler().postDelayed({
            val intent = Intent(this@SplachActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}