package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {
    @Query("SELECT * FROM words WHERE language = :language ORDER BY id ASC")
    fun getWordsByLanguage(language: String): Flow<List<LanguageWord>>

    @Query("SELECT * FROM words WHERE language = :language AND category = :category ORDER BY id ASC")
    fun getWordsByCategory(language: String, category: String): Flow<List<LanguageWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: LanguageWord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<LanguageWord>)

    @Update
    suspend fun updateWord(word: LanguageWord)

    @Delete
    suspend fun deleteWord(word: LanguageWord)

    @Query("DELETE FROM words WHERE language = :language AND isCustom = 1")
    suspend fun clearCustomWords(language: String)

    @Query("SELECT COUNT(*) FROM words WHERE language = :language")
    suspend fun getWordCount(language: String): Int

    // Quiz result methods
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results WHERE language = :language ORDER BY timestamp DESC")
    fun getQuizResultsByLanguage(language: String): Flow<List<QuizResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResult): Long
}
