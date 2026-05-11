package com.xmlstudio.app.export

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xmlstudio.app.databinding.ActivityExportBinding

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private val viewModel: ExportViewModel by viewModels()

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.loadXmlFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(com.xmlstudio.app.R.string.export_xml)
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ExportState.Idle -> showIdle()
                is ExportState.Loading -> showLoading()
                is ExportState.Rendered -> showRendered(state)
                is ExportState.Error -> showError(state.message)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPickFile.setOnClickListener {
            filePicker.launch("text/xml")
        }

        binding.btnPickFileAlt.setOnClickListener {
            filePicker.launch("*/*")
        }

        binding.btnReset.setOnClickListener {
            viewModel.reset()
        }
    }

    private fun showIdle() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.scrollViewResult.visibility = View.GONE
        binding.btnReset.visibility = View.GONE
    }

    private fun showLoading() {
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.scrollViewResult.visibility = View.GONE
        binding.btnReset.visibility = View.GONE
    }

    private fun showRendered(state: ExportState.Rendered) {
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.scrollViewResult.visibility = View.VISIBLE
        binding.btnReset.visibility = View.VISIBLE

        binding.containerRendered.removeAllViews()
        binding.containerRendered.addView(state.view)

        Toast.makeText(this, "Rendered successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.scrollViewResult.visibility = View.GONE
        binding.btnReset.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }
}
