package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    viewModel: LanguageViewModel,
    modifier: Modifier = Modifier
) {
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()
    val isFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()
    val selectedLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val langFlag = viewModel.getLanguageFlag(selectedLang)

    if (flashcards.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No flashcards found to study.")
        }
        return
    }

    val currentCard = flashcards[currentIndex]
    val progress = (currentIndex + 1).toFloat() / flashcards.size.toFloat()

    // Speak word automatically when card changes
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        viewModel.speakWord(currentCard.word)
    }

    // Flip Animation calculations
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlipRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- HEADER ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.CATEGORY_DETAIL) },
                modifier = Modifier.testTag("flashcard_back")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Study Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Show custom icon placeholder to balance top bar
            IconButton(onClick = {}, enabled = false) {
                Text(text = langFlag, fontSize = 24.sp)
            }
        }

        // --- DECK PROGRESS INDICATOR ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Card ${currentIndex + 1} of ${flashcards.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "${(progress * 100).toInt()}% Done",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .testTag("flashcard_progress"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- LARGE FLIPPABLE FLASHCARD CONTAINER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clickable { viewModel.flipCard() }
                .testTag("flashcard_card_clickable"),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // --- FRONT OF CARD (Target Word) ---
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentCard.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Core Word
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentCard.word,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pronunciation: [ ${currentCard.pronunciation} ]",
                                fontSize = 16.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bottom Flip hint
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    viewModel.speakWord(currentCard.word)
                                },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(27.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("flashcard_speak")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Listen pronunciation",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap Card to Flip & Reveal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // --- BACK OF CARD (Translation & Context) ---
                // Rotate back content 180 degrees to cancel parent rotationY
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header info
                        Text(
                            text = "ENGLISH TRANSLATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )

                        // Meaning
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentCard.translation,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Context sentence
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Example Context:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCard.exampleSentence,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentCard.exampleTranslation,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Click to return hint
                        Text(
                            text = "Tap Card to Flip Back",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- FOOTER BUTTONS CONTROLLERS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous card
            IconButton(
                onClick = { viewModel.prevCard() },
                enabled = currentIndex > 0,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        if (currentIndex > 0) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    )
                    .testTag("flashcard_prev")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Previous card",
                    tint = if (currentIndex > 0) MaterialTheme.colorScheme.onSurface else Color.LightGray
                )
            }

            // Quick Toggle Mastered during learning
            FilledTonalIconButton(
                onClick = { viewModel.toggleWordMastered(currentCard) },
                modifier = Modifier
                    .size(64.dp)
                    .testTag("flashcard_toggle_mastered")
            ) {
                Icon(
                    imageVector = if (currentCard.isMastered) Icons.Default.Check else Icons.Default.Star,
                    contentDescription = "Mark Mastered",
                    tint = if (currentCard.isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Next card
            IconButton(
                onClick = { viewModel.nextCard() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("flashcard_next")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Next card",
                    tint = Color.White
                )
            }
        }
    }
}
