package com.example.texteditorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var buttonOpen: Button
    private lateinit var buttonSave: Button

    // Хранит URI текущего открытого файла
    private var currentFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Привязка элементов интерфейса
        editText = findViewById(R.id.editText)
        buttonOpen = findViewById(R.id.buttonOpen)
        buttonSave = findViewById(R.id.buttonSave)

        setupButtons()
        requestStoragePermission()
    }

    private fun setupButtons() {
        buttonOpen.setOnClickListener {
            openFile()
        }

        buttonSave.setOnClickListener {
            saveFile()
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Разрешение на чтение получено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_LONG).show()
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            openTextFile(uri)
        }
    }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            saveTextToFile(uri)
            currentFileUri = uri
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain"))
        }
        openDocumentLauncher.launch(intent)
    }

    private fun openTextFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val text = inputStream.bufferedReader().use { it.readText() }
                editText.setText(text)
                currentFileUri = uri
                Toast.makeText(this, "Файл открыт: \${getFileName(uri)}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при открытии: \${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun saveFile() {
        if (currentFileUri != null) {
            saveTextToFile(currentFileUri!!)
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "новый_файл.txt")
            }
            createDocumentLauncher.launch(intent)
        }
    }

    private fun saveTextToFile(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val text = editText.text.toString()
                outputStream.write(text.toByteArray())
            }
            Toast.makeText(this, "Сохранено: \${getFileName(uri)}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении: \${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun getFileName(uri: Uri): String {
        return contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    uri.lastPathSegment?.removePrefix("document/") ?: "Безымянный.txt"
                }
            } ?: uri.lastPathSegment?.removePrefix("document/") ?: "Безымянный.txt"
    }
}
