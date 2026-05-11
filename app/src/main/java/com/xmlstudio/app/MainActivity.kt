package com.xmlstudio.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.xmlstudio.app.databinding.ActivityMainBinding
import com.xmlstudio.app.editor.EditorActivity
import com.xmlstudio.app.export.ExportActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        animateEntrance()
    }

    private fun setupClickListeners() {
        binding.btnExportXml.setOnClickListener {
            val intent = Intent(this, ExportActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        binding.btnCreateXml.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun animateEntrance() {
        val views = listOf(
            binding.tvAppTitle,
            binding.tvAppSubtitle,
            binding.divider,
            binding.btnExportXml,
            binding.btnCreateXml,
            binding.tvVersion
        )

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(index * 80L)
                .start()
        }
    }
}
