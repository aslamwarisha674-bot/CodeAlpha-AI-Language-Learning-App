package com.example.data

import kotlinx.coroutines.flow.Flow

class LanguageRepository(private val languageDao: LanguageDao) {

    fun getWordsByLanguage(language: String): Flow<List<LanguageWord>> =
        languageDao.getWordsByLanguage(language)

    fun getWordsByCategory(language: String, category: String): Flow<List<LanguageWord>> =
        languageDao.getWordsByCategory(language, category)

    suspend fun insertWord(word: LanguageWord): Long =
        languageDao.insertWord(word)

    suspend fun insertWords(words: List<LanguageWord>) =
        languageDao.insertWords(words)

    suspend fun updateWord(word: LanguageWord) =
        languageDao.updateWord(word)

    suspend fun deleteWord(word: LanguageWord) =
        languageDao.deleteWord(word)

    suspend fun clearCustomWords(language: String) =
        languageDao.clearCustomWords(language)

    // Quiz result methods
    fun getAllQuizResults(): Flow<List<QuizResult>> =
        languageDao.getAllQuizResults()

    fun getQuizResultsByLanguage(language: String): Flow<List<QuizResult>> =
        languageDao.getQuizResultsByLanguage(language)

    suspend fun insertQuizResult(result: QuizResult): Long =
        languageDao.insertQuizResult(result)

