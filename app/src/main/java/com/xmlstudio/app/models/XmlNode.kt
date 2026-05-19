package com.xmlstudio.app.models

data class XmlNode(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: MutableList<XmlNode> = mutableListOf(),
    var text: String? = null  // var so XmlParser can assign text content after parsing children
)

sealed class ParseResult {
    data class Success(val root: XmlNode) : ParseResult()
    data class Failure(val message: String, val cause: Throwable? = null) : ParseResult()
}
