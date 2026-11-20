package iq.kurd.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SettingsActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE) }
    private val navy = Color.rgb(20, 38, 65)
    private val blue = Color.rgb(29, 112, 169)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(26))
            setBackgroundColor(Color.rgb(238, 243, 250))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = shape(navy, 22)
            setPadding(dp(16), dp(18), dp(16), dp(20))
        }
        val logo = TextView(this).apply {
            text = "ك"
            textSize = 48f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(239, 190, 83))
        }
        header.addView(logo)
        val title = TextView(this).apply {
            text = "تەختەکلیلی کوردی نوێ"
            textSize = 22f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }
        header.addView(title)
        val subtitle = TextView(this).apply {
            text = "ڕێکخستن و تایبەتمەندکردنی تەختەکلیل"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(207, 220, 238))
            setPadding(0, dp(5), 0, 0)
        }
        header.addView(subtitle)
        content.addView(header, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) })

        addHeading(content, "گشتی")
        addSetting(content, "چالاککردن و هەڵبژاردنی تەختەکلیل", "تەختەکلیلەکە چالاک بکە یان هەڵیبژێرە") { showKeyboardSetup() }
        addSetting(content, "شێوازی ڕەنگ", "شەوی شین یان ڕووناک") { chooseTheme() }
        addSetting(content, "قەبارەی پیتەکان", "بچووک، مامناوەند یان گەورە") { chooseKeySize() }
        addSetting(content, "شریتی ئامرازەکان", "پیشاندان یان شاردنەوە") { toggleToolbar() }

        addHeading(content, "ئامرازەکان")
        addSetting(content, "دەستەواژەی خێرا", "بینین و بەڕێوەبردنی دەستەواژەکان") { showPhrases() }
        addSetting(content, "دەربارەی تەختەکلیل", "زانیاری و مافی دروستکار") { showAbout() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(content) })
    }

    private fun addHeading(parent: LinearLayout, text: String) {
        parent.addView(TextView(this).apply {
            this.text = text
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
            setPadding(dp(3), dp(8), dp(3), dp(5))
        })
    }

    private fun addSetting(parent: LinearLayout, title: String, description: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(Color.WHITE, 15)
            setPadding(dp(15), dp(12), dp(15), dp(12))
            isClickable = true
            isFocusable = true
            elevation = dp(1).toFloat()
            setOnClickListener { action() }
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        })
        card.addView(TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(Color.rgb(102, 118, 139))
            setPadding(0, dp(4), 0, 0)
        })
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(8)
        })
    }

    private fun showKeyboardSetup() {
        AlertDialog.Builder(this).setTitle("چالاککردنی تەختەکلیل")
            .setMessage("١. تەختەکلیلەکە چالاک بکە.\n٢. پاشان لە لیستی تەختەکلیلەکان هەڵیبژێرە.")
            .setPositiveButton("کردنەوەی ڕێکخستنەکان") { _, _ -> startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
            .setNeutralButton("هەڵبژاردنی تەختەکلیل") { _, _ ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }.setNegativeButton("داخستن", null).show()
    }

    private fun chooseTheme() {
        val names = arrayOf("شەوی شین", "ڕووناک")
        AlertDialog.Builder(this).setTitle("شێوازی ڕەنگ").setSingleChoiceItems(names, if (prefs.getBoolean("light_theme", false)) 1 else 0) { dialog, which ->
            prefs.edit().putBoolean("light_theme", which == 1).apply()
            dialog.dismiss()
        }.setNegativeButton("داخستن", null).show()
    }

    private fun chooseKeySize() {
        val names = arrayOf("بچووک", "مامناوەند", "گەورە")
        val sizes = intArrayOf(19, 22, 25)
        val current = prefs.getInt("key_text_size", 22)
        val selected = sizes.indices.minByOrNull { kotlin.math.abs(sizes[it] - current) } ?: 1
        AlertDialog.Builder(this).setTitle("قەبارەی پیتەکان").setSingleChoiceItems(names, selected) { dialog, which ->
            prefs.edit().putInt("key_text_size", sizes[which]).apply()
            dialog.dismiss()
        }.setNegativeButton("داخستن", null).show()
    }

    private fun toggleToolbar() {
        val visible = prefs.getBoolean("toolbar_visible", true)
        prefs.edit().putBoolean("toolbar_visible", !visible).apply()
        AlertDialog.Builder(this).setMessage(if (visible) "شریتی ئامرازەکان شاردراوەتەوە. بۆ گەڕاندنەوەی، ئەم هەڵبژاردەیە دووبارە بەکاربهێنە." else "شریتی ئامرازەکان چالاک کرا.")
            .setPositiveButton("باشە", null).show()
    }

    private fun showPhrases() {
        val phrases = prefs.getStringSet("phrases", emptySet()).orEmpty()
        AlertDialog.Builder(this).setTitle("دەستەواژە خێراکان")
            .setMessage(if (phrases.isEmpty()) "هێشتا هیچ دەستەواژەیەک زیاد نەکراوە. لە کیبۆردەکە دوگمەی ＋ وشە بەکاربهێنە." else phrases.joinToString("\n"))
            .setPositiveButton("باشە", null).show()
    }

    private fun showAbout() {
        val aboutText = """
            ئەم تەختەکلیلە بەناوی ( تەختەکلیلی کوردی نوێ )، دروستکراوە لە عێراق - هەرێمی کوردستان، بۆ سەرجەم هاوڵاتیان و تاکی کوردی خۆشەویست تاکو بتوانن بە زمانی کوردی ناوەندی ڕەوان دەق و ڕستەکانیان بنوین، پێشکەشە بە سەرجەم تاکێکی کورد لە کوردستان و تەواوی جیهاندا.... هەرشادبن....!

            وەشان: 0.2.0

            سەرجەم مافەکان پارێزراوە بۆ دروستکاری تەختەکلیل.
        """.trimIndent()
        AlertDialog.Builder(this).setTitle("ك  |  تەختەکلیلی کوردی نوێ")
            .setMessage(aboutText).setPositiveButton("باشە", null).show()
    }

    private fun shape(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