    suspend fun checkAndPrepopulate() {
        val esCount = languageDao.getWordCount("es")
        if (esCount == 0) {
            val words = mutableListOf<LanguageWord>()
            
            // --- SPANISH ---
            words.add(LanguageWord(
                language = "es", category = "Vocabulary", word = "Hola", translation = "Hello",
                pronunciation = "OH-lah", exampleSentence = "Hola, ¿cómo estás?", exampleTranslation = "Hello, how are you?"
            ))
            words.add(LanguageWord(
                language = "es", category = "Vocabulary", word = "Gracias", translation = "Thank you",
                pronunciation = "GRAH-syahs", exampleSentence = "Muchas gracias por la comida.", exampleTranslation = "Thank you very much for the food."
            ))
            words.add(LanguageWord(
                language = "es", category = "Phrases", word = "Buenos días", translation = "Good morning",
                pronunciation = "BWEH-nos DEE-ahs", exampleSentence = "Buenos días, mi amigo.", exampleTranslation = "Good morning, my friend."
            ))
            words.add(LanguageWord(
                language = "es", category = "Phrases", word = "¿Dónde está el baño?", translation = "Where is the bathroom?",
                pronunciation = "DON-deh ehs-TAH ehl BAH-nyo", exampleSentence = "Disculpe, ¿dónde está el baño?", exampleTranslation = "Excuse me, where is the bathroom?"
            ))
            words.add(LanguageWord(
                language = "es", category = "Grammar", word = "Yo soy", translation = "I am",
                pronunciation = "yo soy", exampleSentence = "Yo soy un estudiante de español.", exampleTranslation = "I am a Spanish student."
            ))
            words.add(LanguageWord(
                language = "es", category = "Grammar", word = "Tener", translation = "To have",
                pronunciation = "teh-NEHR", exampleSentence = "Tengo una pregunta.", exampleTranslation = "I have a question."
            ))

            // --- FRENCH ---
            words.add(LanguageWord(
                language = "fr", category = "Vocabulary", word = "Bonjour", translation = "Hello",
                pronunciation = "bohn-ZHOOR", exampleSentence = "Bonjour! Comment ça va?", exampleTranslation = "Hello! How is it going?"
            ))
            words.add(LanguageWord(
                language = "fr", category = "Vocabulary", word = "Merci", translation = "Thank you",
                pronunciation = "mair-SEE", exampleSentence = "Merci beaucoup pour l'aide.", exampleTranslation = "Thank you very much for the help."
            ))
            words.add(LanguageWord(
                language = "fr", category = "Phrases", word = "S'il vous plaît", translation = "Please",
                pronunciation = "seel voo PLEH", exampleSentence = "Un café, s'il vous plaît.", exampleTranslation = "A coffee, please."
            ))
            words.add(LanguageWord(
                language = "fr", category = "Phrases", word = "Où est la gare?", translation = "Where is the train station?",
                pronunciation = "oo eh lah gahr", exampleSentence = "Excusez-moi, où est la gare?", exampleTranslation = "Excuse me, where is the train station?"
            ))
            words.add(LanguageWord(
                language = "fr", category = "Grammar", word = "Je suis", translation = "I am",
                pronunciation = "zhuh swee", exampleSentence = "Je suis fatigué aujourd'hui.", exampleTranslation = "I am tired today."
            ))
            words.add(LanguageWord(
                language = "fr", category = "Grammar", word = "Avoir", translation = "To have",
                pronunciation = "ah-VWAR", exampleSentence = "J'ai un livre intéressant.", exampleTranslation = "I have an interesting book."
            ))

            // --- JAPANESE ---
            words.add(LanguageWord(
                language = "ja", category = "Vocabulary", word = "こんにちは", translation = "Hello",
                pronunciation = "Konnichiwa", exampleSentence = "こんにちは、お元気ですか？", exampleTranslation = "Hello, how are you?"
            ))
            words.add(LanguageWord(
                language = "ja", category = "Vocabulary", word = "ありがとう", translation = "Thank you",
                pronunciation = "Arigatou", exampleSentence = "手伝ってくれてありがとう。", exampleTranslation = "Thank you for helping me."
            ))
            words.add(LanguageWord(
                language = "ja", category = "Phrases", word = "すみません", translation = "Excuse me / Sorry",
                pronunciation = "Sumimasen", exampleSentence = "すみません、駅はどこですか？", exampleTranslation = "Excuse me, where is the station?"
            ))
            words.add(LanguageWord(
                language = "ja", category = "Phrases", word = "お会計をお願いします", translation = "The bill, please",
                pronunciation = "O-kaikei o onegai shimasu", exampleSentence = "すみません、お会計をお願いします。", exampleTranslation = "Excuse me, the bill please."
            ))
            words.add(LanguageWord(
                language = "ja", category = "Grammar", word = "〜は〜です", translation = "X is Y",
                pronunciation = "wa desu", exampleSentence = "私は学生です。", exampleTranslation = "I am a student."
            ))
            words.add(LanguageWord(
                language = "ja", category = "Grammar", word = "あります", translation = "To exist / Have (inanimate)",
                pronunciation = "arimasu", exampleSentence = "質問があります。", exampleTranslation = "I have a question."
            ))

            // --- GERMAN ---
            words.add(LanguageWord(
                language = "de", category = "Vocabulary", word = "Hallo", translation = "Hello",
                pronunciation = "HAH-loh", exampleSentence = "Hallo! Wie geht es dir?", exampleTranslation = "Hello! How are you?"
            ))
            words.add(LanguageWord(
                language = "de", category = "Vocabulary", word = "Danke", translation = "Thank you",
                pronunciation = "DAHN-kuh", exampleSentence = "Vielen Dank für das Geschenk.", exampleTranslation = "Thank you very much for the gift."
            ))
            words.add(LanguageWord(
                language = "de", category = "Phrases", word = "Guten Morgen", translation = "Good morning",
                pronunciation = "GOO-ten MOR-gen", exampleSentence = "Guten Morgen, mein Freund.", exampleTranslation = "Good morning, my friend."
            ))
            words.add(LanguageWord(
                language = "de", category = "Phrases", word = "Wo ist die Toilette?", translation = "Where is the bathroom?",
                pronunciation = "voh ist dee twah-LET-uh", exampleSentence = "Entschuldigung, wo ist die Toilette?", exampleTranslation = "Excuse me, where is the bathroom?"
            ))
            words.add(LanguageWord(
                language = "de", category = "Grammar", word = "Ich bin", translation = "I am",
                pronunciation = "ikh bin", exampleSentence = "Ich bin glücklich heute.", exampleTranslation = "Ich bin happy today."
            ))
            words.add(LanguageWord(
                language = "de", category = "Grammar", word = "Haben", translation = "To have",
                pronunciation = "HAH-ben", exampleSentence = "Ich habe eine Frage.", exampleTranslation = "I have a question."
            ))

            languageDao.insertWords(words)
        }
    }
}
