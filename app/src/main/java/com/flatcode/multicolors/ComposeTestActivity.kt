package com.flatcode.multicolors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import io.selimdawa.multicolors.*
import io.selimdawa.multicolors.R
import coil.compose.AsyncImage

class ComposeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MultiColorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ComposeTestContent()
                }
            }
        }
    }
}

@Composable
fun ComposeTestContent() {
    var showThemeDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showThemeDialog) {
        MultiColorThemeDialog(onDismissRequest = { showThemeDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "MultiColors Compose Test",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // 1. MultiColorNightModeButton & Theme Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MultiColorNightModeButton(
                lightIconRes = R.drawable.mc_ic_light,
                darkIconRes = R.drawable.mc_ic_dark,
                modifier = Modifier.size(48.dp)
            )

            MultiColorButton(onClick = { showThemeDialog = true }) {
                Text("Select Theme")
            }
        }

        HorizontalDivider()

        // 2. MultiColorAvatar
        Text("MultiColor Avatar", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MultiColorAvatar(
                modifier = Modifier.size(100.dp),
                image = {
                    AsyncImage(
                        model = "https://picsum.photos/200",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                },
                animateBorder = true
            )

            MultiColorAvatar(
                modifier = Modifier.size(100.dp),
                image = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", color = Color.White)
                    }
                },
                animateBorder = true,
                useRainbow = true
            )
        }

        HorizontalDivider()

        // 3. MultiColorBorderBox
        Text("MultiColor BorderBox", style = MaterialTheme.typography.titleMedium)
        MultiColorBorderBox(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            glowRadius = 8.dp,
            animate = true
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Content inside BorderBox")
            }
        }

        HorizontalDivider()

        // 4. Modifiers and Animated Colors
        Text("Animated Background Modifier", style = MaterialTheme.typography.titleMedium)
        
        val animatedBrush = MultiColorCompose.animatedBrush()
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(animatedBrush, shape = RoundedCornerShape(12.dp))
                .multiColorBorder(width = 2.dp, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Animated Gradient Background", color = Color.White)
        }

        // 5. MultiColorBox
        Text("MultiColor Box", style = MaterialTheme.typography.titleMedium)
        MultiColorBox(
            modifier = Modifier.size(150.dp, 60.dp),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Rounded Box", color = Color.White)
        }
    }
}
