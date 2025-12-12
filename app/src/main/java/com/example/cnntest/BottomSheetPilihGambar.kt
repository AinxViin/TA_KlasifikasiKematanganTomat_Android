package com.example.cnntest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetPilihGambar : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_bottom_sheet_pilih_gambar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(RESULT_SOURCE to SOURCE_GALLERY))
            dismiss()
        }
        view.findViewById<View>(R.id.btnCamera).setOnClickListener {
            parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(RESULT_SOURCE to SOURCE_CAMERA))
            dismiss()
        }
    }

    companion object {
        const val RESULT_KEY = "pick_source_result"
        const val RESULT_SOURCE = "source"
        const val SOURCE_GALLERY = "gallery"
        const val SOURCE_CAMERA = "camera"
    }
}