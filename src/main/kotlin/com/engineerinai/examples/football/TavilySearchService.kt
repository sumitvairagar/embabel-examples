package com.engineerinai.examples.football

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

data class TavilyResult(
    val title: String,
    val url: String,
    val content: String,
)

@Service
class TavilySearchService(
    @Value("\${tavily.api-key}") private val apiKey: String,
) {

    private val client = RestClient.create("https://api.tavily.com")

    fun search(query: String, maxResults: Int = 5): List<TavilyResult> {
        val response = client.post()
            .uri("/search")
            .header("Content-Type", "application/json")
            .body(mapOf(
                "api_key" to apiKey,
                "query" to query,
                "max_results" to maxResults,
                "search_depth" to "advanced",
                "topic" to "news",
                "days" to 7,
            ))
            .retrieve()
            .body(Map::class.java) ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val results = response["results"] as? List<Map<String, Any>> ?: return emptyList()
        return results.map {
            TavilyResult(
                title = it["title"] as? String ?: "",
                url = it["url"] as? String ?: "",
                content = it["content"] as? String ?: "",
            )
        }
    }
}
