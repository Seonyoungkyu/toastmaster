package com.ykseon.toastmaster.ui.timer

import android.R
import android.R.attr.data
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.ykseon.toastmaster.common.DEBATE_ROLE
import com.ykseon.toastmaster.common.EVALUATOR_ROLE
import com.ykseon.toastmaster.common.SPEAKER_ROLE
import com.ykseon.toastmaster.common.TABLE_TOPIC_ROLE
import com.ykseon.toastmaster.databinding.CustomTimerInputDialogBinding


class CustomTimerDialog(
    confirmDialogInterface: CustomTimerDialogCallback,
) : DialogFragment() {

    // 뷰 바인딩 정의
    private var _binding: CustomTimerInputDialogBinding? = null
    private val binding get() = _binding!!

    private var confirmDialogInterface: CustomTimerDialogCallback? = null

    init {
        this.confirmDialogInterface = confirmDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomTimerInputDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.spinner1.setSelection(0)
        binding.spinner2.setSelection(0)
        binding.spinner3.setSelection(0)

        val roles = listOf(SPEAKER_ROLE, EVALUATOR_ROLE, TABLE_TOPIC_ROLE, DEBATE_ROLE)
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, roles)

        binding.roleEdit.setAdapter(adapter)

        binding.cancelButtonButton.setOnClickListener {
            dismiss()
        }

        // 확인 버튼 클릭
        binding.okButton.setOnClickListener {
            if (binding.spinner1.selectedItem.toString() == "Unspecified" ||
                binding.spinner3.selectedItem.toString() == "Unspecified") {
                Toast.makeText(binding.root.context, "Put right time to create timer",Toast.LENGTH_LONG).show()
            }
            else if ( binding.spinner2.selectedItem.toString() == "Unspecified") {
                this.confirmDialogInterface?.onYesButtonClick(
                    binding.roleEdit.text.toString(),
                    binding.spinner1.selectedItem.extractMinute() + "-" +
                            binding.spinner3.selectedItem.extractMinute()
                )
            }
            else
            {
                this.confirmDialogInterface?.onYesButtonClick(
                    binding.roleEdit.text.toString(),
                    binding.spinner1.selectedItem.extractMinute() + "-" +
                            binding.spinner2.selectedItem.extractMinute() + "-" +
                            binding.spinner3.selectedItem.extractMinute()
                )
            }
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Any.extractMinute() =
        (this as String).split(' ')[0]

}

interface CustomTimerDialogCallback {
    fun onYesButtonClick(role: String, cutOffs: String)
}