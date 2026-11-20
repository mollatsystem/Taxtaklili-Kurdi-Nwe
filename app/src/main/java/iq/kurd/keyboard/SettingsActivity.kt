package iq.kurd.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SettingsActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 28)
            setBackgroundColor(android.graphics.Color.rgb(239, 243, 249))
        }

        val logo = TextView(this).apply {
            text = "ك"
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(190, 139, 37))
        }
        content.addView(logo)

        val title = TextView(this).apply {
            text = "تەختەکلیلی کوردی نوێ"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(20, 42, 78))
            setPadding(0, 0, 0, 22)
        }
        content.addView(title)

        addSection(content, "چالاککردن و هەڵبژاردنی تەختەکلیل") { showKeyboardSetup() }
        addSection(content, "شێوازی تەختەکلیل") { chooseTheme() }
        addSection(content, "قەبارەی دوگمەکان") { chooseKeySize() }
        addSection(content, "شریتی ئامرازەکان") { toggleToolbar() }
        addSection(content, "دەستەواژەی خێرا") { showPhrases() }
        addSection(content, "دەربارەی تەختەکلیل") { showAbout() }

        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun addSection(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = 16f
            isAllCaps = false
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 56
        ).apply { setMargins(0, 5, 0, 5) })
    }

    private fun showKeyboardSetup() {
        AlertDialog.Builder(this)
            .setTitle("چالاککردنی تەختەکلیل")
            .setMessage("١. تەختەکلیلەکە چالاک بکە.\n٢. پاشان لە لیستی تەختەکلیلەکان هەڵیبژێرە.")
            .setPositiveButton("کردنەوەی ڕێکخستنەکان") { _, _ ->
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
            .setNeutralButton("هەڵبژاردنی تەختەکلیل") { _, _ ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
            .setNegativeButton("داخستن", null)
            .show()
    }

    private fun chooseTheme() {
        val names = arrayOf("شەوی شین", "ڕووناک")
        AlertDialog.Builder(this)
            .setTitle("شێوازی تەختەکلیل")
            .setItems(names) { _, which ->
                prefs.edit().putBoolean("light_theme", which == 1).apply()
            }
            .show()
    }

    private fun chooseKeySize() {
        val names = arrayOf("بچووک", "مامناوەند", "گەورە")
        val sizes = intArrayOf(18, 21, 24)
        AlertDialog.Builder(this)
            .setTitle("قەبارەی نووسینی دوگمەکان")
            .setItems(names) { _, which ->
                prefs.edit().putInt("key_text_size", sizes[which]).apply()
            }
            .show()
    }

    private fun toggleToolbar() {
        val visible = prefs.getBoolean("toolbar_visible", true)
        prefs.edit().putBoolean("toolbar_visible", !visible).apply()
        AlertDialog.Builder(this)
            .setMessage(if (visible) "شریتی ئامرازەکان شاردراوەتەوە. لە هەمان بەشەوە دەتوانیت بیگەڕێنیتەوە." else "شریتی ئامرازەکان چالاک کرا.")
            .setPositiveButton("باشە", null)
            .show()
    }

    private fun showPhrases() {
        val phrases = prefs.getStringSet("phrases", emptySet()).orEmpty()
        AlertDialog.Builder(this)
            .setTitle("دەستەواژە خێراکان")
            .setMessage(if (phrases.isEmpty()) "هێشتا هیچ دەستەواژەیەک زیاد نەکراوە. لە خودی تەختەکلیلەکە دوگمەی ＋ وشە بەکاربهێنە." else phrases.joinToString("\n"))
            .setPositiveButton("باشە", null)
            .show()
    }

    private fun showAbout() {
        val aboutText = """
            ئەم تەختەکلیلە بەناوی ( تەختەکلیلی کوردی نوێ )، دروستکراوە لە عێراق - هەرێمی کوردستان، بۆ سەرجەم هاوڵاتیان و تاکی کوردی خۆشەویست تاکو بتوانن بە زمانی کوردی ناوەندی ڕەوان دەق و ڕستەکانیان بنوین، پێشکەشە بە سەرجەم تاکێکی کورد لە کوردستان و تەواوی جیهاندا.... هەرشادبن....!

            وەشان: 0.1.0

            سەرجەم مافەکان پارێزراوە بۆ دروستکاری تەختەکلیل.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("ك  |  تەختەکلیلی کوردی نوێ")
            .setMessage(aboutText)
            .setPositiveButton("باشە", null)
            .show()
    }
}
