package com.aelzohry.topsaleqatar.ui.more

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import kotlinx.android.synthetic.main.fragment_more.*
import com.aelzohry.topsaleqatar.BuildConfig
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentMoreBinding
import com.aelzohry.topsaleqatar.helper.AppLanguage
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.helper.showProgress
import com.aelzohry.topsaleqatar.model.User
import com.aelzohry.topsaleqatar.repository.remote.responses.ProfileResponse
import com.aelzohry.topsaleqatar.ui.auth.LoginActivity
import com.aelzohry.topsaleqatar.ui.more.blocked.BlockedFragment
import com.aelzohry.topsaleqatar.ui.more.favorite.FavoriteAdsFragment
import com.aelzohry.topsaleqatar.ui.more.profile.ProfileFragment
import com.aelzohry.topsaleqatar.utils.Binding
import com.aelzohry.topsaleqatar.utils.base.BaseFragment
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import com.aelzohry.topsaleqatar.utils.extenions.setVisible
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_language.*
import kotlinx.android.synthetic.main.dialog_terms.*
import retrofit2.Call


class MoreFragment : BaseFragment<FragmentMoreBinding, BaseViewModel>() {

    override fun getLayoutID(): Int = R.layout.fragment_more

    override fun getViewModel(): BaseViewModel = ViewModelProvider(this)[BaseViewModel::class.java]
    override fun setupUI() {

        initToolbar(getString(R.string.profile))
        appVersionTextView.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        userCells.setVisible(vm.isLogin)
        loginCell.setVisible(!vm.isLogin) //= if (isAuthenticated) View.GONE else View.VISIBLE
        logoutlayout.setVisible(vm.isLogin)


    }

    override fun onResume() {
        super.onResume()
        loadProfile()
        btn_dark.isChecked = Helper.isNightMode

    }

    override fun onClickedListener() {

        userCells.setOnClickListener {
            if (!vm.isLogin) {
                vm.snackLogin.postValue(true)
                return@setOnClickListener
            }
            editProfile()
        }
        loginCell.setOnClickListener {
            login()
        }

        editProfileCell.setOnClickListener {
            if (!vm.isLogin) {
                vm.snackLogin.postValue(true)
                return@setOnClickListener
            }
            editProfile()
        }

        favoritesLayout.setOnClickListener {
            if (!vm.isLogin) {
                vm.snackLogin.postValue(true)
                return@setOnClickListener
            }
            showFavorites()
        }

        blockedLayout.setOnClickListener {
            if (!vm.isLogin) {
                vm.snackLogin.postValue(true)
                return@setOnClickListener
            }
            showBlocked()
        }

        termsCell.setOnClickListener {

            terms()
        }

        helpCell.setOnClickListener {
            help()
        }

        contactCell.setOnClickListener {
            contactUs()
        }

        shareAppCell.setOnClickListener {
            shareApp()
        }

        languageCell.setOnClickListener {
            changeLanguage()
        }

        logoutlayout.setOnClickListener {
            logout()
        }

        btn_dark.setOnCheckedChangeListener { buttonView, isChecked ->
            Helper.isNightMode = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate
                    .MODE_NIGHT_NO
            )
        }
    }


    override fun observerLiveData() {

    }

    private lateinit var repository: Repository
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = RemoteRepository()
    }

    override fun onDestroy() {
        profileCall?.cancel()
        super.onDestroy()
    }

    private fun logout() {
        val pushId = Helper.pushId

        if (pushId == null) {
            proceedLogout()
            return
        }

        val context = context ?: return
        val progress = showProgress(context)
        repository.deletePushToken(pushId) { success ->
            Log.i("Helper", "deletePushToken: $success")
            progress.dismiss()
            if (success) {
                this.proceedLogout()
            }
            // TODO:- handle failure
        }
    }

    private fun proceedLogout() {
        Helper.logout()
        startActivity(
            Intent(context, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }


    private var profileCall: Call<ProfileResponse>? = null

    private fun loadProfile() {
        if (!Helper.isAuthenticated) {
//            endLoading()
            return
        }
//        startLoading()

        profileCall = repository.getProfile { user, message ->
//            endLoading()
            if (user == null) {
                Helper.showToast(context, message)
            } else {
                this.user = user
                this.fillUserData()
            }
        }
    }

//    private fun startLoading() {
//        refreshLayout.isRefreshing = true
//    }

//    private fun endLoading() {
//        refreshLayout.isRefreshing = false
//    }

    private fun fillUserData() {
        val user = user ?: return
        Binding.setWithHidenText(binding.usernameTextView, user._name)
        Binding.setWithHidenText(binding.mobileTextView, user.mobile)
        Binding.setWithHidenText(binding.bioTextView, user.bio)
        user.avatarUrl?.let { url ->
            Picasso.get().load(url).placeholder(R.drawable.avatar).into(avatarImageView)
        }
//        user.stats?.let {
//            followingsTextView.text = it.followingsCount.toString()
//            followersTextView.text = it.followersCount.toString()
//            favoritesTextView.text = it.favAdsCount.toString()
//            myAdsTextView.text = it.adsCount.toString()
//            blockedTextView.text = it.blocksCount.toString()
//        }
    }

    private fun login() {
        startActivity(Intent(activity, LoginActivity::class.java))
    }

    private fun showFavorites() {
        startActivity(Intent(activity, FavoriteAdsFragment::class.java))
//        (activity as MainActivity).pushFragment(FavoriteAdsFragment())
    }

    private fun showBlocked() {
        startActivity(Intent(requireContext(), BlockedFragment::class.java))
//        (activity as MainActivity).pushFragment(BlockedFragment())
    }

    private fun editProfile() {
        val fragment = ProfileFragment.newInstance(requireContext())
//        (activity as MainActivity).pushFragment(fragment)
    }

    private fun help() {
        startActivity(Intent(requireContext(), HelpFragment::class.java))
    }

    private fun terms() {
        val dialog = AlertDialog.Builder(context)
            .setView(R.layout.dialog_terms)
            .setNegativeButton(R.string.ok) { _, _ ->

            }
            .create()

        dialog.show()

        try {
            val res = resources
            val inS = res.openRawResource(R.raw.terms)
            val b = ByteArray(inS.available())
            inS.read(b)
            dialog.termsTextView.text = String(b)
        } catch (e: Exception) {
            dialog.termsTextView.text = getString(R.string.error_terms)
        }
    }

    private fun contactUs() {
        startActivity(Intent(requireContext(), ContactUsFragment::class.java))
    }

    private fun shareApp() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_app)))
    }

    private fun changeLanguage() {
        val context = context ?: return

        val dialog = AlertDialog.Builder(context)
            .setView(R.layout.dialog_language)
            .create()

        dialog.show()

        dialog.englishButton.setOnClickListener {
            dialog.dismiss()
            if (Helper.isEnglish) {
                return@setOnClickListener
            }
            Helper.setLanguage(AppLanguage.English)
            Helper.restartApp(context)
        }

        dialog.arabicButton.setOnClickListener {
            dialog.dismiss()
            if (!Helper.isEnglish) {
                return@setOnClickListener
            }
            Helper.setLanguage(AppLanguage.Arabic)
            Helper.restartApp(context)
        }
    }

}
