package com.flatcode.multicolors.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.flatcode.multicolors.R
import io.selimdawa.multicolors.MultiColorButton
import io.selimdawa.multicolors.MultiColorCompose
import io.selimdawa.multicolors.MultiColorNightModeButton

@Composable
fun ToolbarContent(
    modifier: Modifier = Modifier,
    includeStatusBarsPadding: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (includeStatusBarsPadding) Modifier.statusBarsPadding() else Modifier)
            .background(MultiColorCompose.colorOnBackground)
            .height(56.dp) // Equivalent to ?attr/actionBarSize
            .padding(horizontal = 16.dp)
    ) {
        // Start: Logo
        AndroidView(
            factory = { ctx -> MultiColorButton(ctx) },
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.CenterStart)
        )

        // Center: Multi Colors Title
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.multi),
                color = MultiColorCompose.mc_track,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.colors),
                color = MultiColorCompose.mc_tick,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // End: Night Mode Toggle
        MultiColorNightModeButton(
            lightIconRes = io.selimdawa.multicolors.R.drawable.mc_ic_light,
            darkIconRes = io.selimdawa.multicolors.R.drawable.mc_ic_dark,
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.CenterEnd)
        )
    }
}