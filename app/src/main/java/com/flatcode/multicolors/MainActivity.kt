package com.flatcode.multicolors

import android.content.Intent
import android.content.res.Configuration
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

        binding.colorfulAvatar.setOnClickListener {
            val nextAnimState = !binding.colorfulAvatar.isAnimatingImage
            binding.colorfulAvatar.setAnimateImage(nextAnimState)
            binding.colorfulAvatar.setAnimateBorder(nextAnimState)
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