package com.aelzohry.topsaleqatar.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aelzohry.topsaleqatar.R
import kotlinx.android.synthetic.main.fragment_help.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.databinding.FragmentHelpBinding
import com.aelzohry.topsaleqatar.helper.Constants
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

class HelpFragment : BaseActivity<FragmentHelpBinding, BaseViewModel>() {

    override fun getLayoutID(): Int = R.layout.fragment_help

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {

        initToolbar(getString(R.string.help))
        helpWebView.webViewClient = MyWebViewClient()
        helpWebView.loadUrl(Constants.HELP_URL)
        helpWebView.requestFocus()

    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

}
