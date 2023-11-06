package com.example.teachjr.ui.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.example.teachjr.R
import com.example.teachjr.databinding.BaseBottomSheetBinding
import com.google.android.material.R.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var baseBinding: BaseBottomSheetBinding
    private var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null

    fun hideCrossButton() {
        baseBinding.fabClose.isVisible = false
    }

    fun showCrossButton() {
        baseBinding.fabClose.isVisible = true
    }

    fun setContentHeight(shouldMatchParent: Boolean) {
        baseBinding.cvParent.updateLayoutParams<LinearLayout.LayoutParams> {
            this.height = if (shouldMatchParent) {
                LinearLayout.LayoutParams.MATCH_PARENT
            } else LinearLayout.LayoutParams.WRAP_CONTENT
        }
    }

    var isDraggable = true
        set(value) {
            field = value
            bottomSheetBehavior?.isDraggable = isDraggable
            bottomSheetBehavior?.isHideable = isDraggable
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null) {
            dialog!!.setOnShowListener { dialogInterface: DialogInterface? ->
                if (dialog !is BottomSheetDialog) return@setOnShowListener
                val bottomSheetDialog = dialog as BottomSheetDialog? ?: return@setOnShowListener
                val bottomSheet =
                    bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                        ?: return@setOnShowListener

                bottomSheetBehavior =
                    BottomSheetBehavior.from(bottomSheet)

                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior?.skipCollapsed = true
                bottomSheetBehavior?.isDraggable = isDraggable
                bottomSheetBehavior?.isFitToContents = false
                bottomSheetBehavior?.isHideable = isDraggable

                onBottomSheetBehavior(dialog as? BottomSheetDialog, bottomSheetBehavior)
            }
        }
        baseBinding = BaseBottomSheetBinding.inflate(layoutInflater)
        baseBinding.cvParent.addView(onCreateViewBinding().root)

        baseBinding.root.setOnClickListener {
            if (isCancelable) {
                dismiss()
            }
        }
        baseBinding.llContainer.gravity = Gravity.BOTTOM
        return baseBinding.root
    }

    abstract fun onBottomSheetBehavior(dialog: BottomSheetDialog?, bottomSheetBehavior: BottomSheetBehavior<FrameLayout>?)

    abstract fun onCreateViewBinding(): ViewBinding

    override fun getTheme(): Int {
        return R.style.BaseBottomSheetDialogTheme
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.setReorderingAllowed(true)
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }
}