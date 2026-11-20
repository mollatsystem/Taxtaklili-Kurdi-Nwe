package iq.kurd.keyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 32)
        }

        val title = TextView(this).apply {
            text = "تەختەکلیلی کوردی نوێ"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(title)

        val description = TextView(this).apply {
            text = "بەخێربێیت بۆ تەختەکلیلی کوردی نوێ"
            textSize = 16f
            setPadding(0, 24, 0, 24)
        }
        layout.addView(description)

        val enableButton = Button(this).apply {
            text = "چالاککردنی تەختەکلیل"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }
        layout.addView(enableButton)

        val chooseButton = Button(this).apply {
            text = "هەڵبژاردنی تەختەکلیل"
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
        layout.addView(chooseButton)

        val aboutButton = Button(this).apply {
            text = "دەربارەی تەختەکلیل"
            setOnClickListener {
                showAbout()
            }
        }
        layout.addView(aboutButton)

        setContentView(layout)
    }

    private fun showAbout() {
        val aboutText = """
            ئەم تەختەکلیلە بەناوی (تەختەکلیلی کوردی نوێ)،
            دروستکراوە لە عێراق - هەرێمی کوردستان،
            بۆ سەرجەم هاوڵاتیان و تاکی کوردی خۆشەویست
            تاکو بتوانن بە زمانی کوردی ناوەندی ڕەوان
            دەق و ڕستەکانیان بنووسن.

            وەشان: 0.1.0

            هەموو مافەکان پارێزراوە بۆ دروستکەر.
            هەرشادبن!
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("دەربارە")
            .setMessage(aboutText)
            .setPositiveButton("باشە", null)
            .show()
    }
}
