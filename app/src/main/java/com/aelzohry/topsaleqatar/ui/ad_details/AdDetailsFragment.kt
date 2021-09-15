package com.aelzohry.topsaleqatar.ui.ad_details

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aelzohry.topsaleqatar.BuildConfig
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentAdDetailsBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderAdPhotoBinding
import com.aelzohry.topsaleqatar.databinding.ViewHolderCommentBinding
import com.aelzohry.topsaleqatar.helper.*
import com.aelzohry.topsaleqatar.model.Ad
import com.aelzohry.topsaleqatar.model.AdStatus
import com.aelzohry.topsaleqatar.model.Comment
import com.aelzohry.topsaleqatar.model.Photo
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.AdsResponse
import com.aelzohry.topsaleqatar.repository.remote.responses.RecentComments
import com.aelzohry.topsaleqatar.repository.remote.responses.RecentCommentsResponse
import com.aelzohry.topsaleqatar.repository.remote.responses.RelatedAdResponse
import com.aelzohry.topsaleqatar.ui.MainActivity
import com.aelzohry.topsaleqatar.ui.comments.CommentsFragment
import com.aelzohry.topsaleqatar.ui.messages.ChatFragment
import com.aelzohry.topsaleqatar.ui.user.UserFragment
import com.aelzohry.topsaleqatar.utils.Binding
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseAdapter
import com.aelzohry.topsaleqatar.utils.extenions.setVisible
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.fragment_ad_details.*
import retrofit2.Call


class AdDetailsFragment : BaseActivity<FragmentAdDetailsBinding, AdDetailsViewModel>() {

    companion object {
        private const val ARG_AD = "AD"
        private const val ARG_AD_ID = "AD_ID"
        private const val ARG_CAME_FROM_CHAT = "CAME_FROM_CHAT"

        @JvmStatic
        fun newInstance(
            context: Context,
            ad: Ad?,
            cameFromChat: Boolean = false,
            isClear: Boolean = false,
            adId: String? = ""
        ) {

            val i = Intent(context, AdDetailsFragment::class.java)
                .putExtra(ARG_AD_ID, if (adId?.isNullOrEmpty() == true) ad?._id else adId)
                .putExtra(ARG_AD, ad)
                .putExtra(ARG_CAME_FROM_CHAT, cameFromChat)

            if (isClear) i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }
    }

    override fun getLayoutID(): Int = R.layout.fragment_ad_details

    override fun getViewModel(): AdDetailsViewModel {

        val ad: Ad? = intent.getParcelableExtra(ARG_AD)
        val adId = intent.getStringExtra(ARG_AD_ID) ?: ""
        return ViewModelProvider(
            this,
            ViewModelFactory(ad, adId)
        )[AdDetailsViewModel::class.java]
    }

    override fun setupUI() {
        vm.cameFromChat = intent.getBooleanExtra(ARG_CAME_FROM_CHAT, false)
        initToolbar(getString(R.string.ad_details))
        currentImageView.setOnClickListener {
            openImageInfFullScreen()
        }
        vm.adRes.postValue(vm.ad)

    }

    override fun onClickedListener() {
        binding.reportButton.setOnClickListener { report() }
    }

    override fun observerLiveData() {

        vm.adRes.observe(this, Observer {
            loadData(it)
        })
        vm.userAdsRes.observe(this, Observer {
            binding.userAdsRecyclerView.adapter = RelatedAdsAdapter(it) {
                vm.onAdClickedListener(binding.root, it)
            }
        })

        vm.relatedAdsRes.observe(this, Observer {
            binding.relatedAdsRecyclerView.adapter = RelatedAdsAdapter(it) {
                vm.onAdClickedListener(binding.root, it)
            }
        })

        vm.commentsRes.observe(this, Observer { recentComments ->

            commentsContainerView.setVisible(recentComments.comments.isNotEmpty())
            moreCommentsView.setVisible(recentComments.loadMore)
            commentsRecyclerView.adapter = BaseAdapter<Comment, ViewHolderCommentBinding>(
                R.layout.view_holder_comment,
                vm,
                recentComments.comments
            ) { bind, model, position, adapter ->
                val username = model.user?._name ?: ""

                bind.usernameTextView.text =
                    username + if (vm.ad?.user?._id == model.user?._id) " [${getString(R.string.author)}]" else ""
                bind.dateTextView.text = model.createdAt.toDate()?.ago() ?: ""
                bind.actionsButton.setOnClickListener {
                    setupActionComment(it, model, position, adapter)
                }

            }

        })
    }

