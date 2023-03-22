package com.chat.aichatbot.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chat.aichatbot.R
import com.chat.aichatbot.databinding.StaticRecycleItemViewBinding
import com.chat.aichatbot.models.StaticModule

class StaticRecycleAdapter(
    private val list: ArrayList<StaticModule>,
    var onItemClickListener: ItemOnClickListener
) : RecyclerView.Adapter<StaticRecycleAdapter.ViewHolder>() {

    class ViewHolder(var binding: StaticRecycleItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    // Create new views (
    // invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            StaticRecycleItemViewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val module = list[position]
            questionsTV.text = module.title
            if (module.arrayList.size < 3) {
                yearsOld.visibility = View.GONE
                question1.text = module.arrayList[0]
                question2.text = module.arrayList[1]
            } else {
                question1.text = module.arrayList[0]
                question2.text = module.arrayList[1]
                question3.text = module.arrayList[2]
            }

            sixYearOld.setOnClickListener {
                arrow1.setImageResource(R.drawable.arrow_up)
                sixYearOld.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.bg_color
                    )
                )
                onItemClickListener.onItemClick(module, 0)
            }
            skyBlue.setOnClickListener {
                arrow2.setImageResource(R.drawable.arrow_up)
                skyBlue.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.bg_color
                    )
                )
                onItemClickListener.onItemClick(module, 1)
            }
            yearsOld.setOnClickListener {
                arrow3.setImageResource(R.drawable.arrow_up)
                yearsOld.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.bg_color
                    )
                )
                onItemClickListener.onItemClick(module, 2)
            }


        }
    }

    override fun getItemCount() = list.size

    interface ItemOnClickListener {
        fun onItemClick(module: StaticModule, position: Int)
    }
}