package com.flatcode.multicolors

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import io.selimdawa.multicolors.R as MultiColorR
import com.flatcode.multicolors.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.colorfulAvatar.imageView.load("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=500&q=80")

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
            android.util.Log.d("Navigation", "trans clicked")
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }

        binding.transOld.setOnClickListener {
            android.util.Log.d("Navigation", "transOld clicked")
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }

        updateNightModeButton()

        binding.btnNightMode.setOnClickListener { toggleNightMode(it) }
        binding.tvNightMode.setOnClickListener { toggleNightMode(it) }

        // New animated border settings
        binding.redBlueBorder.apply {
            setAnimationSpeed(2000L) // Rotation speed
            setBorderThickness(5f)   // Border thickness
            setCornerRadius(20f)    // Corner radius
            setGlowRadius(12f)      // Glow intensity (Neon Glow)
            
            // Transition on click
            setOnClickListener {
                val intent = Intent(this@MainActivity, TestActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun updateNightModeButton() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentMode == Configuration.UI_MODE_NIGHT_YES
        android.util.Log.d("NightMode", "Updating button: isNightMode = $isNightMode")
        binding.tvNightMode.text =
            if (isNightMode) getString(R.string.light_mode) else getString(R.string.dark_mode)

        binding.toolbar.btnThemeToggle.setImageResource(
            if (isNightMode) R.drawable.ic_light else R.drawable.ic_night
        )
    }
}