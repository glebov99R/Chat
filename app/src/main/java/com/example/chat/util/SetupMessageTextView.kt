package com.example.chat.util

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView

fun TextView.setupMessageTextView(itemView: View) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // получаем ширину экрана
            val displayMetrics = itemView.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // получаем ширину текста в TextView
            val paint = this@setupMessageTextView.paint
            val textWidth = paint.measureText(this@setupMessageTextView.text.toString())

            // устанавливаем максимальное количество строк в TextView
            val maxLines = 4

            // Устанавливаем максимальную длину строки
            this@setupMessageTextView.maxWidth = (screenWidth * 0.65).toInt()

            // проверяем, нужно ли переносить текст на новую строку
            if (textWidth > screenWidth * 3 / 4) {
                this@setupMessageTextView.maxLines = maxLines
                this@setupMessageTextView.ellipsize = TextUtils.TruncateAt.END
            } else {
                this@setupMessageTextView.maxLines = Integer.MAX_VALUE
                this@setupMessageTextView.ellipsize = null
            }
        }
    })
}
