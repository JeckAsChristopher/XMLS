package com.xmlstudio.app.renderer

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.xmlstudio.app.models.XmlNode

class ComponentFactory(private val context: Context) {

    fun create(node: XmlNode): View? {
        return when (node.tag.lowercase()) {
            "linearlayout" -> createLinearLayout(node)
            "relativelayout" -> createRelativeLayout(node)
            "framelayout" -> createFrameLayout(node)
            "scrollview" -> createScrollView(node)
            "horizontalscrollview" -> createHorizontalScrollView(node)
            "textview" -> createTextView(node)
            "button" -> createButton(node)
            "edittext" -> createEditText(node)
            "imageview" -> createImageView(node)
            "checkbox" -> createCheckBox(node)
            "radiobutton" -> createRadioButton(node)
            "switch" -> createSwitch(node)
            "progressbar" -> createProgressBar(node)
            "view" -> createDivider(node)
            else -> createUnknownView(node)
        }
    }

    private fun applyCommonAttributes(view: View, node: XmlNode) {
        val attrs = node.attributes

        attrs["android:visibility"]?.let { vis ->
            view.visibility = when (vis) {
                "gone" -> View.GONE
                "invisible" -> View.INVISIBLE
                else -> View.VISIBLE
            }
        }

        attrs["android:background"]?.let { bg ->
            if (bg.startsWith("#")) {
                view.setBackgroundColor(AttributeParser.parseColor(bg))
            }
        }

        attrs["android:alpha"]?.let { alpha ->
            view.alpha = alpha.toFloatOrNull() ?: 1f
        }

        val paddingAll = attrs["android:padding"]?.let { AttributeParser.parseDimension(context, it) } ?: 0
        val paddingLeft   = attrs["android:paddingLeft"]?.let   { AttributeParser.parseDimension(context, it) } ?: paddingAll
        val paddingTop    = attrs["android:paddingTop"]?.let    { AttributeParser.parseDimension(context, it) } ?: paddingAll
        val paddingRight  = attrs["android:paddingRight"]?.let  { AttributeParser.parseDimension(context, it) } ?: paddingAll
        val paddingBottom = attrs["android:paddingBottom"]?.let { AttributeParser.parseDimension(context, it) } ?: paddingAll
        val paddingStart  = attrs["android:paddingStart"]?.let  { AttributeParser.parseDimension(context, it) } ?: paddingLeft
        val paddingEnd    = attrs["android:paddingEnd"]?.let    { AttributeParser.parseDimension(context, it) } ?: paddingRight

        view.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
    }

    private fun buildLayoutParams(node: XmlNode): ViewGroup.MarginLayoutParams {
        val attrs = node.attributes
        val width = AttributeParser.parseLayoutSize(
            attrs["android:layout_width"] ?: attrs["layout_width"] ?: "wrap_content"
        )
        val height = AttributeParser.parseLayoutSize(
            attrs["android:layout_height"] ?: attrs["layout_height"] ?: "wrap_content"
        )
        val lp = ViewGroup.MarginLayoutParams(width, height)

        val marginAll = attrs["android:layout_margin"]?.let { AttributeParser.parseDimension(context, it) } ?: 0
        lp.leftMargin   = attrs["android:layout_marginLeft"]?.let   { AttributeParser.parseDimension(context, it) } ?: marginAll
        lp.topMargin    = attrs["android:layout_marginTop"]?.let    { AttributeParser.parseDimension(context, it) } ?: marginAll
        lp.rightMargin  = attrs["android:layout_marginRight"]?.let  { AttributeParser.parseDimension(context, it) } ?: marginAll
        lp.bottomMargin = attrs["android:layout_marginBottom"]?.let { AttributeParser.parseDimension(context, it) } ?: marginAll

        return lp
    }

