package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val language: String,
    val score: Int,                  // Number of correct answers
    val totalQuestions: Int,         // Total number of questions
    val timestamp: Long = System.currentTimeMillis()
)
