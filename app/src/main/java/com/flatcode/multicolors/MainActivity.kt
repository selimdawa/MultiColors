package com.flatcode.multicolors

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import io.selimdawa.multicolors.R as MultiColorR
import com.flatcode.multicolors.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.colorfulAvatar.imageView.load("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&q=80")

        val myCustomColors = listOf(
            getColor(MultiColorR.color.mc_avatar_1),
            getColor(MultiColorR.color.mc_avatar_2),
            getColor(MultiColorR.color.mc_avatar_3),
            getColor(MultiColorR.color.mc_avatar_4),
            getColor(MultiColorR.color.mc_avatar_5),
            getColor(MultiColorR.color.mc_avatar_6),
            getColor(MultiColorR.color.mc_avatar_7),
            getColor(MultiColorR.color.mc_avatar_8),
            getColor(MultiColorR.color.mc_avatar_9),
            getColor(MultiColorR.color.mc_avatar_10)
        )

        binding.colorfulAvatar.setOnLongClickListener {
            if (binding.colorfulAvatar.isUsingCustomColors) {
                binding.colorfulAvatar.resetToThemeColors()
            } else {
                binding.colorfulAvatar.setColors(myCustomColors)
            }
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