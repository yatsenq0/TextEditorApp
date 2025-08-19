Вот **расширенная, очень детальная пошаговая инструкция** по созданию простого Android-приложения на **Kotlin**, которое может:

- Открывать `.txt` файлы с устройства,
- Редактировать текст в них,
- Сохранять изменения обратно в файл.

---

## 📱 Цель приложения

Создать приложение с двумя основными элементами:
1. Поле ввода (EditText) — для редактирования текста.
2. Кнопки:
   - "Открыть" — выбрать `.txt` файл.
   - "Сохранить" — сохранить текущий текст в файл (новый или перезаписать старый).

---

## 🛠️ Требования

- Установленный **Android Studio (версия Flamingo или новее)**
- Базовые знания Kotlin и Android SDK
- Устройство с Android 6.0+ или эмулятор
- Разрешение на доступ к хранилищу (для Android 6.0+)

---

## 🔧 Шаг 1: Создание нового проекта

1. Откройте **Android Studio**.
2. Нажмите **"New Project"** → **"Empty Activity"**.
3. Заполните:
   - **Name:** `TextEditorApp`
   - **Package name:** `com.example.texteditorapp`
   - **Language:** **Kotlin**
   - **Minimum SDK:** **API 23 (Android 6.0)** — важно для запроса разрешений
4. Нажмите **"Finish"**.

> Проект создастся с `MainActivity.kt`, `activity_main.xml`, `AndroidManifest.xml`.

---

## 📄 Шаг 2: Дизайн интерфейса (activity_main.xml)

Откройте `res/layout/activity_main.xml`.

Замените содержимое на следующий XML-код:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <!-- Заголовок -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Текстовый редактор .txt"
        android:textSize="20sp"
        android:layout_marginBottom="16dp"
        android:textStyle="bold" />

    <!-- Поле редактирования текста -->
    <EditText
        android:id="@+id/editText"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:hint="Введите или отредактируйте текст..."
        android:inputType="textMultiLine"
        android:gravity="top|start"
        android:minLines="10"
        android:scrollbars="vertical" />

    <!-- Кнопки -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="16dp">

        <Button
            android:id="@+id/buttonOpen"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Открыть файл"
            android:layout_marginEnd="8dp" />

        <Button
            android:id="@+id/buttonSave"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Сохранить файл"
            android:layout_marginStart="8dp" />
    </LinearLayout>

