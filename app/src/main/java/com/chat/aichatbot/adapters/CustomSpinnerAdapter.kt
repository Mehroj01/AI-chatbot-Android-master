package com.chat.aichatbot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.chat.aichatbot.R

class CustomSpinnerAdapter(context: Context, private val values: List<String>) : BaseAdapter(),
    SpinnerAdapter {
    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): Any {
        return values[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view =
                LayoutInflater.from(parent!!.context).inflate(R.layout.spinner_item, parent, false)
        }
        val itemTextView = view!!.findViewById<TextView>(R.id.text1)
        itemTextView.text = values[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(parent!!.context)
                .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        }
        val itemTextView = view!!.findViewById<TextView>(android.R.id.text1)
        itemTextView.text = values[position]
        return view
    }
}

//val adapter = CustomSpinnerAdapter(this, listOf("Option 1", "Option 2", "Option 3"))
//spinner.adapter = adapter

