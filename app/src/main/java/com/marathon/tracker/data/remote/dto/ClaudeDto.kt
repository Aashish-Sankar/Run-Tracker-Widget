package com.marathon.tracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClaudeRequest(
    @SerializedName("model") val model: String = "claude-sonnet-4-20250514",
    @SerializedName("max_tokens") val maxTokens: Int = 2048,
    @SerializedName("system") val system: String,
    @SerializedName("messages") val messages: List<ClaudeMessage>,
)

data class ClaudeMessage(
    @SerializedName("role") val role: String = "user",
    @SerializedName("content") val content: String,
)

data class ClaudeResponse(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: List<ClaudeContentBlock>,
    @SerializedName("usage") val usage: ClaudeUsage,
)

data class ClaudeContentBlock(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String,
)

data class ClaudeUsage(
    @SerializedName("input_tokens") val inputTokens: Int,
    @SerializedName("output_tokens") val outputTokens: Int,
)
