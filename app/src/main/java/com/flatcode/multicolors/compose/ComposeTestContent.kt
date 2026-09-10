package com.flatcode.multicolors.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.selimdawa.multicolors.MultiColorAvatar
import io.selimdawa.multicolors.MultiColorBorderBox
import io.selimdawa.multicolors.MultiColorBox
import io.selimdawa.multicolors.MultiColorCompose
import io.selimdawa.multicolors.MultiColorRectBorder
import io.selimdawa.multicolors.MultiColorThemeDialog
import com.flatcode.multicolors.R as AppR

@Composable
fun ComposeTestContent() {
    var showThemeDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showThemeDialog) {
        MultiColorThemeDialog(onDismissRequest = { showThemeDialog = false })
    }

    Scaffold(
        topBar = {
            ToolbarContent()
        }, containerColor = MultiColorCompose.colorOnBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 1. MultiColorAvatar
            MultiColorAvatar(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(160.dp),
                image = {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=500&q=80",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                },
                borderThickness = 6.dp,
                glowRadius = 10.dp,
                animateBorder = true,
                alwaysWhite = true,
                showContrast = true,
                contrastSize = 0.25f
            )

            // 2. MultiColorBox (MaterialCardView equivalent)
            MultiColorBox(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(150.dp),
                shape = RoundedCornerShape(24.dp)
            )

            // 3. "New" Section
            Text(
                text = stringResource(AppR.string.new_version),
                color = MultiColorCompose.mc_track,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )

            MultiColorBorderBox(
                modifier = Modifier
                    .width(220.dp)
                    .clickable { },
                thickness = 4.dp,
                glowRadius = 6.dp,
                useRainbow = true,
                cornerRadius = 10.dp
            ) {
                Text(
                    text = stringResource(AppR.string.test_music_ui),
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp),
                    color = MultiColorCompose.colorError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // 4. "Old" Section
            Text(
                text = stringResource(AppR.string.old_version),
                color = Color.Gray,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 0.dp) // Removed padding top 20
            )

            MultiColorBorderBox(
                modifier = Modifier
                    .padding(10.dp)
                    .width(200.dp)
                    .clickable { },
                thickness = 3.dp,
                useRainbow = false,
                cornerRadius = 8.dp
            ) {
                Text(
                    text = stringResource(AppR.string.test_compose_ui), // Fixed text
                    modifier = Modifier.padding(12.dp),
                    color = MultiColorCompose.colorError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // 5. Red & Blue Border
            Text(
                text = stringResource(AppR.string.red_blue_border),
                color = MultiColorCompose.mc_track,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )

            // Custom implementation for RedBlueBorderBox in Compose
            val redBlueColors = listOf(Color.Red, Color.Blue)
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(200.dp)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                MultiColorRectBorder(
                    modifier = Modifier.matchParentSize(),
                    thickness = 5.dp,
                    cornerRadius = 10.dp,
                    glowRadius = 12.dp,
                    glowAlpha = 0.7f,
                    animate = true,
                    animationDuration = 2000, // Matched with MainActivity speed 2000L
                    customColors = redBlueColors
                )
                Text(
                    text = stringResource(AppR.string.premium_design),
                    modifier = Modifier.padding(21.dp),
                    color = MultiColorCompose.mc_track,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}