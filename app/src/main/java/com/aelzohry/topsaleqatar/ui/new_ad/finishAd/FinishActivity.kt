package com.aelzohry.topsaleqatar.ui.new_ad.finishAd

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ActivityFinishBinding
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.ui.new_ad.NewAdFragment
import com.aelzohry.topsaleqatar.utils.Binding
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel

class FinishActivity : BaseActivity<ActivityFinishBinding, BaseViewModel>() {

    private var ad: Ad? = null
    override fun getLayoutID(): Int = R.layout.activity_finish

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {

        initToolbar(getString(R.string.posted))

        ad = intent?.getParcelableExtra<Ad>(ARG_AD)
        Binding.loadImage(binding.iv, ad?.imageUrl)

    }

    override fun onClickedListener() {

        binding.btnShare.setOnClickListener {

            val ad = ad ?: return@setOnClickListener
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody =
                "https://topsale.qa/ads/" + ad._id
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
           startActivity(
                Intent.createChooser(
                    sharingIntent,
                    getString(R.string.share_ad)
                )
            )
            finish()

        }
        binding.btnNewAd.setOnClickListener {

            NewAdFragment.newAd(this)
            finish()
        }
        binding.btnDone.setOnClickListener {
            AdDetailsFragment.newInstance(this, ad!!)
            finish()
        }

    }

    override fun observerLiveData() {

    }

    companion object {
        private const val ARG_AD = "ARG_AD"
        fun newInstance(context: Context, ad: Ad) {
            context.startActivity(
                Intent(context, FinishActivity::class.java)
                    .putExtra(ARG_AD, ad)
            )
            (context as AppCompatActivity).finish()
        }
    }
}