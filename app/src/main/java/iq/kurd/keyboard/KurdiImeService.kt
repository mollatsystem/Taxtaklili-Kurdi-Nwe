package iq.kurd.keyboard

import android.content.Intent
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class KurdiImeService : InputMethodService() {

    private var currentLanguage = 0

    private val layouts = listOf(
        listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
            listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ"),
            listOf("ئ", "ء", "ر", "لا", "و", "ە", "د", "ز", "ڕ", "ژ")
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

    override fun onCreateInputView(): View {
        return createKeyboard()
    }

    private fun createKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 8, 4, 8)
            setBackgroundColor(Color.rgb(225, 230, 238))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addButton(toolbar, "🌐") {
            currentLanguage = (currentLanguage + 1) % layouts.size
            setInputView(createKeyboard())
        }
        addButton(toolbar, "😊") {
            currentInputConnection?.commitText("😊", 1)
        }
        addButton(toolbar, "⚙") {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        root.addView(toolbar)

        layouts[currentLanguage].forEach { row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { key ->
                addButton(line, key) {
                    currentInputConnection?.commitText(key, 1)
                }
            }
            root.addView(line)
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addButton(bottom, "?123") { }
        addButton(bottom, "⌫") {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
        addButton(bottom, ",") {
            currentInputConnection?.commitText(",", 1)
        }
        addButton(bottom, "English/کوردی") {
            currentLanguage = (currentLanguage + 1) % layouts.size
            setInputView(createKeyboard())
        }
        addButton(bottom, "فاصلە") {
            currentInputConnection?.commitText(" ", 1)
        }
        addButton(bottom, ".") {
            currentInputConnection?.commitText(".", 1)
        }
        addButton(bottom, "↵") {
            currentInputConnection?.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
            )
            currentInputConnection?.sendKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)
            )
        }

        root.addView(bottom)
        return root
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
            setPadding(2, 4, 2, 4)
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            0,
            52,
            1f
        ).apply {
            setMargins(2, 3, 2, 3)
        }

        parent.addView(button, params)
    }
}
