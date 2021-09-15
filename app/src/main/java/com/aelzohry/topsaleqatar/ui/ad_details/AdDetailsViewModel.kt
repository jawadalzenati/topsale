package com.aelzohry.topsaleqatar.ui.ad_details

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.helper.showProgress
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.AdStatus
import com.aelzohry.topsaleqatar.model.Comment
import com.aelzohry.topsaleqatar.model.Photo
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.RecentComments
import com.aelzohry.topsaleqatar.ui.MainActivity
import com.aelzohry.topsaleqatar.ui.ad_details.relatedAds.RelatedAdsActivity
import com.aelzohry.topsaleqatar.ui.comments.CommentsFragment
import com.aelzohry.topsaleqatar.ui.messages.ChatFragment
import com.aelzohry.topsaleqatar.ui.user.UserFragment
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import kotlinx.android.synthetic.main.fragment_ad_details.*

/*
* created by : ahmed mustafa
* email : ahmed.mustafa15996@gmail.com
*phone : +201025601465
*/
class AdDetailsViewModel(var ad: Ad?, val adId: String) : BaseViewModel() {

    private var repository: Repository = RemoteRepository()

    var adRes = MutableLiveData<Ad>()
    var commentsRes = MutableLiveData<RecentComments>()
    var relatedAdsRes = MutableLiveData<ArrayList<Ad>>()
    var userAdsRes = MutableLiveData<ArrayList<Ad>>()

    var viewCount = ObservableField(ad?.viewsCount)
    val favState = ObservableField(ad?.isFavourite)
    val followState = ObservableField(ad?.user?.isFollowing)
    val likeCount = ObservableField(ad?.likesCount)
    val commentsCount = ObservableField(ad?.commentsCount)
    val likeState = ObservableField(ad?.isLiked)
    val fixed = ObservableField(ad?.isFixed)
    val selectedPhoto = ObservableField(0)
    val etComment = ObservableField("")
    val relatedAdsState = ObservableField(false)
    val userAdsState = ObservableField(false)

