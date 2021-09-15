package com.aelzohry.topsaleqatar.ui.messages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentMessagesBinding
import com.aelzohry.topsaleqatar.helper.Helper
import com.aelzohry.topsaleqatar.helper.NotificationCenter
import com.aelzohry.topsaleqatar.helper.NotificationType
import com.aelzohry.topsaleqatar.helper.showProgress
import com.aelzohry.topsaleqatar.model.Not
import com.aelzohry.topsaleqatar.model.TChannel
import com.aelzohry.topsaleqatar.model.TMessage
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.ChannelsResponse
import com.aelzohry.topsaleqatar.repository.remote.responses.NotificationsResponse
import com.aelzohry.topsaleqatar.ui.MainActivity
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.ui.ads.AdsFragment
import com.aelzohry.topsaleqatar.ui.messages.adapter.NotificationsAdapter
import com.aelzohry.topsaleqatar.ui.user.UserFragment
import com.aelzohry.topsaleqatar.utils.CustomFrame
import com.aelzohry.topsaleqatar.utils.DividerItemDecorator
import com.aelzohry.topsaleqatar.utils.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_messages.*
import retrofit2.Call

class MessagesFragment : BaseFragment<FragmentMessagesBinding, MessagesViewModel>() {

    enum class Mode {
        ALL, CHAT, NOTIFICATIONS
    }

    private lateinit var repository: Repository
    private lateinit var adapter: NotificationsAdapter

    override fun getLayoutID(): Int = R.layout.fragment_messages

    override fun getViewModel(): MessagesViewModel =
        ViewModelProvider(this)[MessagesViewModel::class.java]

