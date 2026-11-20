package iq.kurd.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class KurdiImeService : InputMethodService() {
    private var language = 0 // 0 Sorani, 1 English, 2 Arabic

    private val soraniRows = listOf(
        listOf("چ", "ح", "خ", "ع", "غ", "ف", "ق", "ڤ"),
        listOf("ە", "و", "ر", "ڕ", "ی", "ێ", "ت", "پ"),
        listOf("ا", "ئ", "ب", "ن", "م", "ک", "گ"),
        listOf("ۆ", "د", "ز", "ژ", "س", "ش", "ل")
    )
    private val englishRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )
    private val arabicRows = listOf(
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م"),
        listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و")
    )

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(6, 7, 6, 7)
            setBackgroundColor(Color.rgb(27, 32, 42))
        }
        val toolbar = LinearLayout(this).apply { gravity = Gravity.CENTER }
        addKey(toolbar, "⚙", 1f) { openSettings() }
        addKey(toolbar, "ک", 1f) { changeLanguage(0) }
        addKey(toolbar, "En", 1f) { changeLanguage(1) }
        addKey(toolbar, "ع", 1f) { changeLanguage(2) }
        addKey(toolbar, "⌫", 1f) { currentInputConnection?.deleteSurroundingText(1, 0) }
        root.addView(toolbar)

        val digits = if (language == 1) "1 2 3 4 5 6 7 8 9 0" else "١ ٢ ٣ ٤ ٥ ٦ ٧ ٨ ٩ ٠"
        root.addView(makeRow(digits.split(" ")))
        val rows = when (language) { 0 -> soraniRows; 1 -> englishRows; else -> arabicRows }
        rows.forEach { root.addView(makeRow(it)) }

        val bottom = LinearLayout(this).apply { gravity = Gravity.CENTER }
        addKey(bottom, "؟", 0.8f) { commit("؟") }
        addKey(bottom, "،", 0.8f) { commit("،") }
        addKey(bottom, "🌐", 0.9f) { changeLanguage((language + 1) % 3) }
        addKey(bottom, "فاصلە", 3f) { commit(" ") }
        addKey(bottom, ".", 0.8f) { commit(".") }
        addKey(bottom, "↵", 1f) {
            currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        root.addView(bottom)
        return root
    }

    private fun makeRow(labels: List<String>): LinearLayout = LinearLayout(this).apply {
        gravity = Gravity.CENTER
        labels.forEach { label -> addKey(this, label, 1f) { commit(label) } }
    }

    private fun addKey(parent: LinearLayout, label: String, weight: Float, action: () -> Unit) {
        val key = Button(this).apply {
            text = label
            isAllCaps = false
            textSize = if (label.length > 2) 12f else 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(62, 71, 87))
            setOnClickListener { action() }
        }
        parent.addView(key, LinearLayout.LayoutParams(0, 47, weight).apply { setMargins(2, 2, 2, 2) })
    }

    private fun commit(value: String) { currentInputConnection?.commitText(value, 1) }
    private fun changeLanguage(value: Int) { language = value; setInputView(buildKeyboard()) }
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
