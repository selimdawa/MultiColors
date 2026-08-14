package com.flatcode.multicolors

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.flatcode.multicolors.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.colorfulAvatar.imageView.load("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&q=80")

        val myCustomColors = listOf(
            Color.parseColor("#FF0000"), // أحمر
            Color.parseColor("#FF7F00"), // برتقالي
            Color.parseColor("#FFFF00"), // أصفر
            Color.parseColor("#00FF00"), // أخضر
            Color.parseColor("#0000FF"), // أزرق
            Color.parseColor("#4B0082"), // نيلي
            Color.parseColor("#8B00FF"), // بنفسجي
            Color.parseColor("#FF1493"), // وردي
            Color.parseColor("#00FFFF"), // سماوي
            Color.parseColor("#ADFF2F"), // ليموني
        )
        binding.colorfulAvatar.setColors(myCustomColors)

        // Reset to theme colors on LONG click
        binding.colorfulAvatar.setOnLongClickListener {
            binding.colorfulAvatar.resetToThemeColors()
            true
        }

        binding.trans.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }

        updateNightModeButton()

        binding.btnNightMode.setOnClickListener {
            val isNightMode =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun updateNightModeButton() {
        val isNightMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        binding.tvNightMode.text =
            if (isNightMode) getString(R.string.light_mode) else getString(R.string.dark_mode)
    }
}