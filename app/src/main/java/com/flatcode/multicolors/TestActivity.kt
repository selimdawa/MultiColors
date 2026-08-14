package com.flatcode.multicolors

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.flatcode.multicolors.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Loading images from URL using Coil
        binding.toolbar2.ivFavourites.load("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80")
    }
}