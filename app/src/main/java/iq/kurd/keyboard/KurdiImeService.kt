package iq.kurd.keyboard

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class KurdiImeService : InputMethodService() {
    private var currentLanguage = 0
    private var symbolsMode = false
    private val prefs by lazy { getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE) }
    private val languageNames = listOf("کوردی", "English", "العربية")

    private val layouts = listOf(
        listOf(
            listOf("ق", "و", "ە", "ر", "ت", "ی", "ئ", "ح", "ۆ", "پ"),
            listOf("ا", "س", "د", "ف", "گ", "ه", "ژ", "ک", "ل", "م"),
            listOf("ز", "خ", "ج", "ڤ", "ب", "ن", "چ")
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

    private val dark get() = !prefs.getBoolean("light_theme", false)
    private val panel get() = if (dark) Color.rgb(20, 29, 47) else Color.rgb(232, 238, 247)
    private val keyColor get() = if (dark) Color.rgb(43, 57, 80) else Color.WHITE
    private val keyText get() = if (dark) Color.WHITE else Color.rgb(25, 42, 66)
    private val accent get() = if (dark) Color.rgb(30, 117, 174) else Color.rgb(25, 111, 174)
    private val muted get() = if (dark) Color.rgb(34, 48, 70) else Color.rgb(215, 226, 240)

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(7), dp(6), dp(8))
            setBackgroundColor(panel)
        }
        if (prefs.getBoolean("toolbar_visible", true)) root.addView(buildToolsToolbar())
        root.addView(buildLanguageToolbar())

        val rows = if (symbolsMode) symbolRows else layouts[currentLanguage]
        rows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                if (index == 1) setPadding(dp(8), 0, dp(8), 0)
                if (index == 2) setPadding(dp(17), 0, dp(17), 0)
            }
            row.forEach { key -> addKey(line, key, 1f) { commit(key) } }
            root.addView(line, LinearLayout.LayoutParams(-1, dp(51)).apply {
                topMargin = dp(2); bottomMargin = dp(2)
            })
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addSpecialKey(bottom, if (symbolsMode) "ABC" else "؟١٢", 1.12f) {
            symbolsMode = !symbolsMode; refreshKeyboard()
        }
        addSpecialKey(bottom, "⌫", 0.92f, accent) { deleteBeforeCursor() }
        addSpecialKey(bottom, "،", 0.72f) { commit(if (currentLanguage == 1) "," else "،") }
        addSpecialKey(bottom, languageNames[currentLanguage], 1.12f) { cycleLanguage() }
        addSpecialKey(bottom, "فاصلە", 2.05f, accent) { commit(" ") }
        addSpecialKey(bottom, ".", 0.7f) { commit(if (currentLanguage == 2) "۔" else ".") }
        addSpecialKey(bottom, "↵", 0.92f, accent) { sendEnter() }
        root.addView(bottom, LinearLayout.LayoutParams(-1, dp(51)).apply { topMargin = dp(3) })

        return ScrollView(this).apply { isFillViewport = true; addView(root) }
    }

    private fun buildToolsToolbar(): View {
        val strip = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        addTool(strip, "＋ وشە") { showPhraseManager() }
        addTool(strip, "📋") { pasteClipboard() }
        addTool(strip, "◀") { moveCursor(-1) }
        addTool(strip, "▶") { moveCursor(1) }
        addTool(strip, "✂") { cutSelection() }
        addTool(strip, "کۆپی") { copySelection() }
        addTool(strip, "هەموو") { selectAllText() }
        addTool(strip, "−") {
            prefs.edit().putBoolean("toolbar_visible", false).apply(); refreshKeyboard()
        }
        return HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(strip)
        }
    }

    private fun buildLanguageToolbar(): View {
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(1), 0, dp(4))
        }
        addTool(strip, "کوردی") { setLanguage(0) }
        addTool(strip, "En") { setLanguage(1) }
        addTool(strip, "عربي") { setLanguage(2) }
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
        val selected = (label == "کوردی" && currentLanguage == 0) || (label == "En" && currentLanguage == 1) || (label == "عربي" && currentLanguage == 2)
        val view = TextView(this).apply {
            text = label
            textSize = 12f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (selected) Color.WHITE else keyText)
            background = rounded(if (selected) accent else muted, 12)
            setPadding(dp(11), 0, dp(11), 0)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        parent.addView(view, LinearLayout.LayoutParams(-2, dp(36)).apply {
            setMargins(dp(2), dp(2), dp(2), dp(3))
        })
    }

    private fun addKey(parent: LinearLayout, label: String, weight: Float, action: () -> Unit) {
        val view = TextView(this).apply {
            text = label
            textSize = prefs.getInt("key_text_size", 22).toFloat()
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT
            setTextColor(keyText)
            background = rounded(keyColor, 9)
            elevation = dp(1).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        parent.addView(view, LinearLayout.LayoutParams(0, dp(46), weight).apply {
            setMargins(dp(2), dp(2), dp(2), dp(2))
        })
    }

    private fun addSpecialKey(parent: LinearLayout, label: String, weight: Float, color: Int = muted, action: () -> Unit) {
        val view = TextView(this).apply {
            text = label
            textSize = if (label == "فاصلە") 12f else 14f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (color == muted) keyText else Color.WHITE)
            background = rounded(color, 10)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        parent.addView(view, LinearLayout.LayoutParams(0, dp(46), weight).apply {
            setMargins(dp(2), dp(2), dp(2), dp(2))
        })
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun refreshKeyboard() { setInputView(buildKeyboard()) }
    private fun setLanguage(index: Int) { currentLanguage = index; symbolsMode = false; refreshKeyboard() }
    private fun commit(value: String) { currentInputConnection?.commitText(value, 1) }

    private fun deleteBeforeCursor() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) ic.commitText("", 1) else ic.deleteSurroundingText(1, 0)
    }
    private fun moveCursor(direction: Int) {
        sendKey(if (direction < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT)
    }
    private fun sendEnter() = sendKey(KeyEvent.KEYCODE_ENTER)
    private fun sendKey(code: Int) {
        val ic: InputConnection = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code)); ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }
    private fun copySelection() {
        val text = currentInputConnection?.getSelectedText(0)?.toString().orEmpty()
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
        } else currentInputConnection?.performContextMenuAction(android.R.id.copy)
    }
    private fun cutSelection() { copySelection(); currentInputConnection?.performContextMenuAction(android.R.id.cut) }
    private fun pasteClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(this)
        if (text != null) currentInputConnection?.commitText(text, 1)
    }
    private fun selectAllText() { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) }
    private fun cycleLanguage() { setLanguage((currentLanguage + 1) % layouts.size) }

    private fun showEmojiPicker() {
        val emojis = arrayOf("😀", "😂", "🥰", "😍", "😊", "🙏", "❤️", "👍", "🌹", "🎉", "😢", "🔥", "💚", "💛", "💙", "🌷")
        AlertDialog.Builder(this).setTitle("ئیمۆجی").setItems(emojis) { _, which -> commit(emojis[which]) }.show()
    }
    private fun showPhraseManager() {
        val saved = prefs.getStringSet("phrases", emptySet())?.toMutableList() ?: mutableListOf()
        val options = (saved + "＋ زیادکردنی دەستەواژە").toTypedArray()
        AlertDialog.Builder(this).setTitle("دەستەواژەی خێرا").setItems(options) { _, which ->
            if (which == options.lastIndex) {
                val input = EditText(this)
                AlertDialog.Builder(this).setTitle("دەستەواژەی نوێ").setView(input)
                    .setPositiveButton("پاشەکەوت") { _, _ ->
                        val value = input.text.toString().trim()
                        if (value.isNotEmpty()) { saved.add(value); prefs.edit().putStringSet("phrases", saved.toSet()).apply(); commit(value) }
                    }.setNegativeButton("پاشگەزبوونەوە", null).show()
            } else commit(saved[which])
        }.show()
    }
}
