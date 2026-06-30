package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// The target model response items
@JsonClass(generateAdapter = true)
data class GeneratedWord(
    val word: String,
    val translation: String,
    val pronunciation: String,
    @Json(name = "exampleSentence") val exampleSentence: String,
    @Json(name = "exampleTranslation") val exampleTranslation: String
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Generates a list of custom words/lessons for a language and topic.
     * Returns an empty list on failure.
     */
    suspend fun generateLessons(languageName: String, topic: String): List<GeneratedWord> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return emptyList()
        }

        val prompt = """
            Generate exactly 3 vocabulary words or useful phrases to learn $languageName on the topic: "$topic".
            Provide accurate translation, helpful pronunciation guide (English phonetic spelling), and an example sentence in $languageName with its English translation.
            
            Return JSON matching this schema:
            [
              {
                "word": "foreign word or phrase",
                "translation": "english translation",
                "pronunciation": "english-friendly phonetic pronunciation guide",
                "exampleSentence": "complete example sentence using the word",
                "exampleTranslation": "english translation of the example sentence"
              }
            ]
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a professional language tutor. You generate highly accurate words, transliterations, and translation sentences. Output ONLY valid JSON array and nothing else.")))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return emptyList()
            
            val type = Types.newParameterizedType(List::class.java, GeneratedWord::class.java)
            val adapter = moshi.adapter<List<GeneratedWord>>(type)
            adapter.fromJson(jsonText) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Generates an explanation / mnemonic for a word.
     */
    suspend fun explainWord(languageName: String, word: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Please configure your Gemini API key in the Secrets Panel to activate the AI Tutor features."
        }

        val prompt = """
            Create an expert, friendly language tutorial explanation for learning the $languageName word/phrase: "$word".
            Provide:
            1. Origin / Literal Meaning: A 1-sentence breakdown.
            2. Mnemonic Trick: An elegant, memorable trick, word association, or visualization technique to remember this word easily.
            3. Cultural context / Usage tip: A quick, polite usage tip.
            
            Make it look super clean, professional, formatted with bold highlights, bullet points, and extremely engaging. Avoid any greeting or filler text.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a professional language tutor from an elite school. You explain words with beautiful formatting, bullet points, and highly memorable phonetic mnemonics.")))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No explanation generated."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error generating explanation: ${e.localizedMessage}"
        }
    }
}
