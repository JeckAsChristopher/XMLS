package com.xmlstudio.app.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditorViewModel : ViewModel() {

    private val _xmlContent = MutableLiveData(DEFAULT_XML)
    val xmlContent: LiveData<String> = _xmlContent

    private val _isSaved = MutableLiveData(true)
    val isSaved: LiveData<Boolean> = _isSaved

    private val _fileName = MutableLiveData("untitled.xml")
    val fileName: LiveData<String> = _fileName

    fun updateContent(content: String) {
        _xmlContent.value = content
        _isSaved.value = false
    }

    fun saveContent(content: String) {
        _xmlContent.value = content
        _isSaved.value = true
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    companion object {
        val DEFAULT_XML = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="#FFFFFF">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello, XML Studio!"
        android:textSize="24sp"
        android:textColor="#1A1A1A"
        android:layout_marginBottom="8dp"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Edit this XML and see it rendered live."
        android:textSize="14sp"
        android:textColor="#666666"
        android:layout_marginBottom="24dp"/>

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tap Me"/>

</LinearLayout>""".trimIndent()
    }
}
