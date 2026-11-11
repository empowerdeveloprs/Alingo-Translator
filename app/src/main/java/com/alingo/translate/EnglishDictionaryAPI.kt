package com.alingo.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class EnglishWordInfo(
    val phonetic: String?,
    val audioUrl: String?
)

object EnglishDictionaryAPI {
    
    suspend fun fetchWordInfo(word: String): EnglishWordInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$word")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)
                if (jsonArray.length() > 0) {
                    val firstEntry = jsonArray.getJSONObject(0)
                    
                    var phonetic = ""
                    if (firstEntry.has("phonetic")) {
                        phonetic = firstEntry.getString("phonetic")
                    }

                    var audioUrl: String? = null
                    if (firstEntry.has("phonetics")) {
                        val phoneticsArray = firstEntry.getJSONArray("phonetics")
                        for (i in 0 until phoneticsArray.length()) {
                            val ph = phoneticsArray.getJSONObject(i)
                            if (ph.has("text") && phonetic.isEmpty()) {
                                phonetic = ph.getString("text")
                            }
                            if (ph.has("audio") && ph.getString("audio").isNotEmpty()) {
                                audioUrl = ph.getString("audio")
                                break
                            }
                        }
                    }
                    return@withContext EnglishWordInfo(phonetic.ifBlank { null }, audioUrl)
                }
            }
            return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
