package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class LanguageWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val language: String,            // e.g., "es" for Spanish, "fr" for French, "ja" for Japanese, "de" for German
    val category: String,            // "Vocabulary", "Phrases", "Grammar", "Greetings"
    val word: String,                // Target language word/phrase
    val translation: String,         // English translation
    val pronunciation: String,       // Pronunciation guides/Romanization
    val exampleSentence: String,     // Example sentence in target language
    val exampleTranslation: String,  // Example sentence translation in English
    val isCustom: Boolean = false,   // Whether created by user/AI tutor dynamically
    val isMastered: Boolean = false  // Track vocabulary mastery
)
