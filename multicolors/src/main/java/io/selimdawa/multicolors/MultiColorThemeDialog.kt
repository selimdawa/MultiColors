package io.selimdawa.multicolors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val themeKey = stringPreferencesKey("color_option")
private val hiddenThemesKey = stringSetPreferencesKey("hidden_themes")

/**
 * A Compose implementation of the MultiColor theme selector dialog.
 */
@Composable
fun MultiColorThemeDialog(
    onDismissRequest: () -> Unit
) {
    var isManageMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentThemeId by MultiColorManager.currentThemeId.collectAsState()
    
    val hiddenThemes by remember(context) {
        context.multiColorDataStore.data.map { it[hiddenThemesKey] ?: emptySet() }
    }.collectAsState(initial = emptySet())

    val allThemes = remember {
        ThemeRegistry.getAllThemes().filter { it.id !in MultiColorManager.excludedThemeIds }
    }

    val themesToShow = if (isManageMode) allThemes else allThemes.filter { it.id !in hiddenThemes }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isManageMode) {
                            IconButton(onClick = { isManageMode = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                        Text(
                            text = stringResource(if (isManageMode) R.string.mc_manage_themes else R.string.mc_select_theme),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isManageMode) {
                        TextButton(onClick = { isManageMode = true }) {
                            Text(stringResource(R.string.mc_edit), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(themesToShow) { theme ->
                        ThemeItem(
                            theme = theme,
                            isSelected = theme.id == currentThemeId,
                            isManageMode = isManageMode,
                            isHidden = theme.id in hiddenThemes,
                            onThemeClick = {
                                if (isManageMode) {
                                    scope.launch {
                                        context.multiColorDataStore.edit { prefs ->
                                            val current = prefs[hiddenThemesKey]?.toMutableSet() ?: mutableSetOf()
                                            if (current.contains(theme.id)) current.remove(theme.id)
                                            else current.add(theme.id)
                                            prefs[hiddenThemesKey] = current
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        context.multiColorDataStore.edit { prefs ->
                                            prefs[themeKey] = theme.id
                                        }
                                    }
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeItem(
    theme: MultiColorTheme,
    isSelected: Boolean,
    isManageMode: Boolean,
    isHidden: Boolean,
    onThemeClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = remember(theme) {
        MultiColorManager.getThemeColors(context, theme).map { Color(it) }
    }
    val brush = remember(colors) {
        Brush.linearGradient(colors)
    }

    Surface(
        onClick = onThemeClick,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(brush)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = stringResource(theme.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            if (isManageMode) {
                Icon(
                    imageVector = if (isHidden) Icons.Default.Add else Icons.Default.Delete,
                    contentDescription = null,
                    tint = if (isHidden) Color.Green else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
