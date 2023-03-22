package com.chat.aichatbot.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.chat.aichatbot.R
import com.chat.aichatbot.databinding.ClearHistoryDialogBinding


class CustomDialog(context: Context, private val onButtonClickListeners: OnButtonClickListeners) :
    Dialog(context) {
    private lateinit var binding: ClearHistoryDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ClearHistoryDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            yesBtn.setOnClickListener {
                if (::binding.isInitialized) {
                    onButtonClickListeners.onYesClick()
                }
                this@CustomDialog.dismiss()
            }
            noBtn.setOnClickListener {
                this@CustomDialog.dismiss();
            }
        }


    }

    interface OnButtonClickListeners {
        fun onYesClick()
    }

    fun setTextValue(sureTxt: String, title: String) {
        if (::binding.isInitialized) {
            binding.title.text = title
            binding.sureTXT.text = sureTxt
        }
    }
}