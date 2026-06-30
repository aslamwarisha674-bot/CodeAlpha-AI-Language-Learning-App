package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LingoApp(
    viewModel: LanguageViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    // Determine if the bottom navigation bar should be visible (only on HOME and STATS screens)
    val isBottomBarVisible = currentScreen == AppScreen.HOME || currentScreen == AppScreen.HISTORY_STATS

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    modifier = Modifier
                        .testTag("app_bottom_nav")
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home_tab")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HISTORY_STATS,
                        onClick = { viewModel.navigateTo(AppScreen.HISTORY_STATS) },
                        icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "My Stats") },
                        label = { Text("My Stats") },
                        modifier = Modifier.testTag("nav_stats_tab")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated visibility or crossfade transitions for screen switches
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 250),
                label = "ScreenTransitions"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> {
                        HomeScreen(viewModel = viewModel)
                    }
                    AppScreen.CATEGORY_DETAIL -> {
                        CategoryDetailScreen(viewModel = viewModel)
                    }
                    AppScreen.FLASHCARD_STUDY -> {
                        FlashcardScreen(viewModel = viewModel)
                    }
                    AppScreen.QUIZ_STUDY -> {
                        QuizScreen(viewModel = viewModel)
                    }
                    AppScreen.HISTORY_STATS -> {
                        HistoryStatsScreen(viewModel = viewModel)
                    }
                    else -> {
                        HomeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
