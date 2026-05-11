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
            val root = parseRecursive(parser)
                ?: return ParseResult.Failure("No root element found.")
            ParseResult.Success(root)
        } catch (e: Exception) {
            ParseResult.Failure("Failed to parse XML: ${e.message}", e)
        }
    }

    private fun parseRecursive(parser: XmlPullParser): XmlNode? {
        var eventType = parser.eventType
        var currentNode: XmlNode? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name ?: "Unknown"
                    val attributes = mutableMapOf<String, String>()
                    for (i in 0 until parser.attributeCount) {
                        val name = parser.getAttributeName(i)
                        val value = parser.getAttributeValue(i)
                        attributes[name] = value
                    }
                    val node = XmlNode(tag = tag, attributes = attributes)
                    if (currentNode == null) {
                        currentNode = node
                        parseChildren(parser, currentNode)
                        return currentNode
                    }
                }
                XmlPullParser.END_TAG -> return currentNode
            }
            eventType = parser.next()
        }
        return currentNode
    }

    private fun parseChildren(parser: XmlPullParser, parent: XmlNode) {
        var eventType = parser.next()
        val textBuilder = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name ?: "Unknown"
                    val attributes = mutableMapOf<String, String>()
                    for (i in 0 until parser.attributeCount) {
                        val name = parser.getAttributeName(i)
                        val value = parser.getAttributeValue(i)
                        attributes[name] = value
                    }
                    val childNode = XmlNode(tag = tag, attributes = attributes)
                    parseChildren(parser, childNode)
                    parent.children.add(childNode)
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim()
                    if (!text.isNullOrEmpty()) {
                        textBuilder.append(text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    val capturedText = textBuilder.toString().trim()
                    if (capturedText.isNotEmpty()) {
                        val nodeWithText = parent.copy(text = capturedText)
                        parent.attributes.toMutableMap().also { map ->
                            map.putAll(nodeWithText.attributes)
                        }
                    }
                    return
                }
            }
            eventType = parser.next()
        }
    }
}
