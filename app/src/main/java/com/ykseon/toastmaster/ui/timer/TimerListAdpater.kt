package com.ykseon.toastmaster.ui.timer

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ykseon.toastmaster.databinding.TimerItemBinding
import androidx.recyclerview.widget.DiffUtil

class TimerListAdapter(
    private val viewModel:TimerFragmentViewModel,
    private val fragmentManager: FragmentManager?
) : ListAdapter<TimerItem, TimerListAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = TimerItemBinding.inflate(LayoutInflater.from(parent.context), null, false )
        binding.timerItem.setOnClickListener {

            if (binding.role.text.isEmpty() || binding.cutoffList.text.isEmpty()) {
                val dialog = CustomTimerDialog(object: CustomTimerDialogCallback {
                    override fun onYesButtonClick(role: String, cutOffs: String) {
                        viewModel.startTimer(
                            parent,
                            role,
                            cutOffs
                        )
                    }
                })
                dialog.show(fragmentManager!!, "Timer Input Dialog")
            }
            else {
                viewModel.startTimer(
                    parent,
                    binding.role.text.toString(),
                    binding.cutoffList.text.toString()
                )
            }
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