    private fun openImageInfFullScreen() {
        val photos = vm.ad?.photos ?: return
        if (photos.isEmpty()) return

        StfalconImageViewer.Builder<Photo>(this, photos) { view, image ->
            Picasso.get().load(image.orgUrl).into(view)
        }.withStartPosition(vm.selectedPhoto.get() ?: 0).show()
    }

    private fun loadData(ad: Ad?) {
        ad ?: return
        setupLocation()
        pendingApprovalTextView.setVisible(ad.status != AdStatus.ACTIVE)
        pendingApprovalTextView.text = ad.status.getTitle(this)
        titleTextView.text = ad.title
        Picasso.get().load(ad.photos.firstOrNull()?.orgUrl).placeholder(R.mipmap.logo)
            .into(currentImageView)
        imagesRecyclerView.setVisible(ad.photos.isNotEmpty())
        fixedImageView.setVisible(ad.isFixed)
        dateTextView.text = ad.publishedAt.toDate()?.ago() ?: ""
        priceTextView.text = ad.price.toString()
        viewsCountTextView.text = ad.viewsCount.toString()
        detailsTextView.text = ad.details

        vm.viewCount.set(ad.viewsCount)
        vm.favState.set(ad.isFavourite)
        vm.followState.set(ad.user?.isFollowing)
        vm.likeCount.set(ad.likesCount)
        vm.commentsCount.set(ad.commentsCount)
        vm.likeState.set(ad.isLiked)
        vm.fixed.set(ad.isFixed)
        // gallery

        ad.location?.let {
            
        }
        imagesRecyclerView.adapter = BaseAdapter<Photo, ViewHolderAdPhotoBinding>(
            R.layout.view_holder_ad_photo,
            vm,
            ad.photos
        ) { vhBind, model, position, adapter ->
            vhBind.imageView.setOnClickListener {
                Binding.loadImage(currentImageView, model.orgUrl)
                vm.onImageClickedListener(model, position)
            }
        }

        // category & type
        categoryTextView.text = ad.category?.title?.localized ?: ""

        subcategoryView.setVisible(ad.subcategory != null)// = if () View.GONE else View.VISIBLE
        subcategoryTextView.text = ad.subcategory?.title?.localized ?: ""

        typeView.visibility = if (ad.type == null) View.GONE else View.VISIBLE
        typeTextView.text = ad.type?.title?.localized ?: ""

        regionView.visibility = if (ad.region == null) View.GONE else View.VISIBLE
        regionTextView.text = ad.region?.title?.localized ?: ""

        roomsView.visibility =
            if (ad.numOfRooms == null || ad.numOfRooms == 0) View.GONE else View.VISIBLE
        roomsTextView.text = ad.numOfRooms.toString() ?: ""

        bathRoomView.visibility =
            if (ad.numOfBathroom == null || ad.numOfBathroom == 0) View.GONE else View.VISIBLE
        bathRoomTextView.text = ad.numOfBathroom.toString() ?: ""

        carMakeView.visibility = if (ad.carMake == null) View.GONE else View.VISIBLE
        carMakeTextView.text = ad.carMake?.title ?: ""

        carModelView.visibility = if (ad.carModel == null) View.GONE else View.VISIBLE
        carModelTextView.text = ad.carModel?.title ?: ""

        carYearView.visibility = if (ad.carYear?.trim().isNullOrEmpty()) View.GONE else View.VISIBLE
        carYearTextView.text = ad.carYear ?: ""

        carKmView.visibility = if (ad.km?.trim().isNullOrEmpty()) View.GONE else View.VISIBLE
        carKmTextView.text = ad.km ?: ""

        // user
        usernameTextView.text = ad.user?._name
        mobileTextView.text = ad.user?.mobile
        ad.user?.avatarUrl?.let {
            Picasso.get().load(it).placeholder(R.drawable.avatar).into(avatarImageView)
        }
//        emailButton.visibility = if (ad.user?.email?.trim().isNullOrEmpty()) View.GONE else View.VISIBLE

    }

