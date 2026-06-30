package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.SoftError
import com.example.ui.theme.SoftSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: LanguageViewModel,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.quizQuestions.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsStateWithLifecycle()
    val isSubmitted by viewModel.isAnswerSubmitted.collectAsStateWithLifecycle()
    val correctCount by viewModel.quizCorrectCount.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isQuizCompleted.collectAsStateWithLifecycle()
    val selectedLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val langName = viewModel.getLanguageName(selectedLang)

    // --- CASE 1: QUIZ SUMMARY / CELEBRATION ---
    if (isCompleted) {
        val totalQuestions = questions.size
        val scorePercent = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions.toFloat() * 100).toInt() else 0

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏆", fontSize = 64.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Quiz Completed!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You tested your $langName knowledge!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scoreboard Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$correctCount / $totalQuestions",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$scorePercent% Accuracy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (scorePercent >= 80) SoftSuccess else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CTA Actions
            Button(
                onClick = { viewModel.startQuizStudy() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("quiz_retry_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Another Quiz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("quiz_home_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back to Home Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Building quiz. Please wait...")
        }
        return
    }

    val currentQuestion = questions[currentIndex]
    val totalQuestions = questions.size
    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()

    // Speak the target word automatically when a new question loads
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        viewModel.speakWord(currentQuestion.word.word)
    }

    // --- CASE 2: ACTIVE QUIZ PLAY ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.testTag("quiz_quit")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quit Quiz",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Daily Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Progress tracker badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${currentIndex + 1}/$totalQuestions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Linear Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .testTag("quiz_progress_bar"),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question display card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .clickable { viewModel.speakWord(currentQuestion.word.word) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QUESTION ${currentIndex + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentQuestion.questionText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { viewModel.speakWord(currentQuestion.word.word) },
                    modifier = Modifier.testTag("quiz_speak_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Speak question word",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multiple Choices Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            currentQuestion.options.forEachIndexed { idx, option ->
                val isSelected = selectedIndex == idx
                val isCorrectAnswer = idx == currentQuestion.correctIndex

                // Decide backing color based on submission status
                val cardColor = when {
                    isSubmitted && isCorrectAnswer -> SoftSuccess.copy(alpha = 0.15f)
                    isSubmitted && isSelected && !isCorrectAnswer -> SoftError.copy(alpha = 0.15f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val strokeColor = when {
                    isSubmitted && isCorrectAnswer -> SoftSuccess
                    isSubmitted && isSelected && !isCorrectAnswer -> SoftError
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .border(
                            width = if (isSelected || (isSubmitted && isCorrectAnswer)) 2.dp else 1.dp,
                            color = if (isSelected || (isSubmitted && isCorrectAnswer)) strokeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isSubmitted) { viewModel.selectQuizAnswer(idx) }
                        .testTag("quiz_option_${idx}"),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A'.code + idx).toChar().toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Right hand check / close icons
                        if (isSubmitted) {
                            if (isCorrectAnswer) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Correct", tint = SoftSuccess)
                            } else if (isSelected) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Incorrect", tint = SoftError)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- SUBMIT / NEXT ACTION BAR ---
        Button(
            onClick = {
                if (isSubmitted) {
                    viewModel.nextQuizQuestion()
                } else {
                    viewModel.submitQuizAnswer()
                }
            },
            enabled = selectedIndex != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("quiz_submit_next_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isSubmitted) "Next Question" else "Submit Answer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
