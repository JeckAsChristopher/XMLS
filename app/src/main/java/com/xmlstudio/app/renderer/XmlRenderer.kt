package com.xmlstudio.app.renderer

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.xmlstudio.app.models.XmlNode
import com.xmlstudio.app.models.ParseResult
import com.xmlstudio.app.parser.XmlParser

class XmlRenderer(private val context: Context) {

    private val parser = XmlParser()
    private val factory = ComponentFactory(context)

    sealed class RenderResult {
        data class Success(val view: View) : RenderResult()
        data class Failure(val message: String) : RenderResult()
    }

    fun render(xmlContent: String): RenderResult {
        return when (val parseResult = parser.parse(xmlContent)) {
            is ParseResult.Success -> {
                try {
                    val view = factory.create(parseResult.root)
                        ?: return RenderResult.Failure("Could not render root element: ${parseResult.root.tag}")
                    if (view.layoutParams == null) {
                        view.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    RenderResult.Success(view)
                } catch (e: Exception) {
                    RenderResult.Failure("Rendering error: ${e.message}")
                }
            }
            is ParseResult.Failure -> RenderResult.Failure(parseResult.message)
        }
    }

    fun renderFromNode(node: XmlNode): RenderResult {
        return try {
            val view = factory.create(node)
                ?: return RenderResult.Failure("Could not render element: ${node.tag}")
            RenderResult.Success(view)
        } catch (e: Exception) {
            RenderResult.Failure("Rendering error: ${e.message}")
        }
    }
}
