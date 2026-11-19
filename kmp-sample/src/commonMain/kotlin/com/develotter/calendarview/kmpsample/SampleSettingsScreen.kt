package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Controlled settings page; the caller owns [settings] and persists every change. */
@Composable
internal fun SampleSettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = settings.language
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(SampleStrings.settingsTitle(language), style = MaterialTheme.typography.headlineSmall)

        DemoChoices(
            title = SampleStrings.languageLabel(language),
            items = AppLanguage.entries,
            selected = settings.language,
            label = { SampleStrings.languageName(it, language) },
        ) { selected -> onSettingsChange(settings.copy(language = selected)) }

        DemoChoices(
            title = SampleStrings.themeLabel(language),
            items = AppThemeMode.entries,
            selected = settings.themeMode,
            label = { SampleStrings.themeModeName(it, language) },
        ) { selected -> onSettingsChange(settings.copy(themeMode = selected)) }

        DemoChoices(
            title = SampleStrings.paletteLabel(language),
            items = AppThemePalette.entries,
            selected = settings.palette,
            label = { SampleStrings.paletteName(it, language) },
        ) { selected -> onSettingsChange(settings.copy(palette = selected)) }
    }
}
