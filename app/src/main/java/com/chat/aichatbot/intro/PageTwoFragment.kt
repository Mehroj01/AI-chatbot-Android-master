package com.chat.aichatbot.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chat.aichatbot.R

/**
 * A simple [Fragment] subclass.
 * Use the [PageTwoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PageTwoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page_two, container, false)
    }

}