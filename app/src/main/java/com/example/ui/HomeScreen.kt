package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LanguageViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val words by viewModel.allWords.collectAsStateWithLifecycle()
    val results by viewModel.allQuizResults.collectAsStateWithLifecycle()

    val langName = viewModel.getLanguageName(selectedLang)
    val langFlag = viewModel.getLanguageFlag(selectedLang)

    val totalWords = words.size
    val masteredWords = words.count { it.isMastered }
    val progressPercent = if (totalWords > 0) (masteredWords.toFloat() / totalWords.toFloat()) else 0f

    val totalQuizzes = results.count { it.language == selectedLang }
    val averageScore = if (totalQuizzes > 0) {
        val totalScore = results.filter { it.language == selectedLang }.sumOf { it.score }
        val totalQuestions = results.filter { it.language == selectedLang }.sumOf { it.totalQuestions }
        if (totalQuestions > 0) (totalScore.toFloat() / totalQuestions.toFloat() * 100).toInt() else 0
    } else 0

    val dynamicStreak = 12
    val dynamicXp = (masteredWords * 20) + (totalQuizzes * 50) + 150

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HIGH DENSITY TOP APP BAR HEADER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(com.example.ui.theme.HighDensityPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = langFlag,
                            fontSize = 24.sp
                        )
                    }
                    Column {
                        Text(
                            text = langName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Intermediate A2 Level",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                // Streak & Score Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak",
                        tint = com.example.ui.theme.FlameColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$dynamicStreak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "XP",
                        tint = com.example.ui.theme.BoltColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$dynamicXp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- LANGUAGE SELECTION TABS ---
        item {
            Column {
                Text(
                    text = "Switch Courses",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val listLanguages = listOf("es", "fr", "ja", "de")
                    listLanguages.forEach { code ->
                        val isSelected = selectedLang == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) com.example.ui.theme.HighDensityPurpleContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { viewModel.selectLanguage(code) }
                                .padding(vertical = 10.dp)
                                .testTag("lang_picker_${code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = viewModel.getLanguageFlag(code),
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = viewModel.getLanguageName(code),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) com.example.ui.theme.HighDensityPurpleContainerText else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- DAILY GOAL PREMIUM CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityPurpleContainer),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Study Goal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.HighDensityPurpleContainerText
                            )
                            Text(
                                text = if (totalWords > 0) "$masteredWords / $totalWords words mastered" else "No words added yet",
                                fontSize = 12.sp,
                                color = com.example.ui.theme.HighDensityPurpleContainerText.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.HighDensityPurpleContainerText
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(com.example.ui.theme.HighDensityPurpleContainer.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = if (progressPercent > 0f) progressPercent else 0.05f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // --- LEARNING CATEGORIES GRID ---
        item {
            Column {
                Text(
                    text = "Learning Categories",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Grid layout with 2 columns
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Vocabulary
                        Card(
                            modifier = Modifier
                                .weight(1.0f)
                                .clickable {
                                    viewModel.selectCategory("Vocabulary")
                                    viewModel.navigateTo(AppScreen.CATEGORY_DETAIL)
                                }
                                .testTag("category_card_Vocabulary"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(com.example.ui.theme.HighDensityPurpleContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = "Vocabulary",
                                        tint = com.example.ui.theme.HighDensityPurpleContainerText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Vocabulary",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$totalWords words active",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Card 2: Grammar
                        Card(
                            modifier = Modifier
                                .weight(1.0f)
                                .clickable {
                                    viewModel.selectCategory("Grammar")
                                    viewModel.navigateTo(AppScreen.CATEGORY_DETAIL)
                                }
                                .testTag("category_card_Grammar"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(com.example.ui.theme.HighDensityPurpleContainer.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Grammar",
                                        tint = com.example.ui.theme.HighDensityPurpleContainerText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Grammar",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Verbs & structures",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 3: Speaking/Phrases
                        Card(
                            modifier = Modifier
                                .weight(1.0f)
                                .clickable {
                                    viewModel.selectCategory("Phrases")
                                    viewModel.navigateTo(AppScreen.CATEGORY_DETAIL)
                                }
                                .testTag("category_card_Phrases"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(com.example.ui.theme.SpeakingCardBg.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Phrases",
                                        tint = com.example.ui.theme.SpeakingCardText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Phrases",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Practical expressions",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Card 4: Quick Quiz
                        Card(
                            modifier = Modifier
                                .weight(1.0f)
                                .clickable { viewModel.startQuizStudy() }
                                .testTag("quick_quiz_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(com.example.ui.theme.QuizCardBg.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Challenge",
                                        tint = com.example.ui.theme.QuizCardText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Quick Quiz",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Test your mastery",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- NEXT LESSON (FEATURED DEEP PURPLE CARD) ---
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Coming Up Next",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    TextButton(
                        onClick = {
                            viewModel.selectCategory("Phrases")
                            viewModel.navigateTo(AppScreen.CATEGORY_DETAIL)
                        }
                    ) {
                        Text(
                            text = "View details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "UNIT 4",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "10 MINS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ordering Food at a Restaurant",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Learn key phrases for dining out, polite requests, and getting custom recommendations.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                viewModel.selectCategory("Phrases")
                                viewModel.navigateTo(AppScreen.CATEGORY_DETAIL)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quick_quiz_button"), // Matches CTA tags if needed
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.HighDensityPurpleContainer,
                                contentColor = com.example.ui.theme.HighDensityPurpleContainerText
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "START LESSON",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
