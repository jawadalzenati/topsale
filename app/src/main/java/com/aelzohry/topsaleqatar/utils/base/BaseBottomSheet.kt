package com.aelzohry.topsaleqatar.utils.base

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.aelzohry.topsaleqatar.BR
import com.aelzohry.topsaleqatar.utils.extenions.showLoading
import com.aelzohry.topsaleqatar.utils.extenions.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kaopiz.kprogresshud.KProgressHUD

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
abstract class BaseBottomSheet<DB : ViewDataBinding, VM : BaseViewModel> :
    BottomSheetDialogFragment() {

    protected lateinit var binding: DB
    protected lateinit var vm: VM
    private var loading: KProgressHUD? = null
    protected lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        performDataBinding(inflater, container)
        setupBaseLiveDataObserver()
        setupUI()
        onClickedListener()
        observerLiveData()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val d = super.onCreateDialog(savedInstanceState)
        d.setOnShowListener {
            var dialog = dialog as BottomSheetDialog
            var bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        dismiss()
                    }
                }

            })

            if (isFullHeight()) setupFullHeight(dialog)
        }
        return d
    }


    protected fun observerNewHeight() {
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            var dialog = dialog as BottomSheetDialog
            var bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.setPeekHeight(0)
        }
    }


    /**
     * @return layout resource id
     */
    @LayoutRes
    protected abstract fun getLayoutID(): Int

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    protected abstract fun getViewModel(): VM

    /**
     * Override for set up ui
     *
     * @return view model instance
     */

    protected abstract fun setupUI()

    /**
     * Override for handel actions
     *
     * @return view model instance
     */
    protected abstract fun onClickedListener()

    /**
     * Override for liveData listener
     *
     * @return view model instance
     */
    protected abstract fun observerLiveData()

    protected abstract fun isFullHeight(): Boolean

    private fun performDataBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = DataBindingUtil.inflate(inflater, getLayoutID(), container, false)
        root = binding.root
        binding.setVariable(BR.vm, vm)
        binding.executePendingBindings()
        lifecycle.addObserver(getViewModel())

    }


    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {

        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            val layoutParams = parentLayout.layoutParams
            val windowHeight = getWindowHeight()
            layoutParams.height = windowHeight
            parentLayout.layoutParams = layoutParams
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (requireContext() as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun setupBaseLiveDataObserver() {
        vm.showToast.observe(viewLifecycleOwner, Observer {

            if (it == null)
                return@Observer

            if (it is String) {
                showToast(it)
                return@Observer
            }

            if (it is Int) {
                showToast(it)
                return@Observer
            }
        })

        loading = requireActivity().showLoading()
        loading?.setCancellable(false)
        loading?.dismiss()
        vm.isLoading.observe(viewLifecycleOwner, Observer {

            if (it) {
                loading?.show()
            } else {
                loading?.dismiss()
            }
        })

        vm.snackLogin.observe(viewLifecycleOwner, Observer {

//            if (it)
//                snackBar(R.string.youMustLogin, R.string.login) {
//
//                }
        })
    }

}