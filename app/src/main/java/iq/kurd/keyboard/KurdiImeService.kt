package iq.kurd.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.graphics.Typeface
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.app.AlertDialog

class KurdiImeService : InputMethodService() {

    private var currentLanguage = 0
    private var symbolsMode = false

    private val prefs by lazy {
        getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
    }

    private val languageNames = listOf("کوردی", "English", "العربية")

    private val layouts = listOf(
        listOf(
            listOf("ق", "و", "ە", "ر", "ت", "ی", "ئ", "ح", "ۆ", "پ"),
            listOf("ا", "س", "د", "ف", "گ", "ه", "ژ", "ک", "ل", "م"),
            listOf("ز", "خ", "ج", "ڤ", "ب", "ن", "م", "چ")
        ),
        listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح"),
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك"),
            listOf("ئ", "ء", "ر", "لا", "و", "ة", "د", "ز", "ى")
        )
    )

    private val symbolRows = listOf(
        listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
        listOf("*", "\"", "'", ":", ";", "!", "؟", "/", "=")
    )

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 7, 5, 7)
            setBackgroundColor(Color.rgb(19, 28, 48))
        }

        if (prefs.getBoolean("toolbar_visible", true)) {
            root.addView(buildToolsToolbar())
        }
        root.addView(buildMainToolbar())

        val keyRows = if (symbolsMode) symbolRows else layouts[currentLanguage]
        keyRows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { key ->
                addKey(line, key, 1f) { commit(key) }
            }
            root.addView(line)
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addSpecialKey(bottom, if (symbolsMode) "ABC" else "?123", 1.25f) {
            symbolsMode = !symbolsMode
            setInputView(buildKeyboard())
        }
        addSpecialKey(bottom, "⌫", 1f) { deleteBeforeCursor() }
        addSpecialKey(bottom, "،", 0.8f) { commit(if (currentLanguage == 1) "," else "،") }
        addSpecialKey(bottom, languageNames[currentLanguage], 1.35f) { cycleLanguage() }
        addSpecialKey(bottom, "فاصلە", 2.4f) { commit(" ") }
        addSpecialKey(bottom, ".", 0.8f) { commit(".") }
        addSpecialKey(bottom, "↵", 1f) { sendEnter() }
        root.addView(bottom)

        return ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        }
    }

    private fun buildToolsToolbar(): View {
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        addTool(strip, "＋ وشە") { showPhraseManager() }
        addTool(strip, "📋") { pasteClipboard() }
        addTool(strip, "◀") { moveCursor(-1) }
        addTool(strip, "▶") { moveCursor(1) }
        addTool(strip, "✂") { cutSelection() }
        addTool(strip, "کۆپی") { copySelection() }
        addTool(strip, "هەموو") { selectAllText() }
        addTool(strip, "−") {
            prefs.edit().putBoolean("toolbar_visible", false).apply()
            setInputView(buildKeyboard())
        }
        return HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(strip)
        }
    }

    private fun buildMainToolbar(): View {
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addTool(strip, "ک") { currentLanguage = 0; symbolsMode = false; setInputView(buildKeyboard()) }
        addTool(strip, "En") { currentLanguage = 1; symbolsMode = false; setInputView(buildKeyboard()) }
        addTool(strip, "ع") { currentLanguage = 2; symbolsMode = false; setInputView(buildKeyboard()) }
        addTool(strip, "📋") { pasteClipboard() }
        addTool(strip, "😊") { showEmojiPicker() }
        addTool(strip, "⚙") {
            startActivity(Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        return HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(strip)
        }
    }

    private fun addTool(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(38, 58, 91))
            setPadding(5, 1, 5, 1)
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 42
        ).apply { setMargins(2, 2, 2, 4) })
    }

    private fun addKey(parent: LinearLayout, label: String, weight: Float, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = prefs.getInt("key_text_size", 21).toFloat()
            isAllCaps = false
            typeface = Typeface.DEFAULT
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(43, 57, 82))
            setPadding(0, 0, 0, 0)
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(0, 49, weight).apply {
            setMargins(2, 3, 2, 3)
        })
    }

    private fun addSpecialKey(parent: LinearLayout, label: String, weight: Float, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(25, 104, 157))
            setPadding(0, 0, 0, 0)
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(0, 49, weight).apply {
            setMargins(2, 3, 2, 3)
        })
    }

    private fun commit(value: String) {
        currentInputConnection?.commitText(value, 1)
    }

    private fun deleteBeforeCursor() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) ic.commitText("", 1)
        else ic.deleteSurroundingText(1, 0)
    }

    private fun moveCursor(direction: Int) {
        val keyCode = if (direction < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        sendKey(keyCode)
    }

    private fun sendEnter() = sendKey(KeyEvent.KEYCODE_ENTER)

    private fun sendKey(code: Int) {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    private fun copySelection() {
        val text = currentInputConnection?.getSelectedText(0)?.toString().orEmpty()
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
        } else {
            currentInputConnection?.performContextMenuAction(android.R.id.copy)
        }
    }

    private fun cutSelection() {
        copySelection()
        currentInputConnection?.performContextMenuAction(android.R.id.cut)
    }

    private fun pasteClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(this)
        if (text != null) currentInputConnection?.commitText(text, 1)
    }

    private fun selectAllText() {
        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
    }

    private fun cycleLanguage() {
        currentLanguage = (currentLanguage + 1) % layouts.size
        symbolsMode = false
        setInputView(buildKeyboard())
    }

    private fun showEmojiPicker() {
        val emojis = arrayOf("😀", "😂", "🥰", "😍", "😊", "🙏", "❤️", "👍", "🌹", "🎉", "😢", "🔥")
        AlertDialog.Builder(this)
            .setTitle("ئیمۆجی")
            .setItems(emojis) { _, which -> commit(emojis[which]) }
            .show()
    }

    private fun showPhraseManager() {
        val saved = prefs.getStringSet("phrases", emptySet())?.toMutableList() ?: mutableListOf()
        val options = (saved + "＋ زیادکردنی دەستەواژە").toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("دەستەواژەی خێرا")
            .setItems(options) { _, which ->
                if (which == options.lastIndex) {
                    val input = EditText(this)
                    AlertDialog.Builder(this)
                        .setTitle("دەستەواژەی نوێ")
                        .setView(input)
                        .setPositiveButton("پاشەکەوت") { _, _ ->
                            val value = input.text.toString().trim()
                            if (value.isNotEmpty()) {
                                saved.add(value)
                                prefs.edit().putStringSet("phrases", saved.toSet()).apply()
                                commit(value)
                            }
                        }
                        .setNegativeButton("پاشگەزبوونەوە", null)
                        .show()
                } else {
                    commit(saved[which])
                }
            }
            .show()
    }
}
