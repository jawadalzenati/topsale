package com.aelzohry.topsaleqatar.ui.more

import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_contact_us.*
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentContactUsBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel


class ContactUsFragment : BaseActivity<FragmentContactUsBinding, BaseViewModel>() {

    override fun getLayoutID(): Int = R.layout.fragment_contact_us

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {
        initToolbar(getString(R.string.contact_us))
    }

    override fun onClickedListener() {
        officeTextView.setOnClickListener {
            Helper.callPhone("66466622", this)
        }

        infoEmailTextView.setOnClickListener {
            Helper.sendEmail("info@topsale.email", this)
        }
    }

    override fun observerLiveData() {

    }

}
