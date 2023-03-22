package com.chat.aichatbot.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.chat.aichatbot.R
import com.chat.aichatbot.adapters.CustomSpinnerAdapter
import com.chat.aichatbot.adapters.ThreadAdapter
import com.chat.aichatbot.databinding.FragmentMainBinding
import com.chat.aichatbot.room.AppDatabase
import com.chat.aichatbot.room.ThreadModule
import com.chat.aichatbot.utils.Constants
import com.chat.aichatbot.utils.CustomDialog
import com.chat.aichatbot.utils.MySharedPreference
import com.chat.aichatbot.utils.TextSizeValue
import com.chat.aichatbot.utils.TextSizeValue.floatSizes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentMainBinding? = null
    private lateinit var spinnerAdapter: CustomSpinnerAdapter
    private lateinit var mySharedPreference: MySharedPreference
    private var isSearchBar: Boolean = false
    private lateinit var threadAdapter: ThreadAdapter
    private lateinit var threadsList: ArrayList<ThreadModule>
    private lateinit var appDatabase: AppDatabase
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        mySharedPreference = MySharedPreference(requireContext())
        spinnerAdapter = CustomSpinnerAdapter(requireContext(), TextSizeValue.sizeInDesign())
        appDatabase = AppDatabase.getInstance(requireContext())
        threadsList = ArrayList(appDatabase.threadDao().getAllThreads())
        threadAdapter = ThreadAdapter(
            object : ThreadAdapter.ItemOnClickListener {
                override fun deleteItem(module: ThreadModule, position: Int) {
                    appDatabase.threadDao().deleteThread(module)
                    threadsList.remove(module)
                    threadAdapter.let {
                        it.submitList(threadsList.toMutableList())
                        it.notifyDataSetChanged()
                    }
                    appDatabase.messageDao().deleteThreadMessages(module.id)
                }

                override fun onItemClick(module: ThreadModule) {
                    val bundle = Bundle()
                    bundle.putSerializable("thread", module)
                    findNavController().navigate(R.id.action_mainFragment_to_threadFragment, bundle)
                }

            })

        threadAdapter.submitList(threadsList)
        binding.apply {

            settings.setOnClickListener {
                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.settings_bottom_sheet, null)
                dialog.setContentView(view)
                dialog.setCancelable(false)
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
                    spinnerView.setSelection(floatSizes().indexOf(selectedSize!!.toFloat()))
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
                                dialog.dismiss()
                                clearAllHistory()
                                skipTheFragment()
                            }
                        })
                    customDialog.setTextValue(
                        "Are you sure you want to clear your history?",
                        "Clear History"
                    )
                    customDialog.show()
                    dialog.dismiss()
                }

                shareApp.setOnClickListener {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.setType("text/plain")
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app!")
                    startActivity(Intent.createChooser(shareIntent, "Share app using"))
                }

                switch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        mySharedPreference.setPreferences(Constants.CHECKED, "true")
                    } else {
                        mySharedPreference.setPreferences(Constants.CHECKED, "false")
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
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Handle touch event when user starts sliding the thumb
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Handle touch event when user stops sliding the thumb
                    }
                })

                back.setOnClickListener {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Your settings is saved", Toast.LENGTH_SHORT).show()
                }

                spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
                    OnItemClickListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        mySharedPreference.setPreferences(
                            Constants.TV_SIZE,
                            floatSizes()[position].toString()
                        )
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

            searchBar.setOnClickListener {
                if (!isSearchBar) {
                    searchIcon.setImageResource(R.drawable.x_icon)
                    searchEdit.visibility = View.VISIBLE
                    searchEdit.hint = "Search..."
                    menuBar.visibility = View.VISIBLE
                    searchEdit.requestFocus()
                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT)
                    menuBar.setOnClickListener {
                        val imm =
                            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                        searchEdit.setText("")
                        isSearchBar = false
                        searchIcon.setImageResource(R.drawable.search_icon)
                        searchEdit.visibility = View.INVISIBLE
                        menuBar.visibility = View.GONE
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
                                    threadAdapter.submitList(threadsList)
                                }
                            }

                        }

                        override fun afterTextChanged(s: Editable?) {

                        }

                    })
                    isSearchBar = true
                } else {
                    searchEdit.setText("")
                }

            }

            threadRv.adapter = threadAdapter

            startChat.setOnClickListener {
                val list1 = appDatabase.threadDao().getAllThreads()
                if (list1.isEmpty()) {
                    appDatabase.threadDao().insertMessage(
                        ThreadModule(
                            name = "Thread 1"
                        )
                    )
                    val bundle = Bundle()
                    bundle.putSerializable(
                        "thread",
                        appDatabase.threadDao().getAllThreads()[list1.size]
                    )
                    findNavController().navigate(R.id.action_mainFragment_to_threadFragment, bundle)
                } else {
                    appDatabase.threadDao().insertMessage(
                        ThreadModule(
                            name = "Thread ${
                                list1.size + 1
                            }"

                        )
                    )
                    val bundle = Bundle()
                    bundle.putSerializable(
                        "thread",
                        appDatabase.threadDao().getAllThreads()[list1.size]
                    )
                    findNavController().navigate(R.id.action_mainFragment_to_threadFragment, bundle)
                }
            }
        }


        return binding.root
    }

    private fun clearAllHistory() {
        val cacheDir = requireActivity().applicationContext.cacheDir
        cacheDir.deleteRecursively()
        val sharedPreferences =
            requireActivity().applicationContext.getSharedPreferences(
                "have",
                Context.MODE_PRIVATE
            )
        sharedPreferences.edit().clear().apply()
        appDatabase.clearAllTables()
    }

    private fun filterByText(filterKey: String) {
        val threadList = LinkedHashSet<ThreadModule>()
        for (i in threadsList.indices) {
            if (!threadsList[i].name.lowercase(Locale.ROOT)
                    .contains(filterKey.lowercase(Locale.getDefault()))
            ) {
                val messages = appDatabase.messageDao().getThreadMessages(threadsList[i].id)
                for (k in messages.indices) {
                    if (messages[k].message.lowercase().contains(filterKey.lowercase())) {
                        threadList.add(threadsList[i])
                    }
                }
            } else {
                threadList.add(threadsList[i])
            }
        }
        threadAdapter.submitList(kotlin.collections.ArrayList(threadList))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val newThreadList = appDatabase.threadDao().getAllThreads()
        threadAdapter.submitList(newThreadList)
        threadAdapter.notifyDataSetChanged()
    }

    private fun skipTheFragment() {
        val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(R.id.mainFragment, true)
            .build()
        findNavController().navigate(
            R.id.action_mainFragment_to_homeFragment,
            bundleOf(), navOptions
        )
    }

    override fun onPause() {
        super.onPause()
        isSearchBar = false
    }
}