package com.flatcode.multicolors.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.selimdawa.multicolors.MultiColorTheme
import io.selimdawa.multicolors.multiColorBackground

class ComposeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MultiColorTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .multiColorBackground(), color = Color.Transparent
                ) {
                    ComposeTestContent()
                }
            }
        }
    }
}
