package com.chat.aichatbot.adapters


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chat.aichatbot.R
import com.chat.aichatbot.databinding.MessageItemBinding
import com.chat.aichatbot.databinding.OutOfMessagesItemBinding
import com.chat.aichatbot.models.MessageModule
import com.chat.aichatbot.room.AppDatabase
import com.chat.aichatbot.utils.Constants
import com.chat.aichatbot.utils.CustomDialog
import com.chat.aichatbot.utils.MySharedPreference
import com.chat.aichatbot.utils.TextSizeValue
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet


class MessageAdapter(
    val list: ArrayList<MessageModule>,
    private val itemClickListener: ItemOnClickListener, private val context: Context
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var mySharePreference: MySharedPreference = MySharedPreference(context)

    class ViewHolder(var binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            MessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        when (viewType) {
            USER_MESSAGE -> {
                binding.relativeLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        viewGroup.context,
                        R.color.user_color
                    )
                )
                binding.contextualMenu.visibility = View.INVISIBLE
            }
            AI_MESSAGE -> {
                binding.relativeLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        viewGroup.context,
                        android.R.color.transparent
                    )
                )
                binding.contextualMenu.visibility = View.VISIBLE
            }
            OUT_OF_MESSAGES -> {
                val binding1 =
                    OutOfMessagesItemBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )

                return ViewHolder(binding1)
            }
        }


        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[holder.adapterPosition].id != -1) {
            val binding = holder.binding as MessageItemBinding
            val module = list[position]
            binding.question.text = module.message
            val size = mySharePreference.getPreferences(Constants.TV_SIZE)
            if (size != "") {
                TextSizeValue.setSizeToTV(
                    size!!.toFloat(), binding.question
                )
            }
            binding.contextualMenu.setOnClickListener {
                showPopUpIcon(it, module, holder.adapterPosition)
            }
        } else {
            val binding = holder.binding as OutOfMessagesItemBinding
            binding.apply {
                startTrial.setOnClickListener {
                    itemClickListener.navigate()
                }
            }
            displayTheText(binding.question, holder.itemView.context, binding.startTrial)
        }


    }

    private fun showPopUpIcon(view: View, message: MessageModule, position: Int) {
        val userMessage = getUserMessage(position)
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
            if (userMessage != null) {
                val clipboardManager =
                    view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData =
                    ClipData.newPlainText("label", message.message + "\n" + userMessage.message)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(view.context, "Text is copied", Toast.LENGTH_SHORT).show()
                popupWindow.dismiss()
            } else {
                val clipboardManager =
                    view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", message.message)
                clipboardManager.setPrimaryClip(clipData)
                popupWindow.dismiss()
            }

        }

        val speak = customView.findViewById<LinearLayout>(R.id.speak)
        speak.setOnClickListener {
            if (userMessage != null) {
                val newMessage = message
                newMessage.message = userMessage.message + message.message
                itemClickListener.speak(newMessage)
            } else {
                itemClickListener.speak(message)
            }
            popupWindow.dismiss()

        }


        val share = customView.findViewById<LinearLayout>(R.id.share)
        share.setOnClickListener {
            if (userMessage != null) {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, userMessage.message + "\n" + message.message)
                sendIntent.type = "text/plain"

                val shareIntent = Intent.createChooser(sendIntent, null)
                view.context.startActivity(shareIntent)
            } else {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, message.message)
                sendIntent.type = "text/plain"

                val shareIntent = Intent.createChooser(sendIntent, null)
                view.context.startActivity(shareIntent)
            }

            popupWindow.dismiss()
        }

        val delete = customView.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            val customDialog =
                CustomDialog(view.context, object : CustomDialog.OnButtonClickListeners {
                    override fun onYesClick() {
                        if (userMessage != null) {
                            itemClickListener.deleteItem(message, userMessage, position)
                        } else {
                            itemClickListener.deleteItem(position = position, message = message)
                        }

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

    private fun getUserMessage(position: Int): MessageModule? {
        return if (position - 1 > 0) {
            if (list[position - 1].isUser) {
                list[position - 1]
            } else {
                null
            }
        } else {
            null
        }

    }

    fun setOutOfMessages(message: MessageModule, recyclerView: RecyclerView) {
        list.add(message)
        notifyItemInserted(list.size)
        recyclerView.scrollToPosition(this.itemCount - 1)
    }


    override fun getItemViewType(position: Int): Int {
        if (list[position].id == -1) {
            return OUT_OF_MESSAGES
        }
        return if (list[position].isUser) {
            USER_MESSAGE
        } else {
            AI_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun insertData(message: MessageModule, recyclerView: RecyclerView) {
        list.add(message)
        this.notifyItemInserted(list.size)
        recyclerView.scrollToPosition(this.itemCount - 1)
    }

    fun setList(messagesList: List<MessageModule>) {
        list.clear()
        list.addAll(messagesList)
        notifyDataSetChanged()
    }

    fun insertUserData(message: MessageModule, messageRv: RecyclerView) {
        list.add(message)
        this.notifyItemInserted(list.size)
        messageRv.scrollToPosition(this.itemCount - 1)
    }

    companion object {
        private const val USER_MESSAGE = 0
        private const val AI_MESSAGE = 1
        private const val OUT_OF_MESSAGES = 2
    }


    interface ItemOnClickListener {
        fun speak(module: MessageModule)
        fun deleteItem(message: MessageModule, aiMessage: MessageModule? = null, position: Int)

        fun navigate()
    }

    private fun displayTheText(textView: TextView, context: Context, linearLayout: LinearLayout) {
        val text = context.getText(R.string.out_of_free_messages)

        var index = 0
        val handler = Handler(Looper.getMainLooper())

        val characterAdder = object : Runnable {
            override fun run() {
                textView.text = text.subSequence(0, ++index)
                if (index < text.length) {
                    handler.postDelayed(this, 30)
                }
            }
        }

        handler.postDelayed(characterAdder, 0)
        Handler(Looper.getMainLooper()).postDelayed({
            linearLayout.visibility = View.VISIBLE
        }, 320 * 30)
    }
}

