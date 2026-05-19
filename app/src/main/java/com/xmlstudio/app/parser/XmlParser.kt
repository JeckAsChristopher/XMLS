package com.xmlstudio.app.parser

import com.xmlstudio.app.models.ParseResult
import com.xmlstudio.app.models.XmlNode
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class XmlParser {

    fun parse(xmlContent: String): ParseResult {
        if (xmlContent.isBlank()) {
            return ParseResult.Failure("XML content is empty.")
        }
        return try {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = false
            }
            val parser = factory.newPullParser().apply {
                setInput(StringReader(xmlContent))
            }
            val root = parseRoot(parser)
                ?: return ParseResult.Failure("No root element found.")
            ParseResult.Success(root)
        } catch (e: Exception) {
            ParseResult.Failure("Failed to parse XML: ${e.message}", e)
        }
    }

    /**
     * Advances through the document until it finds the first START_TAG,
     * builds that node with all its children, and returns it.
     */
    private fun parseRoot(parser: XmlPullParser): XmlNode? {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                return buildNode(parser)
            }
            eventType = parser.next()
        }
        return null
    }

    /**
     * Called when the parser is positioned on a START_TAG.
     * Reads all children and text content, stops at the matching END_TAG.
     */
    private fun buildNode(parser: XmlPullParser): XmlNode {
        val tag = parser.name ?: "Unknown"
        val attributes = mutableMapOf<String, String>()
        for (i in 0 until parser.attributeCount) {
            attributes[parser.getAttributeName(i)] = parser.getAttributeValue(i)
        }

        val node = XmlNode(tag = tag, attributes = attributes)
        val textBuilder = StringBuilder()

        var eventType = parser.next()
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    node.children.add(buildNode(parser))
                }
                XmlPullParser.TEXT -> {
                    val chunk = parser.text?.trim()
                    if (!chunk.isNullOrEmpty()) textBuilder.append(chunk)
                }
                XmlPullParser.END_TAG -> {
                    val captured = textBuilder.toString().trim()
                    if (captured.isNotEmpty()) node.text = captured
                    return node
                }
            }
            eventType = parser.next()
        }
        return node
    }
}
