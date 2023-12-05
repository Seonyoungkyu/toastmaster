package com.ykseon.toastmaster.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.databinding.FragmentTimerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val timerFragmentViewModel by viewModels<TimerFragmentViewModel>()
    private val timerListAdapter by lazy {
        TimerListAdapter(timerFragmentViewModel)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        binding.viewModel = timerFragmentViewModel
        binding.lifecycleOwner = this

        binding.recyclerView.layoutManager = GridLayoutManager(context,3)
        binding.recyclerView.adapter = timerListAdapter

        timerListAdapter.submitList(timerList)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}