    private fun setupLocation() {

//        (fragmentManager.findFragmentById(
//            R.id.view_map
//        ) as MapFragment).getMapAsync {
//
//            val circleOptions = CircleOptions()
//                .center(LatLng(21.5419362, 39.2178875))
//                .radius(500.0)
//                .strokeWidth(2f)
//                .strokeColor(Color.BLUE)
//                .fillColor(Color.parseColor("#500084d3"))
//
//            it.addCircle(circleOptions)
//        }

    }

    private fun report() {
        if (!vm.isLogin) {
            vm.snackLogin.postValue(true)
            return
        }

        val dialog = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.hint = getString(R.string.suitable_reason)
        dialog.setTitle(getString(R.string.report))
        dialog.setView(editText)

        dialog.setPositiveButton(getString(R.string.send)) { _, _ ->
            val text = editText.text.toString()
            if (text.isEmpty()) return@setPositiveButton
            editText.hideKeyboard()
            vm.onReportAdClickedListener(text)
        }

        dialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->

        }

        dialog.show()
    }

    private fun setupActionComment(
        v: View,
        comment: Comment,
        position: Int,
        adapter: BaseAdapter<Comment, ViewHolderCommentBinding>
    ): PopupMenu {


        val popupMenu = PopupMenu(v.context, v)
        val menu = popupMenu.menu
        popupMenu.menuInflater.inflate(R.menu.comments_menu, menu)
        menu.findItem(R.id.edit).isVisible = comment.isCommentOwner
        menu.findItem(R.id.delete).isVisible = comment.isAdOwner || comment.isCommentOwner
        menu.findItem(R.id.email).isVisible = (comment.user?.email?.isNotEmpty()
            ?: false) && !(comment.isAdOwner || comment.isCommentOwner)
        menu.findItem(R.id.profile).isVisible = true
        menu.findItem(R.id.block).isVisible = true
        menu.findItem(R.id.call).isVisible = true
        menu.findItem(R.id.chat).isVisible = comment.isAdOwner

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.delete -> vm.onDeleteCommentClickedListener(
                    comment,
                    v
                ) { adapter.removeItem(position) }


                R.id.edit -> editCommentDialog(
                    comment,
                    v
                ) { text ->
                    comment.text = text
                    commentsRecyclerView.adapter?.notifyDataSetChanged()
                }


                R.id.block -> vm.onBlocCommentClickedListener(
                    comment,
                    v
                ) {
                    finish()
                }


                R.id.profile -> vm.onProfileCommentClickedListener(
                    comment,
                    v
                ) { }


                R.id.call -> vm.onCallCommentClickedListener(
                    comment,
                    v
                ) { }
                R.id.chat -> vm.onChatCommentClickedListener(
                    comment,
                    v
                ) { }
                R.id.email -> vm.onEmailCommentClickedListener(
                    comment,
                    v
                ) { }
            }
            true
        }

        popupMenu.show()
        return popupMenu
    }

    private fun editCommentDialog(comment: Comment, v: View, result: (newComment: String) -> Unit) {
        val dialog = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.hint = getString(R.string.comment)
        editText.setText(comment.text)
        dialog.setTitle(getString(R.string.edit_comment))
        dialog.setView(editText)

        dialog.setPositiveButton(getString(R.string.edit)) { _, _ ->
            val text = editText.text.toString()
            if (text.isEmpty()) return@setPositiveButton
            vm.onEditCommentClickedListener(comment, text, v) { result(text) }
        }

        dialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->

        }

        dialog.show()

    }


}
