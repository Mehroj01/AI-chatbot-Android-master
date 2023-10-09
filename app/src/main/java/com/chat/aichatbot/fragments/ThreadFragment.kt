package com.chat.aichatbot.fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.chat.aichatbot.R
import com.chat.aichatbot.adapters.CustomSpinnerAdapter
import com.chat.aichatbot.adapters.MessageAdapter
import com.chat.aichatbot.databinding.FragmentThreadBinding
import com.chat.aichatbot.models.Message
import com.chat.aichatbot.models.MessageModule
import com.chat.aichatbot.models.MessageX
import com.chat.aichatbot.models.RequestBody
import com.chat.aichatbot.room.AppDatabase
import com.chat.aichatbot.room.ThreadModule
import com.chat.aichatbot.utils.*
import com.chat.aichatbot.viewmodels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.Signature
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

class ThreadFragment : Fragment(R.layout.fragment_thread) {

    private var isSpeaking = false
    private var messageSpeech: String? = null
    private var textToSpeech: TextToSpeech? = null
    private lateinit var networkHelper: NetworkHelper
    private var threadModule: ThreadModule? = null
    private var message: String? = null
    private var isSearchBar: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            threadModule = it.getSerializable("thread") as ThreadModule
            message = it.getString("message")
        }
    }

    private var _binding: FragmentThreadBinding? = null
    private lateinit var spinnerAdapter: CustomSpinnerAdapter
    private lateinit var messagesList: LinkedHashSet<MessageModule>
    private lateinit var appDatabase: AppDatabase
    private var actualProgress: Float = 1.0f
    private lateinit var viewModel: MainViewModel
    private lateinit var mySharedPreference: MySharedPreference
    private lateinit var messageAdapter: MessageAdapter
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThreadBinding.inflate(inflater, container, false)

        appDatabase = AppDatabase.getInstance(requireContext())
        spinnerAdapter = CustomSpinnerAdapter(requireContext(), TextSizeValue.sizeInDesign())
        mySharedPreference = MySharedPreference(requireContext())

        val progress = mySharedPreference.getPreferences(Constants.SLIDER_PROGRESS)
        if (progress != "") {
            actualProgress = (progress!!.toFloat()) / 100
        }
        networkHelper = NetworkHelper(requireContext())
        if (threadModule == null) {
            threadModule = ThreadModule(id = 1, name = "Thread 1")
        }
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        messagesList = LinkedHashSet(appDatabase.messageDao().getThreadMessages(threadModule!!.id))
        messageAdapter =
            MessageAdapter(ArrayList(messagesList), object : MessageAdapter.ItemOnClickListener {
                override fun speak(module: MessageModule) {
                    val checked = mySharedPreference.getPreferences(Constants.CHECKED)
                    if (checked != "") {
                        if (checked.toBoolean()) {
                            messageSpeech = module.message
                            speakFun(module.message, actualProgress)
                        }
                    }

                }

                override fun deleteItem(
                    message: MessageModule,
                    aiMessage: MessageModule?,
                    position: Int
                ) {
                    if (aiMessage == null) {
                        appDatabase.messageDao().deleteMessage(message)
                        messageAdapter.list.remove(message)
                        messageAdapter.notifyItemRemoved(position)
                        messageAdapter.notifyItemRangeChanged(position, messageAdapter.list.size)
                    } else {
                        appDatabase.messageDao().deleteMessage(message)
                        appDatabase.messageDao().deleteMessage(aiMessage)
                        messageAdapter.list.remove(aiMessage)
                        messageAdapter.list.remove(message)
                        messageAdapter.notifyDataSetChanged()
                    }
                }

                override fun navigate() {
                    findNavController().navigate(R.id.trialFragment)
                }

            }, requireContext())



        if (message != null) {
            updateEverything(message!!, true)
            requestTheApi(message!!)
        }

        binding.apply {
            val boolean = mySharedPreference.getBoolPreference(Constants.PROMO_ACTIVE)
            if (boolean) {
                binding.promoOffer.visibility = View.GONE
            } else {
                val count = mySharedPreference.getIntPreference(Constants.FREE_MESSAGES_COUNT)
                if (count != -1) {
                    viewModel.setTheCount(count)
                } else {
                    viewModel.setTheCount(10)
                }

                viewModel.freeMessageCount.observe(viewLifecycleOwner) {
                    if (it >= 0) {
                        mySharedPreference.setIntPreference(Constants.FREE_MESSAGES_COUNT, it)
                    }
                    if (it >= 0) {
                        if (it == 0) {
                            messagesLeft.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.holo_red_light
                                )
                            )
                        }
                        messagesLeft.text = "You have free $it messages left"
                    } else {
                        messageAdapter.setOutOfMessages(
                            MessageModule(-1, -1, false, ""),
                            binding.messageRv
                        )
                    }
                }
            }

            settings.setOnClickListener {
                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.settings_bottom_sheet, null)
                dialog.setContentView(view)
                dialog.setCancelable(true)
                val back = view.findViewById<ImageView>(R.id.back)
                val spinnerView = view.findViewById<Spinner>(R.id.sizesSpinner)
                val slider = view.findViewById<SeekBar>(R.id.speedSeekBar)
                val switch = view.findViewById<SwitchCompat>(R.id.soundSwitch)
                val shareApp = view.findViewById<TextView>(R.id.shareApp)
                val clearHistory = view.findViewById<TextView>(R.id.clearHistory)
                spinnerView.adapter = spinnerAdapter
                val selectedSize = mySharedPreference.getPreferences(Constants.TV_SIZE)
                val sliderProgress = mySharedPreference.getPreferences(Constants.SLIDER_PROGRESS)
                val checked = mySharedPreference.getPreferences(Constants.CHECKED)
                if (selectedSize.toString() != "") {
                    spinnerView.setSelection(
                        TextSizeValue.floatSizes().indexOf(selectedSize!!.toFloat())
                    )
                } else {
                    spinnerView.setSelection(2)
                }
                if (sliderProgress.toString() == "") {
                    slider.progress = 100
                } else {
                    slider.progress = sliderProgress!!.toInt()
                }
                if (checked != "") {
                    switch.isChecked = checked.toBoolean()
                } else {
                    switch.isChecked = true
                }

                clearHistory.setOnClickListener {
                    val customDialog =
                        CustomDialog(view.context, object : CustomDialog.OnButtonClickListeners {
                            override fun onYesClick() {
                                appDatabase.messageDao().deleteThreadMessages(threadModule!!.id)
                                messageAdapter.setList(
                                    appDatabase.messageDao().getThreadMessages(threadModule!!.id)
                                )
                                dialog.dismiss()
                            }
                        })
                    customDialog.setTextValue(
                        "Are you sure you want to clear this item?",
                        "Delete an Item"
                    )
                    customDialog.show()
                }

                shareApp.setOnClickListener {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app!")
                    startActivity(Intent.createChooser(shareIntent, "Share app using"))
                }

                switch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        mySharedPreference.setPreferences(Constants.CHECKED, "true")
                    } else {
                        mySharedPreference.setPreferences(Constants.CHECKED, "false")
                        if (textToSpeech != null) {
                            if (textToSpeech!!.isSpeaking) {
                                textToSpeech!!.stop()
                            }
                        }
                    }
                }
                slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        mySharedPreference.setPreferences(
                            Constants.SLIDER_PROGRESS,
                            progress.toString()
                        )

                        actualProgress = progress.toFloat() / 100
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }
                })

                back.setOnClickListener {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Your settings is saved", Toast.LENGTH_SHORT)
                        .show()
                    if (textToSpeech != null) {
                        if (textToSpeech!!.isSpeaking) {
                            speakFun(messageSpeech!!, actualProgress)
                        }
                    }


                }


                spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
                    AdapterView.OnItemClickListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        mySharedPreference.setPreferences(
                            Constants.TV_SIZE,
                            TextSizeValue.floatSizes()[position].toString()
                        )
                        messageAdapter.notifyDataSetChanged()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Handle case where nothing is selected
                    }

                    override fun onItemClick(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                    }
                }

                dialog.show()
            }

            searchIconFr.setOnClickListener {
                searchBar.visibility = View.VISIBLE
                searchIcon.setImageResource(R.drawable.x_icon)
                searchEdit.visibility = View.VISIBLE
                searchEdit.hint = "Search..."
                settings.visibility = View.INVISIBLE
                menuBar.visibility = View.VISIBLE
                back.visibility = View.INVISIBLE
                searchEdit.requestFocus()
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(
                    searchEdit,
                    InputMethodManager.SHOW_IMPLICIT
                )
                menuBar.setOnClickListener {
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                    searchEdit.setText("")
                    isSearchBar = false
                    searchIcon.setImageResource(R.drawable.search_icon)
                    searchEdit.visibility = View.INVISIBLE
                    menuBar.visibility = View.GONE
                    settings.visibility = View.VISIBLE
                    back.visibility = View.VISIBLE
                    searchBar.visibility = View.GONE
                }

                searchIcon.setOnClickListener {
                    searchEdit.setText("")
                    isSearchBar = false
                }

                searchEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s != null) {
                            if (s.toString() != "") {
                                filterByText(s.toString())
                            } else {
                                messageAdapter.setList(
                                    appDatabase.messageDao()
                                        .getThreadMessages(threadModule!!.id)
                                )
                            }
                        }

                    }

                    override fun afterTextChanged(s: Editable?) {

                    }

                })
                isSearchBar = true

            }

            viewModel.chatResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is EventLoader.Success -> {
                        messageSpeech = it.data.choices[0].message.content
                        val checked = mySharedPreference.getPreferences(Constants.CHECKED)
                        updateEverything(it.data.choices[0].message.content, false)
                        if (checked != "") {
                            if (checked.toBoolean()) {
                                speakFun(
                                    it.data.choices[0].message.content,
                                    actualProgress = actualProgress
                                )
                            }
                        } else {
                            speakFun(
                                it.data.choices[0].message.content,
                                actualProgress = actualProgress
                            )
                        }

                    }
                    is EventLoader.Error -> {
                        Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }
            }

            threadTv.text = threadModule!!.name

            back.setOnClickListener {
                findNavController().popBackStack()
            }

            messageRv.adapter = messageAdapter

            send.setOnClickListener {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                val editTextString = editText.text.toString()
                if (editTextString != "") {
                    updateEverything(editTextString, true)
                    if (viewModel.freeMessageCount.value == 0) {
                        viewModel.setTheCount(-1)
                    }
                    if (viewModel.freeMessageCount.value!! > 0) {

                        requestTheApi(editTextString)
                    }

                    editText.setText("")
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Request should not be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            goPro.setOnClickListener {
                findNavController().navigate(R.id.trialFragment)
            }

        }


        return binding.root
    }


    private fun filterByText(filterKey: String) {
        val newList = kotlin.collections.ArrayList<MessageModule>()
        val existingList = kotlin.collections.ArrayList(messagesList)
        for (i in messagesList.indices) {
            if (existingList[i].message.lowercase().contains(filterKey.lowercase())
            ) {
                newList.add(existingList[i])
            }
        }

        messageAdapter.setList(newList)

    }

    private fun updateEverything(string: String, isUser: Boolean) {
        if (networkHelper.isNetworkConnected()) {
            var string1 = string
            if (!isUser) {
                string1 = string.trim()
                messageAdapter.insertData(
                    MessageModule(
                        isUser = isUser,
                        message = string1,
                        chatId = threadModule!!.id
                    ), binding.messageRv
                )

            } else {
                messageAdapter.insertUserData(
                    MessageModule(
                        isUser = isUser,
                        message = string,
                        chatId = threadModule!!.id
                    ), binding.messageRv
                )
            }
            appDatabase.messageDao().insertMessage(
                MessageModule(
                    isUser = isUser,
                    message = string1,
                    chatId = threadModule!!.id
                )
            )
        }
    }


    private fun requestTheApi(message: String) {
        if (networkHelper.isNetworkConnected()) {
            viewModel.token.observe(requireActivity()) { token ->
                if (token != "") {
                    viewModel.isLoading.observe(requireActivity()) { isLoading ->
                        if (isLoading) {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.send.isEnabled = false
                        } else {
                            if (_binding != null) {
                                binding.progressBar.visibility = View.GONE
                                Handler(Looper.getMainLooper()).postDelayed({
                                    binding.send.isEnabled = true
                                }, 400)

                            }
                        }
                    }
                    viewModel.getChatResponse(
                        RequestBody(
                            model = "gpt-3.5-turbo",
                            messages = listOf(MessageX(content = message, role = "system"))
                        ), token
                    )

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Token is not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            viewModel.getTheKey()
        } else {
            Toast.makeText(requireContext(), "Please connect to the internet!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        val mySharedPreference = MySharedPreference(requireContext())
        val entered = mySharedPreference.getPreferences("entered")
        if (entered == "") {
            mySharedPreference.setPreferences("entered", "true")
        }

        if (message == null) {
            binding.editText.requestFocus()
            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        val messages = appDatabase.messageDao().getThreadMessages(threadModule!!.id)
        if (messages.isEmpty()) {
            appDatabase.threadDao().deleteThread(threadModule!!)
        }
    }

    fun speakFun(message: String, actualProgress: Float) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(requireActivity().applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech!!.language = Locale.US
                    textToSpeech!!.setSpeechRate(actualProgress)
                    textToSpeech!!.speak(message, TextToSpeech.QUEUE_ADD, null, null)
                }
            }
        } else {
            if (textToSpeech!!.isSpeaking) {
                textToSpeech!!.stop()
            }
            textToSpeech!!.setSpeechRate(actualProgress)
            textToSpeech!!.speak(message, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        isSearchBar = false
    }

    override fun onStop() {
        super.onStop()
        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            isSpeaking = false
        }
    }


}