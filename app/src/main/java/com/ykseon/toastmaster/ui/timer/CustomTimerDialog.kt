package com.ykseon.toastmaster.ui.timer

import android.R
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.core.view.doOnLayout
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
    var initialTimerItem: TimerItem? = null

    init {
        this.confirmDialogInterface = confirmDialogInterface
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
    private fun Spinner.selectString(value: String): Boolean {
        for(i in 0 until adapter.count) {
            if (value == adapter.getItem(i))    {
                setSelection(i)
                return true
            }
        }

        return false
    }
    private fun setInitialContent(item: TimerItem) {
        val cutoffList = item.cutoffs.split('-')
        var first = "Unspecified"
        var second = "Unspecified"
        var third = "Unspecified"

        if (cutoffList.size == 2) {
            first = cutoffList[0] + " min"
            third = cutoffList[1] + " min"
            second = "Unspecified"
        } else if (cutoffList.size == 3) {
            first = cutoffList[0] + " min"
            second = cutoffList[1] + " min"
            third = cutoffList[2] + " min"
        }

        binding.spinner1.selectString(first)
        binding.spinner2.selectString(second)
        binding.spinner3.selectString(third)
        binding.roleSpinner.selectString(item.role)
        binding.nameEdit.setText(item.name)
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
        binding.roleSpinner.setSelection(0)

        val roles = listOf(SPEAKER_ROLE, EVALUATOR_ROLE, TABLE_TOPIC_ROLE, DEBATE_ROLE)
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, roles)

        binding.nameEdit.setAdapter(adapter)

        // imeOptions 속성에 FLAG_NO_ENTER_ACTION 속성을 제거합니다.
        binding.nameEdit.imeOptions = 0

        // onEditorAction() 메서드를 재정의하고, ACTION_DONE 액션을 처리합니다.
        binding.nameEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 키보드를 내립니다.
                val inputMethodManager = context?.getSystemService<InputMethodManager>()
                inputMethodManager?.hideSoftInputFromWindow(binding.nameEdit.windowToken, 0)
            }
            false
        }

        initialTimerItem?.let { setInitialContent(it) }


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
                    TimerItem(
                    binding.roleSpinner.selectedItem as String,
                        binding.nameEdit.text.generateName(),
                    binding.spinner1.selectedItem.extractMinute() + "-" +
                            binding.spinner3.selectedItem.extractMinute()
                    )
                )
            }
            else
            {
                this.confirmDialogInterface?.onYesButtonClick(
                    TimerItem(
                        binding.roleSpinner.selectedItem as String,
                        binding.nameEdit.text.generateName(),
                    binding.spinner1.selectedItem.extractMinute() + "-" +
                            binding.spinner2.selectedItem.extractMinute() + "-" +
                            binding.spinner3.selectedItem.extractMinute()
                    )
                )
            }
            dismiss()
        }


        view.doOnLayout {
            binding.spinner1.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val value = binding.spinner1.selectedItem.toString()

                    if (value == "Unspecified") {
                        // do nothing
                    }
                    else if (value == "1 min" )
                    {
                        binding.spinner2.selectString("Unspecified")
                        binding.spinner3.selectString("2 min")
                    }
                    else if (value == "2 min")
                    {
                        binding.spinner2.selectString("Unspecified")
                        binding.spinner3.selectString("3 min")
                    }
                    else {
                        val min = value.split(' ')[0].toInt()
                        val string2 = "${min+1} min"
                        val string3 = "${min+2} min"
                        if (!binding.spinner2.selectString(string2)) {
                            binding.spinner2.selectString(value)
                        }
                        if (!binding.spinner3.selectString(string3)) {
                            binding.spinner3.selectString(value)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        return view
    }

    private fun Editable.generateName() = if (this.toString() == "") "Anonymous" else this.toString()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Any.extractMinute() =
        (this as String).split(' ')[0]

}

interface CustomTimerDialogCallback {
    fun onYesButtonClick(item: TimerItem)
}