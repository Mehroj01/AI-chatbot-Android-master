package com.chat.aichatbot.utils

import android.util.TypedValue
import android.widget.TextView

object TextSizeValue {
    fun setSizeToTV(size: Float, textView: TextView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    fun floatSizes(): List<Float> {
        return listOf(15f, 16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f, 24f, 25f, 26f, 27f)
    }

    fun sizeInDesign(): List<String> {
        return listOf(
            "15px",
            "16px",
            "17px",
            "18px",
            "19px",
            "20px",
            "21px",
            "22px",
            "23px",
            "24px",
            "25px",
            "26px",
            "27px"
        )
    }
}