</LinearLayout>
```

---

## 🔐 Шаг 3: Добавление разрешений в AndroidManifest.xml

Откройте `app/src/main/AndroidManifest.xml`.

Добавьте разрешения **до** тега `<application>`:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

> ⚠️ Важно:
> - `WRITE_EXTERNAL_STORAGE` требуется только до **Android 10 (API 29)**.
> - Начиная с Android 10+, Google ограничил доступ к внешнему хранилищу. Мы будем использовать **Storage Access Framework (SAF)**, чтобы избежать проблем с разрешениями.

---

## 🧩 Шаг 4: Реализация MainActivity.kt

Откройте `MainActivity.kt` и замените содержимое:

```kotlin
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

    // Хранит URI открытого файла (если есть)
    private var currentFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Привязка UI элементов
        editText = findViewById(R.id.editText)
        buttonOpen = findViewById(R.id.buttonOpen)
        buttonSave = findViewById(R.id.buttonSave)

        // Настройка кнопок
        setupButtons()

        // Запрос разрешений (для Android 6.0 - 10)
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
            // Запрашиваем разрешение
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // ЛАУНЧЕР для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_LONG).show()
        }
    }

    // ЛАУНЧЕР для выбора файла
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            openTextFile(uri)
        }
    }

    // ЛАУНЧЕР для сохранения файла
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            saveTextToFile(uri)
            currentFileUri = uri // Сохраняем URI для будущих сохранений
        }
    }

    private fun openFile() {
        // Используем Storage Access Framework
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            // Можно добавить фильтр по расширению
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
                Toast.makeText(this, "Файл открыт: ${getFileName(uri)}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при открытии файла: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun saveFile() {
        if (currentFileUri != null) {
            // Пытаемся перезаписать текущий файл
            saveTextToFile(currentFileUri!!)
        } else {
            // Предлагаем пользователю выбрать имя и место
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
            Toast.makeText(this, "Файл сохранён: ${getFileName(uri)}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // Получаем имя файла по URI
    private fun getFileName(uri: Uri): String {
        return contentResolver.query(uri, arrayOf(android.provider.MediaStore.DisplayName), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    uri.lastPathSegment?.removePrefix("document/") ?: "Безымянный.txt"
                }
            } ?: uri.lastPathSegment?.removePrefix("document/") ?: "Безымянный.txt"
    }
}
```

---

## ✅ Объяснение ключевых частей кода

### 1. **Storage Access Framework (SAF)**

- Используем `OpenDocument` и `CreateDocument` — это современный способ работы с файлами.
- Не требует `WRITE_EXTERNAL_STORAGE` на Android 11+.
- Пользователь сам выбирает файл через системный диалог.

### 2. **ActivityResultContracts**

- Замена устаревшему `startActivityForResult()`.
- Безопаснее и чище.

### 3. **Разрешения**

- Только `READ_EXTERNAL_STORAGE` — для доступа к файлам.
- На Android 11+ (API 30+) это разрешение почти не работает, поэтому SAF обязателен.

### 4. **Работа с URI**

- Файл не читается напрямую по пути, а через `ContentResolver`.
- Это безопасно и соответствует политике Android.

---

## 🧪 Шаг 5: Тестирование приложения

1. Подключите устройство или запустите эмулятор.
2. Нажмите ▶️ **Run** в Android Studio.
3. При первом запуске:
   - Приложение запросит разрешение на чтение.
   - Разрешите его.
4. Нажмите **"Открыть файл"**:
   - Выберите `.txt` файл (например, созданный в проводнике).
   - Текст появится в поле.
5. Отредактируйте текст.
6. Нажмите **"Сохранить файл"**:
   - Если файл был открыт — он перезапишется.
   - Если нет — предложит имя и место.

---

## 📁 Шаг 6: Создание тестового .txt файла

Чтобы протестировать:

1. Через проводник на телефоне создайте файл `test.txt`.
2. Напишите в нём что-то, например:
   ```
   Привет, это тестовый файл!
   Можно редактировать и сохранять.
   ```
3. Загрузите его в папку `Downloads` или `Documents`.
4. Откройте через приложение.

---

## 🧰 Дополнительные улучшения (по желанию)

| Функция | Как реализовать |
|--------|-----------------|
| Поддержка `.txt` в списке выбора | Уже есть через `type = "text/plain"` |
| Авто-сохранение | Используйте `SharedPreferences` для временного хранения |
| Подсветка расширения | Проверяйте имя файла: `if (name.endsWith(".txt"))` |
| Диалог подтверждения перезаписи | Добавьте `AlertDialog` перед сохранением |
| Темная тема | Добавьте `Theme.Material3.DayNight` |

---

## 📦 Шаг 7: Сборка APK

1. В Android Studio: **Build → Generate Signed Bundle / APK**
2. Выберите **APK**
3. Создайте новый ключ (keystore) или используйте существующий
4. Укажите путь, пароль, alias
5. Выберите `release` и подпишите
6. Готово! APK можно установить на любом устройстве.

---

## 📚 Полезные ссылки

- [Storage Access Framework (SAF)](https://developer.android.com/guide/topics/providers/document-provider)
- [ActivityResult API](https://developer.android.com/training/basics/intents/result)
- [Android Permissions Guide](https://developer.android.com/guide/topics/permissions/overview)

---

## ✅ Вывод

Вы создали полноценное Android-приложение на Kotlin, которое:

- Открывает `.txt` файлы через системный диалог,
- Позволяет редактировать текст,
- Сохраняет изменения (в старый или новый файл),
- Работает на всех версиях Android от 6.0 и выше,
- Использует современные практики (SAF, ActivityResult, безопасные разрешения).

---

Если хочешь — могу добавить:
- Поддержку нескольких вкладок,
- Синтаксическую подсветку,
- Экспорт в PDF,
- Резервное копирование.

Напиши, что нужно!
