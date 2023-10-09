package com.chat.aichatbot.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chat.aichatbot.R
import com.chat.aichatbot.databinding.ThreadItemRvBinding
import com.chat.aichatbot.room.AppDatabase
import com.chat.aichatbot.room.ThreadModule
import com.chat.aichatbot.utils.CustomDialog

class ThreadAdapter(
    private val itemClick: ItemOnClickListener
) : ListAdapter<ThreadModule, ThreadAdapter.ViewHolder>(MyDiffUtils()) {

    class ViewHolder(var binding: ThreadItemRvBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ThreadItemRvBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val threadModule = getItem(position)
        val messages = AppDatabase.getInstance(holder.itemView.context).messageDao()
            .getThreadUserMessages(threadModule.id, true)
        holder.binding.apply {
            threadNum.text = threadModule.name
            if (messages.size > 4) {
                extraQuestions.visibility = View.VISIBLE
                extraQuestions.text = "${messages.size - 4} more questions"
                var string = ""
                for (i in 0 until 4) {
                    string += if (i == 0) {
                        "\n${i + 1}. ${messages[i].message}"
                    } else {
                        "\n" +
                                "\n${i + 1}. ${messages[i].message}"
                    }
                }
                questions.text = string
                extraQuestions.setOnClickListener {
                    var string1 = ""
                    for (i in messages.indices) {
                        if (i == 0) {
                            string1 += "\n${i + 1}. ${messages[i].message}"
                        }else {
                            string1 += "\n" +
                                    "\n${i + 1}. ${messages[i].message}"
                            questions.text = string1
                        }
                    }
                    extraQuestions.visibility = View.GONE
                }
            } else {
                var string = ""
                for (i in messages.indices) {
                    string += if (i == 0) {
                        "\n${i + 1}. ${messages[i].message}"
                    } else {
                        "\n" +
                                "\n${i + 1}. ${messages[i].message}"
                    }
                }
                questions.text = string
                extraQuestions.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            if (currentList.size <= position) {
                itemClick.onItemClick(getItem(currentList.size - 1))
            } else {
                itemClick.onItemClick(getItem(position))
            }
        }

        holder.binding.contextualMenu.setOnClickListener {
            showPopUpIcon(it, threadModule, position)
        }
    }


    private fun showPopUpIcon(view: View, message: ThreadModule, position: Int) {
        val popupWindow = PopupWindow(view.context)
        val inflater =
            view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.custom_popup_dialog, null)
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val backgroundDrawable = ColorDrawable(Color.TRANSPARENT)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(backgroundDrawable)

        popupWindow.contentView = customView

        val copy = customView.findViewById<LinearLayout>(R.id.copy)
        copy.setOnClickListener {
            val clipboardManager =
                view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", message.name)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(view.context, "Text is copied", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        val speak = customView.findViewById<LinearLayout>(R.id.speak)
        speak.visibility = View.GONE

        val share = customView.findViewById<LinearLayout>(R.id.share)
        share.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, message.name)
            sendIntent.type = "text/plain"

            val shareIntent = Intent.createChooser(sendIntent, null)
            view.context.startActivity(shareIntent)
            popupWindow.dismiss()
        }

        val delete = customView.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            val customDialog =
                CustomDialog(view.context, object : CustomDialog.OnButtonClickListeners {
                    override fun onYesClick() {
                        itemClick.deleteItem(message, position)
                    }
                })
            customDialog.setTextValue(
                "Are you sure you want to delete this thread?",
                "Delete a thread"
            )
            customDialog.show()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(view)
    }

    interface ItemOnClickListener {
        fun deleteItem(module: ThreadModule, position: Int)
        fun onItemClick(module: ThreadModule)
    }

    class MyDiffUtils : DiffUtil.ItemCallback<ThreadModule>() {
        override fun areItemsTheSame(oldItem: ThreadModule, newItem: ThreadModule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ThreadModule, newItem: ThreadModule): Boolean {
            return oldItem == newItem
        }

    }


}