    private fun createLinearLayout(node: XmlNode): LinearLayout {
        val layout = LinearLayout(context)
        val attrs = node.attributes
        layout.orientation = if (attrs["android:orientation"]?.lowercase() == "horizontal")
            LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

        attrs["android:gravity"]?.let { layout.gravity = AttributeParser.parseGravity(it) }
        applyCommonAttributes(layout, node)
        layout.layoutParams = buildLayoutParams(node)

        node.children.forEach { child ->
            create(child)?.let { childView ->
                val childLp = LinearLayout.LayoutParams(buildLayoutParams(child))
                child.attributes["android:layout_weight"]?.toFloatOrNull()?.let { weight ->
                    childLp.weight = weight
                }
                childView.layoutParams = childLp
                layout.addView(childView)
            }
        }
        return layout
    }

    private fun createRelativeLayout(node: XmlNode): RelativeLayout {
        val layout = RelativeLayout(context)
        applyCommonAttributes(layout, node)
        layout.layoutParams = buildLayoutParams(node)
        node.children.forEach { child ->
            create(child)?.let { layout.addView(it, buildLayoutParams(child)) }
        }
        return layout
    }

    private fun createFrameLayout(node: XmlNode): FrameLayout {
        val layout = FrameLayout(context)
        applyCommonAttributes(layout, node)
        layout.layoutParams = buildLayoutParams(node)
        node.children.forEach { child ->
            create(child)?.let { layout.addView(it, buildLayoutParams(child)) }
        }
        return layout
    }

    private fun createScrollView(node: XmlNode): ScrollView {
        val scrollView = ScrollView(context)
        applyCommonAttributes(scrollView, node)
        scrollView.layoutParams = buildLayoutParams(node)
        scrollView.isFillViewport = node.attributes["android:fillViewport"] == "true"

        val inner = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        node.children.forEach { child ->
            create(child)?.let { inner.addView(it, buildLayoutParams(child)) }
        }
        scrollView.addView(inner)
        return scrollView
    }

    private fun createHorizontalScrollView(node: XmlNode): HorizontalScrollView {
        val scrollView = HorizontalScrollView(context)
        applyCommonAttributes(scrollView, node)
        scrollView.layoutParams = buildLayoutParams(node)

        val inner = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        node.children.forEach { child ->
            create(child)?.let { inner.addView(it, buildLayoutParams(child)) }
        }
        scrollView.addView(inner)
        return scrollView
    }

    private fun createTextView(node: XmlNode): TextView {
        val attrs = node.attributes
        val tv = TextView(context)
        tv.text = attrs["android:text"] ?: attrs["text"] ?: node.text ?: ""
        tv.textSize = AttributeParser.parseTextSize(context, attrs["android:textSize"] ?: attrs["textSize"] ?: "14sp")

        attrs["android:textColor"]?.let { tv.setTextColor(AttributeParser.parseColor(it)) }
        attrs["android:gravity"]?.let { tv.gravity = AttributeParser.parseGravity(it) }
        attrs["android:textStyle"]?.let { style ->
            tv.setTypeface(tv.typeface, when (style) {
                "bold" -> Typeface.BOLD
                "italic" -> Typeface.ITALIC
                "bold|italic", "italic|bold" -> Typeface.BOLD_ITALIC
                else -> Typeface.NORMAL
            })
        }
        attrs["android:maxLines"]?.toIntOrNull()?.let { tv.maxLines = it }
        attrs["android:lines"]?.toIntOrNull()?.let { tv.setLines(it) }
        attrs["android:hint"]?.let { tv.hint = it }
        attrs["android:textAllCaps"]?.let { tv.isAllCaps = it == "true" }

        applyCommonAttributes(tv, node)
        tv.layoutParams = buildLayoutParams(node)
        return tv
    }

