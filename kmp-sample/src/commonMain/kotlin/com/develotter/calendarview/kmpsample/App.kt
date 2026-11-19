package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
internal fun App(
    todayLabels: Map<DemoCalendar, String>,
    dateDetails: (Long) -> List<String>,
    renderCalendar: @Composable (CalendarExplorerOptions, (Long) -> Unit) -> Unit,
    renderViewport: @Composable (PlaygroundOptions, (Long) -> Unit, (Long) -> Boolean) -> Unit,
    renderDialogMonth: @Composable (DemoCalendar) -> Unit,
    rangePolicyAccepts: (Long) -> Boolean,
    renderHeatmap: @Composable (HeatmapOptions, (String) -> Unit) -> Unit,
) {
    var pageName by rememberSaveable { mutableStateOf(DemoPage.Home.name) }
    var calendarName by rememberSaveable { mutableStateOf(DemoCalendar.Gregorian.name) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    var languageName by rememberSaveable { mutableStateOf(AppLanguage.English.name) }
    var themeModeName by rememberSaveable { mutableStateOf(AppThemeMode.System.name) }
    var paletteName by rememberSaveable { mutableStateOf(AppThemePalette.Blue.name) }

    val settings = AppSettings(
        language = AppLanguage.valueOf(languageName),
        themeMode = AppThemeMode.valueOf(themeModeName),
        palette = AppThemePalette.valueOf(paletteName),
    )

    val onSettingsChange: (AppSettings) -> Unit = { newSettings ->
        languageName = newSettings.language.name
        themeModeName = newSettings.themeMode.name
        paletteName = newSettings.palette.name
    }

    val screenStateHolder = rememberSaveableStateHolder()

    val page = DemoPage.valueOf(pageName)
    val calendar = DemoCalendar.valueOf(calendarName)

    val navigate: (DemoPage) -> Unit = { destination ->
        pageName = destination.name
        menuOpen = false
    }

    SampleTheme(settings = settings) {
        Surface(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth >= 840.dp

                Row(Modifier.fillMaxSize()) {
                    if (wide) {
                        Surface(
                            modifier = Modifier.width(230.dp).fillMaxHeight(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            SampleNavigation(
                                currentPage = page,
                                onNavigate = navigate,
                                language = settings.language,
                                modifier = Modifier.fillMaxHeight(),
                            )
                        }
                    }

                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!wide) {
                                OutlinedButton(onClick = { menuOpen = true }) {
                                    Text(SampleStrings.menu(settings.language))
                                }
                            }
                            Text(
                                text = SampleStrings.pageTitle(page, settings.language),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        HorizontalDivider()

                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            screenStateHolder.SaveableStateProvider(page.name) {
                                when (page) {
                                    DemoPage.Home -> SampleHomeScreen(
                                        todayLabels = todayLabels,
                                        onOpenCalendar = { selected ->
                                            calendarName = selected.name
                                            navigate(DemoPage.Calendars)
                                        },
                                        onNavigate = navigate,
                                    )

                                    DemoPage.Calendars -> SampleCalendarsScreen(
                                        calendar = calendar,
                                        onCalendarChange = { calendarName = it.name },
                                        dateDetails = dateDetails,
                                        renderCalendar = renderCalendar,
                                    )

                                    DemoPage.Playground -> SamplePlaygroundScreen(
                                        renderViewport = renderViewport,
                                        renderDialogMonth = renderDialogMonth,
                                        rangePolicyAccepts = rangePolicyAccepts,
                                    )

                                    DemoPage.Heatmaps -> SampleHeatmapsScreen(
                                        renderHeatmap = renderHeatmap,
                                    )

                                    DemoPage.Settings -> SampleSettingsScreen(
                                        settings = settings,
                                        onSettingsChange = onSettingsChange,
                                    )
                                }
                            }
                        }
                    }
                }

                if (!wide && menuOpen) {
                    Dialog(onDismissRequest = { menuOpen = false }) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 6.dp,
                        ) {
                            SampleNavigation(
                                currentPage = page,
                                onNavigate = navigate,
                                language = settings.language,
                                onClose = { menuOpen = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleNavigation(
    currentPage: DemoPage,
    onNavigate: (DemoPage) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = SampleStrings.appTitle(language),
                style = MaterialTheme.typography.titleMedium,
            )
            if (onClose != null) {
                TextButton(onClick = onClose) {
                    Text(SampleStrings.close(language))
                }
            }
        }

        HorizontalDivider()

        DemoPage.entries.forEach { page ->
            NavigationDrawerItem(
                label = { Text(SampleStrings.pageTitle(page, language)) },
                selected = currentPage == page,
                onClick = { onNavigate(page) },
            )
        }
    }
}
