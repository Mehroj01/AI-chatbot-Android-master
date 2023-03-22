package com.chat.aichatbot.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.chat.aichatbot.R
import com.chat.aichatbot.adapters.StaticRecycleAdapter
import com.chat.aichatbot.databinding.FragmentHomeBinding
import com.chat.aichatbot.models.Message
import com.chat.aichatbot.models.StaticModule
import com.chat.aichatbot.room.AppDatabase
import com.chat.aichatbot.room.ThreadModule
import com.chat.aichatbot.utils.Constants
import com.chat.aichatbot.utils.MySharedPreference
import com.chat.aichatbot.viewmodels.MainViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentHomeBinding? = null
    private lateinit var mySharedPreference: MySharedPreference
    private lateinit var appDatabase: AppDatabase
    private lateinit var viewModel: MainViewModel
    private lateinit var staticRecycleAdapter: StaticRecycleAdapter
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getInstance(requireContext())
        mySharedPreference = MySharedPreference(requireContext())
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val entered = mySharedPreference.getPreferences("entered")
        if (entered != "") {
            skipTheFragment()
        }


        staticRecycleAdapter = StaticRecycleAdapter(Constants.arrayList,
            object : StaticRecycleAdapter.ItemOnClickListener {
                override fun onItemClick(module: StaticModule, position: Int) {
                    val thread = ThreadModule(name = "Thread 1")
                    appDatabase.threadDao().insertMessage(thread)
                    val list = appDatabase.threadDao().getAllThreads()

                    val bundle = Bundle()
                    bundle.putString("message", module.arrayList[position])
                    bundle.putSerializable("thread", list[list.size-1])
                    findNavController().navigate(R.id.threadFragment, bundle)
                }

            })
        binding.apply {

            recycleStatic.adapter = staticRecycleAdapter
            startChat.setOnClickListener {
                val thread = ThreadModule(name = "Thread 1")
                appDatabase.threadDao().insertMessage(thread)

                val list = appDatabase.threadDao().getAllThreads()

                val bundle = Bundle()
                bundle.putSerializable("thread", list[list.size-1])
                findNavController().navigate(R.id.threadFragment,bundle)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun skipTheFragment() {
        val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        findNavController().navigate(
            R.id.action_homeFragment_to_mainFragment,
            bundleOf(), navOptions
        );
    }


}