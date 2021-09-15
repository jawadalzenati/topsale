package com.aelzohry.topsaleqatar.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.ActivitySplashBinding
import com.aelzohry.topsaleqatar.databinding.DialogLanguageBinding
import com.aelzohry.topsaleqatar.helper.AppLanguage
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseBottomSheet
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import kotlinx.android.synthetic.main.dialog_language.*

class SplashActivity : BaseActivity<ActivitySplashBinding,BaseViewModel>() {

    override fun getLayoutID(): Int = R.layout.activity_splash

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]

    override fun setupUI() {
    }

    override fun onClickedListener() {

    }

    override fun observerLiveData() {

    }

    override fun onStart() {
        super.onStart()

        if (Helper.isFirstRun) {
            Helper.isFirstRun = false
            askForLanguage()
        } else {
            setHandler()
        }
    }

    private fun setHandler() {
        Handler().postDelayed({
            goToMainScreen()
        }, 1000)
    }

    private fun askForLanguage() {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_language)
            .create()

        dialog.show()
        dialog.setOnDismissListener {
            goToMainScreen()
        }

        dialog.englishButton.setOnClickListener {
            Helper.setLanguage(AppLanguage.English)
            dialog.dismiss()
        }

        dialog.arabicButton.setOnClickListener {
            Helper.setLanguage(AppLanguage.Arabic)
            dialog.dismiss()
        }
    }

    private fun goToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}