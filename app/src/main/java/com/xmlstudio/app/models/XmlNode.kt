package com.xmlstudio.app.models

data class XmlNode(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: MutableList<XmlNode> = mutableListOf(),
    val text: String? = null
)

sealed class ParseResult {
    data class Success(val root: XmlNode) : ParseResult()
    data class Failure(val message: String, val cause: Throwable? = null) : ParseResult()
}
