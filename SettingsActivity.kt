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
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFFF3F5F9.toInt())
        }
        page.addView(TextView(this).apply {
            text = "تەختەکلیلی کوردی نوێ"
            textSize = 26f
            setTextColor(0xFF202938.toInt())
        })
        page.addView(TextView(this).apply {
            text = "وەشانی سەرەتایی 0.1.0 • کوردی / English / العربية"
            textSize = 14f
            setTextColor(0xFF667085.toInt())
        })
        page.addView(Button(this).apply {
            text = "چالاککردنی تەختەکلیل"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        page.addView(Button(this).apply {
            text = "هەڵبژاردنی تەختەکلیل"
            setOnClickListener {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        })
        setContentView(page)
    }
}