    override fun setupUI() {

        initToolbar(getString(R.string.messages))
        vm.frameState.set(CustomFrame.FrameState.LAYOUT)
        refreshLayout.setOnRefreshListener { refresh() }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                // check for scroll down
                {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !didLoadAllNotifications) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loadNextPage()
                        }
                    }
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerView.addItemDecoration(
            DividerItemDecorator(
                ContextCompat.getDrawable(requireContext(),R.drawable.divider_line)
            )
        )
        adapter = NotificationsAdapter(arrayListOf()) {
            didTapNotWrapper(it)
        }
        recyclerView.adapter = adapter
        NotificationCenter.addObserver(
            requireContext(),
            NotificationType.NewMessageReceived,
            newMessageReceiver
        )

        NotificationCenter.addObserver(
            requireContext(),
            NotificationType.NewNotificationReceived,
            newNotificationReceiver
        )
        refresh()
    }

    override fun onClickedListener() {

        binding.btnAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                vm.typeState.set(Mode.ALL)
                refresh()
            }
        }

        binding.btnMessages.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                vm.typeState.set(Mode.CHAT)
                refresh()
            }
        }

        binding.btnNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                vm.typeState.set(Mode.NOTIFICATIONS)
                refresh()
            }
        }
    }

    override fun observerLiveData() {

    }

    private val newMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val newMessage = intent.getParcelableExtra<TMessage>("new_message") ?: return
            val wrapper = adapter.wrappers.firstOrNull { it.channel?._id == newMessage.channelId }
            if (wrapper != null) {
                wrapper.channel?.lastMessage = newMessage
                adapter.sort()
            } else {
                refresh()
            }
        }
    }

    private val newNotificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            if (vm.typeState.get() == Mode.CHAT) return
            val newNotification = intent.getParcelableExtra<Not>("new_notification") ?: return
            val wrapper = adapter.wrappers.firstOrNull { it.not?._id == newNotification._id }
            if (wrapper == null) {
                adapter.wrappers.add(
                    0,
                    NotWrapper(newNotification.createdAt, newNotification, null)
                )
                adapter.notifyDataSetChanged()
            } else {
                refresh()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = RemoteRepository()
    }

    override fun onDestroy() {
        notificationsCall?.cancel()
        channelsCall?.cancel()
        NotificationCenter.removeObserver(requireContext(), newMessageReceiver)
        NotificationCenter.removeObserver(requireContext(), newNotificationReceiver)
        super.onDestroy()
    }

    private fun didTapNotWrapper(wrapper: NotWrapper) {
        wrapper.channel?.let { channel ->
            didTapChannel(channel)
            if (!channel.seen) {
                channel.seen = true
                adapter.notifyDataSetChanged()
            }
        }

        wrapper.not?.let { not ->
            if (!not.seen) {
                not.seen = true
                adapter.notifyDataSetChanged()
            }
            didTapNot(not)
        }
    }

    private fun didTapChannel(channel: TChannel) {
        ChatFragment.newInstance(requireContext(), channel)
    }

    private fun didTapNot(not: Not) {
        val activity = activity as MainActivity
        when {
            not.objectCategory != null -> {
                AdsFragment.newInstance(requireContext(), not.objectCategory)
            }
            not.objectAd != null -> {
                Log.e("ad_details_chat", not.objectAd.toString())
                vm.onAdClickedListener(binding.root, not.objectAd)
            }
            not.objectUser != null -> {
                UserFragment.newInstance(requireContext(), not.objectUser, null,not.objectUser._id)
//                activity.pushFragment(fragment)
            }
            not.objectComment != null -> {
                val adId = not.objectComment.adId ?: return
                val progress = showProgress(activity)
                repository.getAd(adId) {
                    progress.dismiss()
                    it?.let { ad ->
                        vm.onAdClickedListener(binding.root,ad)
//                        AdDetailsFragment.newInstance(requireContext(), ad)
//                        activity.pushFragment(fragment)
                    }
                }
            }
        }
    }

    private var isLoading = false
    private var currentPage = 1
    private var didLoadAllNotifications = false
    private var didLoadAllChannels = false

    private fun refresh() {
        isLoading = false
        didLoadAllNotifications = false
        currentPage = 1
        adapter.wrappers.clear()
        adapter.notifyDataSetChanged()
        loadData(1)
    }

    private var notificationsCall: Call<NotificationsResponse>? = null
    private var channelsCall: Call<ChannelsResponse>? = null

    private fun loadData(page: Int) {
        if (isLoading) return
        startLoading()

        when (vm.typeState.get()) {
            Mode.ALL -> {
                loadNotification(page)
                loadMessages(page)
            }
            Mode.NOTIFICATIONS -> {
                loadNotification(page)
            }

            Mode.CHAT -> {
                loadMessages(page)
            }
        }
//        if (mode == Mode.ALL || mode == Mode.NOTIFICATIONS) {
//
//        }
//
//        if (mode == Mode.ALL || mode == Mode.CHAT) {
//
//        }
    }

    private fun loadNotification(page: Int) {
        notificationsCall = repository.getNotifications(page) { success, message, notifications ->
            if (success) {
                notifications?.let { nots ->
                    currentPage = page
                    val wrappers = nots.map { NotWrapper(it.createdAt, it, null) }
                    adapter.wrappers.addAll(wrappers)
                    adapter.sort()
                }
                if (notifications.isNullOrEmpty()) {
                    didLoadAllNotifications = true
                }
            } else {
                Helper.showToast(context, message)
            }
            endLoading()
        }
    }

    private fun loadMessages(page: Int) {
        channelsCall =
            repository.getChannels(page) { success, message, channels ->
                if (success) {
                    channels?.let { channels ->
                        currentPage = page
                        val wrappers = channels.mapNotNull {
                            if (it.createdAt == null) null else NotWrapper(
                                it.createdAt!!,
                                null,
                                it
                            )
                        }
                        adapter.wrappers.addAll(wrappers)
                        adapter.sort()
                    }
                    if (channels.isNullOrEmpty()) {
                        didLoadAllChannels = true
                    }
                } else {
                    Helper.showToast(context, message)
                }
                endLoading()
            }
    }

    private fun loadNextPage() {
        if (isLoading) return
        loadData(currentPage + 1)
    }

    private fun startLoading() {
        isLoading = true
        refreshLayout.isRefreshing = true
    }

    private fun endLoading() {
        isLoading = false
        refreshLayout.isRefreshing = false
    }

}

data class NotWrapper(
    val createdAt: String,
    val not: Not?,
    val channel: TChannel?
) {
    val seen: Boolean
        get() = not?.seen ?: channel?.seen ?: true
}