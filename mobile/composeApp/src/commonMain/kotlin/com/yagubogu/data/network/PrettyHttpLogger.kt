package com.yagubogu.data.network

import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal object PrettyHttpLogger : KtorLogger {
    private val logger = Logger.withTag("KtorLogger")

    private val prettyJson = Json { prettyPrint = true }

    override fun log(message: String) {
        val lines = message.lines()
        val firstLine = lines.firstOrNull() ?: return

        val prefix =
            when {
                firstLine.startsWith("REQUEST") -> "→"
                firstLine.startsWith("RESPONSE") -> "←"
                else -> "·"
            }

        val formatted =
            buildString {
                appendLine(DIVIDER)
                appendLine("$prefix $firstLine")
                lines.drop(1).forEach { line ->
                    if (line.isNotBlank()) appendLine(prettyFormatLine(line))
                }
                append(DIVIDER)
            }

        logger.d { formatted }
    }

    private fun prettyFormatLine(line: String): String {
        val trimmed = line.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            val pretty = tryPrettyJson(trimmed)
            if (pretty != null) {
                return pretty.lines().joinToString("\n") { "  $it" }
            }
        }
        return "  $line"
    }

    private fun tryPrettyJson(raw: String): String? =
        try {
            val element = prettyJson.parseToJsonElement(raw)
            prettyJson.encodeToString(JsonElement.serializer(), element)
        } catch (_: Exception) {
            null
        }
}

private const val DIVIDER = "────────────────────────────────────────"
