package com.flatcode.multicolors.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.selimdawa.multicolors.MultiColorCompose
import io.selimdawa.multicolors.MultiColorTheme

class ComposeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MultiColorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MultiColorCompose.colorOnBackground
                ) {
                    ComposeTestContent()
                }
            }
        }
    }
}