    var cameFromChat = false
    fun onImageClickedListener(model: Photo, i: Int) {
        selectedPhoto.set(i)
        model.orgUrl
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun loadData() {

        repository.getAd(adId) { ad ->
            ad ?: return@getAd
            adRes.postValue(ad)
        }
        incrementViews()
        loadComments()
        loadUserAds()
        loadRelatedAds()
    }

    private fun incrementViews() {
        val ad = ad ?: return
        if (ad.user?._id == Helper.userId) return
        repository.viewAd(ad._id) { success, _ ->
            if (success) {
                ad.viewsCount++
                viewCount.set(ad.viewsCount)

            }
        }
    }

    private fun loadRelatedAds() {
        val ad = this.ad ?: return
        repository.getRelatedAds(ad._id) {
            it?.data?.let { response ->
                Log.e("ads_count_related", it.data.ads.size.toString())
                if (response.ads.isNotEmpty()) {

                    val newList = ArrayList<Ad>()
                    response.ads.forEachIndexed { index, ad ->
                        if (index < 3)
                            newList.add(ad)
                    }
                    relatedAdsRes.postValue(newList)
                    relatedAdsState.set(true)
                }
            }
        }
    }

    private fun loadUserAds() {
        val ad = this.ad?.user ?: return
        repository.getUserAds(ad._id, 1) {
            it?.data?.let { response ->

                Log.e("ads_count_user", it.data.ads.size.toString())
                if (response.ads.isNotEmpty()) {
                    val newList = ArrayList<Ad>()
                    response.ads.forEachIndexed { index, ad ->
                        if (index < 3)
                            newList.add(ad)
                    }
                    userAdsRes.postValue(newList)
                    userAdsState.set(true)
                }
            }
        }
    }

    fun onCallPhoneClickedListener(v: View) {

        val phone = ad?.user?.mobile ?: return
        Helper.callPhone(phone, v.context)

    }

    fun onWhatsappClickedListener(v: View) {
        val mobile = this.ad?.user?.mobile ?: return
        val url = "https://wa.me/${mobile}"
        Helper.openUrl(url, v.context)
    }

    fun onChatClickedListener(v: View) {
        if (cameFromChat) {
            (v.context as AppCompatActivity).onBackPressed()
            return
        }

        val ad = this.ad ?: return
        isLoading.postValue(true)
        repository.newChannel(ad._id, null) { channel, message ->
            isLoading.postValue(false)
            if (channel == null) {
                showToast.postValue(message)
                return@newChannel
            }

            ChatFragment.newInstance(v.context, channel, true)
        }
    }

    fun onToggleFollowClickedListener() {
        if (!isLogin) {
            snackLogin.postValue(true)
            return
        }

        val ad = this.ad ?: return
        val user = ad.user ?: return
        isLoading.postValue(true)

        if (user.isFollowing) {
            repository.unFollowUser(user._id) { success, message ->
                if (success) {
                    this.ad?.user?.isFollowing = false
                    followState.set(false)
                }
                showToast.postValue(message)
                isLoading.postValue(false)
            }
        } else {
            repository.followUser(user._id) { success, message ->
                if (success) {
                    this.ad?.user?.isFollowing = true
                    followState.set(true)
                }
                showToast.postValue(message)
                isLoading.postValue(false)
            }
        }
    }

    fun onToggleLikeClickedListener() {
        if (!isLogin) {
            snackLogin.postValue(true)
            return
        }

        val ad = this.ad ?: return
        isLoading.postValue(true)

        repository.toggleLike(ad._id) { success, message ->
            if (success) {
                if (ad.isLiked) ad.likesCount-- else ad.likesCount++
                ad.isLiked = !ad.isLiked
                likeCount.set(ad.likesCount)
                likeState.set(ad.isLiked)
            }
            showToast.postValue(message)
            isLoading.postValue(false)
        }
    }

    fun onToggleFavClickedListener() {
        if (!isLogin) {
            snackLogin.postValue(true)
            return
        }

        val ad = this.ad ?: return
        isLoading.postValue(true)
        repository.toggleFavoriteAd(ad._id) { success, message ->
            if (success) {
                ad.isFavourite = !ad.isFavourite
                favState.set(ad?.isFavourite)
            }
            showToast.postValue(message)
            isLoading.postValue(false)
        }
    }

    fun onShareClickedListener(v: View) {
        val ad = ad ?: return
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            "https://topsale.qa/ads/" + ad._id
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        v.context.startActivity(
            Intent.createChooser(
                sharingIntent,
                v.context.getString(R.string.share_ad)
            )
        )
    }

    fun onRelateAdAllBtnClickedListener(v: View) {
        RelatedAdsActivity.newInstance(v.context, ad?.id ?: "")
    }

    fun onUserProfileClickedListener(v: View) {
        val user = ad?.user ?: return
        UserFragment.newInstance(v.context, user, ad,user._id)
    }

    fun onBlocUserClickedListener(v: View) {
        if (!isLogin) {
            snackLogin.postValue(true)
            return
        }

        val ad = this.ad ?: return
        val user = ad.user ?: return
        isLoading.postValue(true)
        repository.blockUser(user._id) { success, message ->
            showToast.postValue(message)
            isLoading.postValue(false)
            if (success) {
                v.context.startActivity(
                    Intent(v.context, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    fun onReportAdClickedListener(text: String) {
        val ad = this.ad ?: return

        isLoading.postValue(true)
        repository.reportAd(ad._id, text) { _, message ->
            showToast.postValue(message)
            isLoading.postValue(false)

        }
    }

    fun onSendCommentClickedListener(v: View) {
        if (!isLogin) {
            snackLogin.postValue(true)
            return
        }

        val ad = this.ad ?: return

        val comment = etComment.get() ?: ""
        if (comment.isEmpty()) {
            showToast.postValue(R.string.enter_comment)
            return
        }

        isLoading.postValue(true)
        repository.newComment(ad._id, comment) {
            isLoading.postValue(false)
            showToast.postValue(it?.message ?: "Network Error")
            it?.let {
                it.data?.let { newComment ->
                    loadComments()
                    etComment.set("")
                    ad.commentsCount = newComment.newCommentsCount
                    commentsCount.set(newComment.newCommentsCount)
                }
            }
        }
    }

    fun onMoreCommentsClickedListener(v: View) {
        val ad = ad ?: return
        CommentsFragment.newInstance(v.context, ad)
    }

// comment action

    fun loadComments() {
        val ad = this.ad ?: return
        repository.getRecentComments(ad._id) { response ->
            commentsRes.postValue(response)
        }
    }

    fun onDeleteCommentClickedListener(comment: Comment, v: View, result: () -> Unit) {
        isLoading.postValue(true)
        repository.deleteComment(comment._id) { success, message ->
            isLoading.postValue(false)
            showToast.postValue(message)
            if (success) {
                result()
            }
        }
    }

    fun onEditCommentClickedListener(
        comment: Comment,
        text: String,
        v: View,
        result: () -> Unit
    ) {

        isLoading.postValue(true)
        repository.editComment(comment._id, text) { response ->
            val message = response?.message ?: "Error"
            val success = response?.success ?: false
            val updatedComment = response?.data?.comment

            isLoading.postValue(false)
            showToast.postValue(message)

            if (success) {
                if (updatedComment != null) {
                    result()
                } else {
                    loadComments()
                }
            }
        }

    }


    fun onBlocCommentClickedListener(comment: Comment, v: View, result: () -> Unit) {
        if (!isLogin) {
            snackLogin.postValue(true)
            return
        }

        val ad = this.ad ?: return
        val adUser = ad.user ?: return
        val user = comment.user ?: return
        val isAuthor = user._id == adUser._id

        isLoading.postValue(true)
        repository.blockUser(user._id) { success, message ->
            showToast.postValue(message)
            isLoading.postValue(false)
            if (success) {
                result()
            }
        }
    }

    fun onProfileCommentClickedListener(comment: Comment, v: View, result: () -> Unit) {
        val user = comment.user ?: return
        UserFragment.newInstance(v.context, user, null,user._id)
    }

    fun onCallCommentClickedListener(comment: Comment, v: View, result: () -> Unit) {
        val phone = comment.user?.mobile ?: return
        Helper.callPhone(phone, v.context)
    }

    fun onChatCommentClickedListener(comment: Comment, v: View, result: () -> Unit) {
        val context = v.context ?: return

        isLoading.postValue(true)
        repository.newChannel(ad?._id, comment.user?._id) { channel, message ->
            isLoading.postValue(false)
            if (channel == null) {
                showToast.postValue(message)
                return@newChannel
            }

            ChatFragment.newInstance(context, channel)
        }
    }


    fun onEmailCommentClickedListener(comment: Comment, v: View, result: () -> Unit) {
        val user = comment.user ?: return
        val email = user.email ?: return
        Helper.sendEmail(email, v.context)
    }


}