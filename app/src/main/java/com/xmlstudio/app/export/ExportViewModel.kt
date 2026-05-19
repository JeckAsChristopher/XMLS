package com.xmlstudio.app.export

import android.app.Application
import android.net.Uri
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xmlstudio.app.renderer.XmlRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Rendered(val view: View) : ExportState()
    data class Error(val message: String) : ExportState()
}

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val renderer = XmlRenderer(application)

    private val _state = MutableLiveData<ExportState>(ExportState.Idle)
    val state: LiveData<ExportState> = _state

    fun loadXmlFile(uri: Uri) {
        _state.value = ExportState.Loading
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    } ?: throw IllegalStateException("Cannot read file.")
                }
                parseAndRender(content)
            } catch (e: Exception) {
                _state.value = ExportState.Error("Could not read file: ${e.message}")
            }
        }
    }

    fun parseXmlContent(content: String) {
        _state.value = ExportState.Loading
        viewModelScope.launch {
            parseAndRender(content)
        }
    }

    private suspend fun parseAndRender(content: String) {
        val result = withContext(Dispatchers.Default) {
            renderer.render(content)
        }
        _state.value = when (result) {
            is XmlRenderer.RenderResult.Success -> ExportState.Rendered(result.view)
            is XmlRenderer.RenderResult.Failure -> ExportState.Error(result.message)
        }
    }

    fun reset() {
        _state.value = ExportState.Idle
    }
}
