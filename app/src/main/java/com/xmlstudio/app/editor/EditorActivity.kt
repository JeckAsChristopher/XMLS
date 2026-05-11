package com.xmlstudio.app.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.xmlstudio.app.R
import com.xmlstudio.app.databinding.ActivityEditorBinding
import com.xmlstudio.app.export.ExportState
import com.xmlstudio.app.export.ExportViewModel
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.tm4e.core.registry.IThemeSource

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private val viewModel: EditorViewModel by viewModels()
    private val exportViewModel: ExportViewModel by viewModels()
    private var isPreviewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupEditor()
        setupPreviewPanel()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.create_xml)
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupEditor() {
        try {
            FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(assets))

            val themeRegistry = ThemeRegistry.getInstance()
            val darkThemePath = "textmate/darcula.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(darkThemePath),
                        darkThemePath,
                        null
                    ),
                    "darcula"
                )
            )

            ThemeRegistry.getInstance().setTheme("darcula")
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")

            val colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            binding.codeEditor.colorScheme = colorScheme

            val language = TextMateLanguage.create("text.xml", true)
            binding.codeEditor.setEditorLanguage(language)
        } catch (e: Exception) {
            binding.codeEditor.colorScheme = EditorColorScheme()
        }

        binding.codeEditor.apply {
            isWordwrap = false
            isLineNumberEnabled = true
            isHardwareAcceleratedDrawAllowed = true
            textSizePx = resources.displayMetrics.scaledDensity * 13f
        }

        viewModel.xmlContent.value?.let { content ->
            binding.codeEditor.setText(content)
        }

        binding.codeEditor.subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
            val content = binding.codeEditor.text.toString()
            viewModel.updateContent(content)
            if (isPreviewMode) {
                refreshPreview(content)
            }
        }
    }

    private fun setupPreviewPanel() {
        binding.btnTogglePreview.setOnClickListener {
            isPreviewMode = !isPreviewMode
            updatePreviewPanelVisibility()
            if (isPreviewMode) {
                refreshPreview(binding.codeEditor.text.toString())
            }
        }
    }

    private fun updatePreviewPanelVisibility() {
        if (isPreviewMode) {
            binding.previewPanel.visibility = View.VISIBLE
            binding.btnTogglePreview.text = getString(R.string.hide_preview)
        } else {
            binding.previewPanel.visibility = View.GONE
            binding.btnTogglePreview.text = getString(R.string.show_preview)
        }
    }

    private fun refreshPreview(content: String) {
        exportViewModel.parseXmlContent(content)
    }

    private fun setupObservers() {
        exportViewModel.state.observe(this) { state ->
            when (state) {
                is ExportState.Idle -> {}
                is ExportState.Loading -> {
                    binding.previewContainer.removeAllViews()
                    binding.tvPreviewError.visibility = View.GONE
                }
                is ExportState.Rendered -> {
                    binding.previewContainer.removeAllViews()
                    binding.previewContainer.addView(state.view)
                    binding.tvPreviewError.visibility = View.GONE
                }
                is ExportState.Error -> {
                    binding.previewContainer.removeAllViews()
                    binding.tvPreviewError.text = state.message
                    binding.tvPreviewError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_undo -> {
                binding.codeEditor.undo()
                true
            }
            R.id.action_redo -> {
                binding.codeEditor.redo()
                true
            }
            R.id.action_format -> {
                formatXml()
                true
            }
            R.id.action_clear -> {
                showClearConfirm()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun formatXml() {
        val content = binding.codeEditor.text.toString()
        try {
            val formatted = basicXmlFormat(content)
            binding.codeEditor.setText(formatted)
        } catch (e: Exception) {
            // No-op on format failure
        }
    }

    private fun basicXmlFormat(xml: String): String {
        val sb = StringBuilder()
        var indent = 0
        val indentStr = "    "
        var i = 0
        val trimmed = xml.trim()

        while (i < trimmed.length) {
            val ch = trimmed[i]
            if (ch == '<') {
                val end = trimmed.indexOf('>', i)
                if (end == -1) break
                val tag = trimmed.substring(i, end + 1)
                val isClosing = tag.startsWith("</")
                val isSelfClosing = tag.endsWith("/>")
                val isDeclaration = tag.startsWith("<?")

                if (isClosing) indent = maxOf(0, indent - 1)
                sb.append("\n").append(indentStr.repeat(indent)).append(tag.trim())
                if (!isClosing && !isSelfClosing && !isDeclaration) indent++
                i = end + 1
            } else {
                val nextTag = trimmed.indexOf('<', i)
                val text = if (nextTag == -1) trimmed.substring(i) else trimmed.substring(i, nextTag)
                if (text.isNotBlank()) sb.append(text.trim())
                i = if (nextTag == -1) trimmed.length else nextTag
            }
        }
        return sb.toString().trim()
    }

    private fun showClearConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Clear Editor")
            .setMessage("Are you sure you want to clear all content?")
            .setPositiveButton("Clear") { _, _ -> binding.codeEditor.setText("") }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.codeEditor.release()
    }
}
