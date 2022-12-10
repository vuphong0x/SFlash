package com.phongvv.sflash

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phongvv.sflash.databinding.DialogNoFlashlightBinding

class NoFlashlightDialog : BottomSheetDialogFragment() {
    private lateinit var binding: DialogNoFlashlightBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNoFlashlightBinding.inflate(requireActivity().layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(binding.root)
        binding.containerUseScreen.setOnClickListener { dismiss() }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult(NO_FLASH_DIALOG_DISMISSED, Bundle())
    }

    companion object {
        const val NO_FLASH_DIALOG_DISMISSED = "no_flash_dialog_dismissed"
    }
}