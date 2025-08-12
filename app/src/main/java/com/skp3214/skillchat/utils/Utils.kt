package com.skp3214.skillchat.utils

fun String.parseCodeBlock(): Pair<String, String>? {
    val regex = "```(\\w+)?\\n([\\s\\S]*?)\\n```".toRegex()
    val match = regex.find(this)
    return if (match != null && match.groupValues.size >= 3) {
        val language = match.groupValues[1].ifEmpty { "code" }
        val code = match.groupValues[2].trim()
        language to code
    } else {
        val simpleCodeRegex = "```([\\s\\S]*)```".toRegex()
        val simpleMatch = simpleCodeRegex.find(this.trim())
        if (simpleMatch != null && simpleMatch.groupValues.size == 2) {
            "code" to simpleMatch.groupValues[1].trim()
        } else {
            null
        }
    }
}