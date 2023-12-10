package com.ykseon.toastmaster.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
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

        binding.spinner1.setSelection(2)
        binding.spinner2.setSelection(3)
        binding.spinner3.setSelection(4)

        // 취소 버튼 클릭
        binding.cancelButtonButton.setOnClickListener {
            dismiss()
        }

        // 확인 버튼 클릭
        binding.okButton.setOnClickListener {
            this.confirmDialogInterface?.onYesButtonClick( binding.roleEdit.text.toString(),
                binding.spinner1.selectedItem.extractMinute() + "-" +
                        binding.spinner2.selectedItem.extractMinute() + "-" +
                        binding.spinner3.selectedItem.extractMinute()
            )
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