package com.chat.aichatbot.utils

import com.chat.aichatbot.models.StaticModule

object Constants {
    const val CHECKED: String="checked"
    const val TV_SIZE = "tv_size"
    const val SLIDER_PROGRESS = "SLIDER_PROGRESS"
    val arrayList = arrayListOf(
        StaticModule(
            "Ask Questions...", arrayListOf(
                "“Explain how internet works for \n" +
                        "6 year old”",
                "“Why is the sky blue?”",
                "“Got any creative ideas for a 5 year \n" +
                        "old’s birthday?”"
            )
        ),
        StaticModule(
            "Write...", arrayListOf(
                "“Write a tweet about self motivation”",
                "“Write poem on love”"
            )
        ),
        StaticModule(
            "Translate...", arrayListOf(
                "Translate “hello” to Spanish",
                "Translate “How can I help you” to Chinese"
            )
        ),
        StaticModule(
            "Email...", arrayListOf(
                "Write email for job application",
                "Write email for leave approval"
            )
        ),
        StaticModule(
            "Ask recipe...", arrayListOf(
                "Easy 3 breakfast recipe ",
                "Recipe for nutritious smoothie",
                "Two green salad recipe"
            )
        ),
        StaticModule(
            "Education...", arrayListOf(
                "5 Best books for mathematics",
                "Solve 5x2(12) + 5x102-234(2+93)",
                "Best learning book for 12 year child"
            )
        ),
    )
}