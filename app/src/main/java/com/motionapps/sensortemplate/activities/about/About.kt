package com.motionapps.sensortemplate.activities.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.motionapps.sensortemplate.R

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val linearLayout = findViewById<LinearLayout>(R.id.about_linear)

        // creation of views filled with title and text of license
        for (about_titles in AboutEnum.values()) {
            //title
            val title = TextView(this)
            title.setText(about_titles.title)
            title.setTextColor(ContextCompat.getColor(this, R.color.black))
            title.textSize = 20f
            title.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            title.setPadding(8, 8, 8, 8)

            linearLayout.addView(title)

            val space = Space(this)
            space.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 30
            )
            linearLayout.addView(space)

            // text
            val text = TextView(this)
            text.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text.setText(about_titles.text)
            text.setPadding(8, 8, 8, 8)
            text.setTextColor(ContextCompat.getColor(this, R.color.black))

            linearLayout.addView(text)

        }
    }
}
