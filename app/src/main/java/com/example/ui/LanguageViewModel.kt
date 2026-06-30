package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.RetrofitClient
import com.example.data.AppDatabase
import com.example.data.LanguageRepository
import com.example.data.LanguageWord
import com.example.data.QuizResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class AppScreen {
    HOME,
    CATEGORY_DETAIL,
    FLASHCARD_STUDY,
    QUIZ_STUDY,
    AI_TUTOR,
    HISTORY_STATS
}

data class QuizQuestion(
    val word: LanguageWord,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int
)

class LanguageViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = LanguageRepository(AppDatabase.getDatabase(application).languageDao())
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // App Navigation & Selected Context
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("es") // "es", "fr", "ja", "de"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Vocabulary") // "Vocabulary", "Phrases", "Grammar"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Loaded Database Vocabulary
    val allWords: StateFlow<List<LanguageWord>> = _selectedLanguage
        .flatMapLatest { lang -> repository.getWordsByLanguage(lang) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredWords: StateFlow<List<LanguageWord>> = combine(_selectedLanguage, _selectedCategory) { lang, cat ->
        Pair(lang, cat)
    }.flatMapLatest { (lang, cat) ->
        repository.getWordsByCategory(lang, cat)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quiz History Flow
    val quizResults: StateFlow<List<QuizResult>> = _selectedLanguage
        .flatMapLatest { lang -> repository.getQuizResultsByLanguage(lang) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizResults: StateFlow<List<QuizResult>> = repository.getAllQuizResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Study State (Flashcards)
    private val _flashcards = MutableStateFlow<List<LanguageWord>>(emptyList())
    val flashcards: StateFlow<List<LanguageWord>> = _flashcards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // Active Study State (Quiz)
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _quizCorrectCount = MutableStateFlow(0)
    val quizCorrectCount: StateFlow<Int> = _quizCorrectCount.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted: StateFlow<Boolean> = _isQuizCompleted.asStateFlow()

    // AI Tutor state
    private val _aiTopicInput = MutableStateFlow("")
    val aiTopicInput: StateFlow<String> = _aiTopicInput.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // AI Explanation State
    private val _explainTargetWord = MutableStateFlow<String?>(null)
    val explainTargetWord: StateFlow<String?> = _explainTargetWord.asStateFlow()

    private val _isExplainLoading = MutableStateFlow(false)
    val isExplainLoading: StateFlow<Boolean> = _isExplainLoading.asStateFlow()

    private val _explainText = MutableStateFlow<String?>(null)
    val explainText: StateFlow<String?> = _explainText.asStateFlow()

    init {
        // Ensure database is prepopulated
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndPrepopulate()
        }

        // Initialize Native TTS Engine
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            updateTtsLanguage(_selectedLanguage.value)
        } else {
            Log.e("LanguageViewModel", "TextToSpeech initialization failed")
        }
    }

    // Navigational Controls
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        updateTtsLanguage(langCode)
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // TTS Voice Pronunciation
    fun speakWord(text: String) {
        if (isTtsReady) {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LingoTTS")
            } catch (e: Exception) {
                Log.e("LanguageViewModel", "Speech output error: ${e.message}")
            }
        }
    }

    private fun updateTtsLanguage(langCode: String) {
        if (!isTtsReady) return
        val locale = when (langCode) {
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRANCE
            "ja" -> Locale.JAPAN
            "de" -> Locale.GERMANY
            else -> Locale.US
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("LanguageViewModel", "TTS Language $langCode is not supported on this device.")
        }
    }

    // Mastery persistence update
    fun toggleWordMastered(word: LanguageWord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWord(word.copy(isMastered = !word.isMastered))
        }
    }

    // Delete custom word
    fun deleteCustomWord(word: LanguageWord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWord(word)
        }
    }

    // Clear custom words for language
    fun clearCustomWords() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCustomWords(_selectedLanguage.value)
        }
    }

    // --- Study Flashcard Session Setup ---
    fun startFlashcardStudy() {
        val currentList = filteredWords.value
        if (currentList.isNotEmpty()) {
            _flashcards.value = currentList.shuffled()
            _currentCardIndex.value = 0
            _isCardFlipped.value = false
            navigateTo(AppScreen.FLASHCARD_STUDY)
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard() {
        if (_currentCardIndex.value < _flashcards.value.size - 1) {
            _currentCardIndex.value += 1
            _isCardFlipped.value = false
        } else {
            // End of deck, loop back to details
            navigateTo(AppScreen.CATEGORY_DETAIL)
        }
    }

    fun prevCard() {
        if (_currentCardIndex.value > 0) {
            _currentCardIndex.value -= 1
            _isCardFlipped.value = false
        }
    }

    // --- Quiz Study Session Setup ---
    fun startQuizStudy() {
        val allAvailable = allWords.value
        if (allAvailable.size < 2) return // Need at least two words to build distractors

        viewModelScope.launch(Dispatchers.Default) {
            val shuffledWords = allAvailable.shuffled().take(5) // Max 5 questions per quiz
            val questions = shuffledWords.map { targetWord ->
                // Collect other words as distractors
                val otherWords = allAvailable.filter { it.id != targetWord.id }
                
                // Construct a question text (50% word-to-translation, 50% translation-to-word)
                val isWordToTranslation = (0..1).random() == 0
                val correctOption: String
                val questionText: String
                val options: List<String>

                if (isWordToTranslation) {
                    questionText = "Translate this: \"${targetWord.word}\""
                    correctOption = targetWord.translation
                    val distractors = otherWords.map { it.translation }.distinct()
                        .shuffled().take(3)
                    
                    // Pad if there are not enough distractors
                    val paddedDistractors = (distractors + listOf("None of the above", "No option", "Unknown")).take(3)
                    options = (paddedDistractors + correctOption).shuffled()
                } else {
                    questionText = "Which term means: \"${targetWord.translation}\"?"
                    correctOption = targetWord.word
                    val distractors = otherWords.map { it.word }.distinct()
                        .shuffled().take(3)
                    
                    val paddedDistractors = (distractors + listOf("N/A", "Unknown phrase", "None")).take(3)
                    options = (paddedDistractors + correctOption).shuffled()
                }

                QuizQuestion(
                    word = targetWord,
                    questionText = questionText,
                    options = options,
                    correctIndex = options.indexOf(correctOption)
                )
            }

            _quizQuestions.value = questions
            _currentQuestionIndex.value = 0
            _selectedAnswerIndex.value = null
            _isAnswerSubmitted.value = false
            _quizCorrectCount.value = 0
            _isQuizCompleted.value = false
            
            withContext(Dispatchers.Main) {
                navigateTo(AppScreen.QUIZ_STUDY)
            }
        }
    }

    fun selectQuizAnswer(index: Int) {
        if (_isAnswerSubmitted.value) return
        _selectedAnswerIndex.value = index
    }

    fun submitQuizAnswer() {
        val selected = _selectedAnswerIndex.value ?: return
        if (_isAnswerSubmitted.value) return

        _isAnswerSubmitted.value = true
        val currentQuestion = _quizQuestions.value[_currentQuestionIndex.value]
        if (selected == currentQuestion.correctIndex) {
            _quizCorrectCount.value += 1
        }
    }

    fun nextQuizQuestion() {
        _selectedAnswerIndex.value = null
        _isAnswerSubmitted.value = false

        if (_currentQuestionIndex.value < _quizQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        } else {
            // Save results and mark complete
            _isQuizCompleted.value = true
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertQuizResult(
                    QuizResult(
                        language = _selectedLanguage.value,
                        score = _quizCorrectCount.value,
                        totalQuestions = _quizQuestions.value.size
                    )
                )
            }
        }
    }

    // --- AI Smart Generation ---
    fun updateAiTopic(topic: String) {
        _aiTopicInput.value = topic
    }

    fun generateAiVocabulary() {
        val topic = _aiTopicInput.value.trim()
        if (topic.isEmpty()) return

        val langCode = _selectedLanguage.value
        val languageName = getLanguageName(langCode)

        _isAiGenerating.value = true
        _aiError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = RetrofitClient.generateLessons(languageName, topic)
                if (results.isNotEmpty()) {
                    val dbWords = results.map { gen ->
                        LanguageWord(
                            language = langCode,
                            category = "Vocabulary", // Place under general vocabulary category
                            word = gen.word,
                            translation = gen.translation,
                            pronunciation = gen.pronunciation,
                            exampleSentence = gen.exampleSentence,
                            exampleTranslation = gen.exampleTranslation,
                            isCustom = true
                        )
                    }
                    repository.insertWords(dbWords)
                    
                    withContext(Dispatchers.Main) {
                        _aiTopicInput.value = ""
                        // Automatically update to show Vocabulary and see the new generated items!
                        _selectedCategory.value = "Vocabulary"
                        _currentScreen.value = AppScreen.CATEGORY_DETAIL
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _aiError.value = "Unable to connect or parse response. Please verify your Gemini API key in the Secrets Panel."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _aiError.value = "An unexpected error occurred: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isAiGenerating.value = false
                }
            }
        }
    }

    // --- AI Explanations and Mnemonics ---
    fun requestExplanation(word: String) {
        _explainTargetWord.value = word
        _isExplainLoading.value = true
        _explainText.value = null

        val langCode = _selectedLanguage.value
        val languageName = getLanguageName(langCode)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val explanation = RetrofitClient.explainWord(languageName, word)
                withContext(Dispatchers.Main) {
                    _explainText.value = explanation
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _explainText.value = "Error loading tutor tips: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isExplainLoading.value = false
                }
            }
        }
    }

    fun dismissExplanation() {
        _explainTargetWord.value = null
        _explainText.value = null
        _isExplainLoading.value = false
    }

    // Language Name Helper
    fun getLanguageName(code: String): String = when (code) {
        "es" -> "Spanish"
        "fr" -> "French"
        "ja" -> "Japanese"
        "de" -> "German"
        else -> "English"
    }

    fun getLanguageFlag(code: String): String = when (code) {
        "es" -> "🇪🇸"
        "fr" -> "🇫🇷"
        "ja" -> "🇯🇵"
        "de" -> "🇩🇪"
        else -> "🌐"
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("LanguageViewModel", "TTS Shutdown failed: ${e.message}")
        }
    }
}