    private fun createButton(node: XmlNode): Button {
        val attrs = node.attributes
        val btn = Button(context)
        btn.text = attrs["android:text"] ?: attrs["text"] ?: node.text ?: "Button"
        btn.textSize = AttributeParser.parseTextSize(context, attrs["android:textSize"] ?: "14sp")
        attrs["android:textColor"]?.let { btn.setTextColor(AttributeParser.parseColor(it)) }

        applyCommonAttributes(btn, node)
        btn.layoutParams = buildLayoutParams(node)
        return btn
    }

    private fun createEditText(node: XmlNode): EditText {
        val attrs = node.attributes
        val et = EditText(context)
        et.hint = attrs["android:hint"] ?: attrs["hint"] ?: ""
        et.setText(attrs["android:text"] ?: attrs["text"] ?: node.text ?: "")
        et.textSize = AttributeParser.parseTextSize(context, attrs["android:textSize"] ?: "14sp")
        attrs["android:textColor"]?.let { et.setTextColor(AttributeParser.parseColor(it)) }
        attrs["android:maxLines"]?.toIntOrNull()?.let { et.maxLines = it }

        applyCommonAttributes(et, node)
        et.layoutParams = buildLayoutParams(node)
        return et
    }

    private fun createImageView(node: XmlNode): ImageView {
        val iv = ImageView(context)
        iv.setImageResource(android.R.drawable.ic_menu_gallery)
        node.attributes["android:scaleType"]?.let { scaleType ->
            iv.scaleType = when (scaleType.lowercase()) {
                "center" -> ImageView.ScaleType.CENTER
                "center_crop", "centercrop" -> ImageView.ScaleType.CENTER_CROP
                "center_inside", "centerinside" -> ImageView.ScaleType.CENTER_INSIDE
                "fit_center", "fitcenter" -> ImageView.ScaleType.FIT_CENTER
                "fit_xy", "fitxy" -> ImageView.ScaleType.FIT_XY
                "matrix" -> ImageView.ScaleType.MATRIX
                else -> ImageView.ScaleType.FIT_CENTER
            }
        }
        applyCommonAttributes(iv, node)
        iv.layoutParams = buildLayoutParams(node)
        return iv
    }

    private fun createCheckBox(node: XmlNode): CheckBox {
        val cb = CheckBox(context)
        cb.text = node.attributes["android:text"] ?: node.text ?: ""
        cb.isChecked = node.attributes["android:checked"] == "true"
        applyCommonAttributes(cb, node)
        cb.layoutParams = buildLayoutParams(node)
        return cb
    }

    private fun createRadioButton(node: XmlNode): RadioButton {
        val rb = RadioButton(context)
        rb.text = node.attributes["android:text"] ?: node.text ?: ""
        rb.isChecked = node.attributes["android:checked"] == "true"
        applyCommonAttributes(rb, node)
        rb.layoutParams = buildLayoutParams(node)
        return rb
    }

    @Suppress("DEPRECATION")
    private fun createSwitch(node: XmlNode): Switch {
        val sw = Switch(context)
        sw.text = node.attributes["android:text"] ?: node.text ?: ""
        sw.isChecked = node.attributes["android:checked"] == "true"
        applyCommonAttributes(sw, node)
        sw.layoutParams = buildLayoutParams(node)
        return sw
    }

    private fun createProgressBar(node: XmlNode): ProgressBar {
        val pb = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        pb.max = node.attributes["android:max"]?.toIntOrNull() ?: 100
        pb.progress = node.attributes["android:progress"]?.toIntOrNull() ?: 0
        applyCommonAttributes(pb, node)
        pb.layoutParams = buildLayoutParams(node)
        return pb
    }

    private fun createDivider(node: XmlNode): View {
        val divider = View(context)
        applyCommonAttributes(divider, node)
        divider.layoutParams = buildLayoutParams(node)
        return divider
    }

    private fun createUnknownView(node: XmlNode): TextView {
        val tv = TextView(context)
        tv.text = "[${node.tag}]"
        tv.alpha = 0.5f
        tv.setPadding(8, 4, 8, 4)
        tv.layoutParams = buildLayoutParams(node)
        return tv
    }
}
