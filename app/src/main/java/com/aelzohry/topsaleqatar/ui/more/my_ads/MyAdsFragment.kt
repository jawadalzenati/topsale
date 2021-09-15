package com.aelzohry.topsaleqatar.ui.more.my_ads

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentMyAdsBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderMyAdBinding
import com.aelzohry.topsaleqatar.helper.showProgress
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.ui.new_ad.NewAdFragment
import com.aelzohry.topsaleqatar.utils.base.BaseFragment
import com.aelzohry.topsaleqatar.utils.base.BasePagedAdapter
import com.aelzohry.topsaleqatar.utils.extenions.setupDialog

class MyAdsFragment : BaseFragment<FragmentMyAdsBinding, MyAdsViewModel>() {

    private lateinit var adapter: BasePagedAdapter<Ad, ViewHolderMyAdBinding>
    override fun getLayoutID(): Int = R.layout.fragment_my_ads

    override fun getViewModel(): MyAdsViewModel =
        ViewModelProvider(this)[MyAdsViewModel::class.java]

    override fun setupUI() {

        initToolbar(getString(R.string.my_ads))
        adapter = BasePagedAdapter(
            R.layout.view_holder_my_ad,
            vm,
            { oldItem, newItem -> oldItem == newItem },
            { oldItem, newItem -> oldItem._id == newItem._id }) { bind, model, position, adapter ->
            bind.btnEdit.setOnClickListener {

                NewAdFragment.editAd(requireContext(), model)
            }

            bind.btnOptions.setOnClickListener {

                val popupMenu = PopupMenu(requireContext(), it)
                val menu = popupMenu.menu
                popupMenu.menuInflater.inflate(R.menu.my_ads_option_menu, menu)
                menu.findItem(R.id.btn_active_ad).title =
                    getString(if (model.isActive == true) R.string.deactivate_ad else R.string.activate_ad)
                popupMenu.setOnMenuItemClickListener {

                    when (it.itemId) {
                        R.id.btn_delete_ad -> {
                            deleteAd(model)
                        }
                        R.id.btn_active_ad -> {
                            deActive(model) {
                                model.isActive = if (model.isActive == true) false else true
                            }
                        }
                    }
                    return@setOnMenuItemClickListener true
                }

                popupMenu.show()

//                val d = Dialog(requireContext())
//                setupDialog(
//                    arrayListOf(
//                        getString(R.string.delete_ad),
//                        getString(if (model.isActive == true) R.string.deactivate_ad else R.string.activate_ad)
//                    ), getString(R.string.select)
//                ) { i ->
//
//                    when (i) {
//                        0 -> deleteAd(model)
//                        1 -> deActive(model) {
//                            model.isActive = if (model.isActive == true) false else true
//                        }
//                    }
//                }
            }
        }

        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        binding.recyclerView.adapter = adapter


    }

    override fun onClickedListener() {
    }

    override fun observerLiveData() {

        vm.getFooterState().observe(this, Observer {
            adapter.setState(it)
        })

        vm.getLayoutState().observe(this, Observer {
            vm.frameState.set(it)
        })

        vm.adsRes.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    private fun deleteAd(ad: Ad) {
        val context = this.context ?: return

        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirmation))
            .setMessage(getString(R.string.delete_ad_message))
            .setPositiveButton(getString(R.string.delete_ad)) { dialog, _ ->
                val progress = showProgress(context)
                vm.onRemoveClickedListener(ad) {
                    progress.dismiss()
                }

            }
            .setNegativeButton(getString(R.string.back)) { _, _ -> }
            .create()

        dialog.show()
    }

    fun deActive(ad: Ad, result: () -> Unit) {
        val context = this.context ?: return
        val progress = showProgress(context)
        vm.onDeActiveClickedListener(ad) {
            progress.dismiss()
            result()
        }
    }
}
