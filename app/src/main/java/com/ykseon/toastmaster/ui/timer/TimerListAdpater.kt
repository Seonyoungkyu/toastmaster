package com.ykseon.toastmaster.ui.timer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ykseon.toastmaster.databinding.TimerItemBinding
import androidx.recyclerview.widget.DiffUtil

class TimerListAdapter(
    private val viewModel:TimerFragmentViewModel
) : ListAdapter<TimerItem, TimerListAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = TimerItemBinding.inflate(LayoutInflater.from(parent.context), null, false )
        binding.timerItem.setOnClickListener {
            viewModel.buttonClick(parent, binding.role.text.toString(), binding.cutoffList.text.toString())
        }
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ItemViewHolder(private val binding: TimerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimerItem) {
            binding.role.text = item.role
            binding.cutoffList.text = item.cutoffs
        }
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<TimerItem>() {
        override fun areItemsTheSame(oldItem: TimerItem, newItem: TimerItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TimerItem, newItem: TimerItem): Boolean {
            return oldItem == newItem
        }
    }
}
