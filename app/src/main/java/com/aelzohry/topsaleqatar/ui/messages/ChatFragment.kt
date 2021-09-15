package com.aelzohry.topsaleqatar.ui.messages

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aelzohry.topsaleqatar.App
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.databinding.FragmentChatBinding
import com.aelzohry.topsaleqatar.helper.*
import com.aelzohry.topsaleqatar.model.TChannel
import com.aelzohry.topsaleqatar.model.TMessage
import com.aelzohry.topsaleqatar.repository.Repository
import com.aelzohry.topsaleqatar.repository.remote.RemoteRepository
import com.aelzohry.topsaleqatar.repository.remote.responses.MessagesResponse
import com.aelzohry.topsaleqatar.ui.ad_details.AdDetailsFragment
import com.aelzohry.topsaleqatar.ui.messages.adapter.MessagesAdapter
import com.aelzohry.topsaleqatar.ui.messages.vm.ChatViewModel
import com.aelzohry.topsaleqatar.utils.base.BaseActivity
import com.aelzohry.topsaleqatar.utils.base.BaseViewModel
import com.devlomi.record_view.OnRecordListener
import com.github.dhaval2404.imagepicker.ImagePicker
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_chat.*
import retrofit2.Call
import java.io.*


private const val ARG_CHANNEL = "CHANNEL"
private const val ARG_CAME_FROM_AD = "CAME_FROM_AD"

class ChatFragment : BaseActivity<FragmentChatBinding, ChatViewModel>() {
    private lateinit var channel: TChannel
    private lateinit var repository: Repository
    private var messagesCall: Call<MessagesResponse>? = null
    private lateinit var adapter: MessagesAdapter
    private var cameFromAd: Boolean = false

    private var mRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null


    private val newMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val newMessage = intent.getParcelableExtra<TMessage>("new_message") ?: return
            if (newMessage.channelId == channel._id) {
                if (!adapter.isMessageAdded(newMessage._id)) {
                    adapter.addMessage(newMessage)
                    scrollToBottom()
                }
            }
        }
    }

    override fun getLayoutID(): Int = R.layout.fragment_chat

    override fun getViewModel(): ChatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

    override fun setupUI() {
        repository = RemoteRepository()
        intent?.let {
            channel = it.getParcelableExtra(ARG_CHANNEL)!!
            cameFromAd = it.getBooleanExtra(ARG_CAME_FROM_AD, false)
        }
        initToolbar(channel.ad?.title ?: channel.partner?._name ?: "")


        adapter = MessagesAdapter(channel.sender!!._id, vm) {
            newSeen(it)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter
        loadMessages()

        NotificationCenter.addObserver(
            this,
            NotificationType.NewMessageReceived,
            newMessageReceiver
        )

        channel.ad?.let {
            adTitleTextView.text = it.title
            priceTextView.text = "${it.price.toInt()} ${getString(R.string.currency)}"



            Picasso.get()
                .load(it.user?.avatarUrl)
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(avatarImageView)

            Picasso.get()
                .load(it.imageUrl)
                .error(R.mipmap.logo)
                .placeholder(R.mipmap.logo)
                .into(adImageView)
            adLayout.visibility = View.VISIBLE
        }
        setupRecord()

    }

    override fun onClickedListener() {
        adLayout.setOnClickListener { openAd() }
        callButton.setOnClickListener { call() }
        sendButton.setOnClickListener { sendText() }
        attachmentButton.setOnClickListener { addMedia(it) }
    }

    override fun observerLiveData() {

    }


    override fun onDestroy() {
        messagesCall?.cancel()
        NotificationCenter.removeObserver(this, newMessageReceiver)
        super.onDestroy()
    }

    private fun openAd() {
        val ad = channel.ad ?: return

        if (cameFromAd) {
            onBackPressed()
        } else {
            AdDetailsFragment.newInstance(this, ad, true)
        }
    }

    private fun call() {
        val ad = channel.ad ?: return
        val mobile = ad.user?.mobile ?: return
        Helper.callPhone(mobile, this)
    }

    private fun newSeen(message: TMessage) {
        Log.i(TAG, "newSeen messageId: ${message._id}")
        SocketHelper.sendNewSeenMessage(message._id, channel._id)
    }

    private fun loadMessages() {
        val context = this ?: return
        val progress = showProgress(context)
        messagesCall =
            repository.getMessages(channel._id, 1) { success, messages ->
                progress.dismiss()
                if (success) {
                    messages?.let {
                        adapter.addMessages(it)
                        scrollToBottom()
                    }
                } else {
                    onBackPressed()
                }
            }
    }

    private fun scrollToBottom() {
        if (!adapter.isEmpty)
            recyclerView.scrollToPosition(adapter.messagesCount - 1)
    }

    private fun sendText() {
        val text = editText.text.toString()
        if (text.isEmpty()) return

        val data = mapOf(
            "user" to channel.sender!!.toDict,
            "channel_id" to channel._id,
            "partner_id" to channel.partner!!._id,
            "body" to text,
            "type" to "text"
        )
        SocketHelper.sendNewTextMessage(data)
        editText.text.clear()
    }

    // MARK:- Audio

    private fun setupRecord() {
        recordButton.setRecordView(recordView)
        recordButton.isListenForRecord = true

        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                Dexter.withContext(this@ChatFragment)
                    .withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report?.areAllPermissionsGranted() == true)
                                startRecording()

                            if (report?.isAnyPermissionPermanentlyDenied == true)
                                Helper.showSettingsDialog(
                                    this@ChatFragment,
                                    "${getString(R.string.microphone)} - ${getString(R.string.storage)}"
                                )
                        }


                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }
                    }).check()
            }

            override fun onCancel() {
                stopRecording(false, false)
            }

            override fun onFinish(recordTime: Long) {
                stopRecording(true, true, recordTime / 1000)
            }

            override fun onLessThanSecond() {
                stopRecording(false)
            }
        })

        recordView.setOnBasketAnimationEndListener() {
            recordView.visibility = View.GONE
            editText.visibility = View.VISIBLE
            attachmentButton.visibility = View.VISIBLE
        }
    }

    private fun startRecording(): Boolean {
        recordView.visibility = View.VISIBLE
        editText.visibility = View.GONE
        attachmentButton.visibility = View.GONE

        mRecorder = MediaRecorder()
        mRecorder?.setAudioChannels(2)
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
        mRecorder?.setAudioEncodingBitRate(48000)
        mRecorder?.setAudioSamplingRate(16000)

        mOutputFile = getOutputFile()
        mOutputFile!!.parentFile?.mkdirs()
        mRecorder?.setOutputFile(mOutputFile!!.absolutePath)

        return try {
            mRecorder?.prepare()
            mRecorder?.start()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getOutputFile(): File {
        return File(
            Environment.getExternalStorageDirectory().absolutePath.toString()
                    + "/" + "topsale/Voice Notes/" + "rec_voice_" + System.currentTimeMillis()
                .toString() + ".m4a"
        )
    }

    private fun stopRecording(
        saveFile: Boolean,
        resetControls: Boolean = true,
        recordTime: Long? = null
    ) {
        if (resetControls) {
            recordView.visibility = View.GONE
            editText.visibility = View.VISIBLE
            attachmentButton.visibility = View.VISIBLE
        }

        try {
            mRecorder?.stop()
            mRecorder?.release()
            mRecorder = null
            if (saveFile && mOutputFile != null) {
                val uri =
                    Uri.parse("file://" + mOutputFile!!.absolutePath)
                didRecordAudio(uri, recordTime ?: 0)
            } else {
                mOutputFile?.delete()
            }
        } catch (e: NullPointerException) {

        } catch (e: RuntimeException) {

        }
    }

    private fun didRecordAudio(uri: Uri, recordTime: Long) {
        Log.i(TAG, "ddiRecordAudio $uri")
        val context = this ?: return
        val path = uri.path ?: return
        val progress = showProgress(context)
        repository.newMessageAudio(
            channel._id,
            path,
            recordTime.toInt()
        ) { newMessage, alertMessage ->
            progress.dismiss()
            if (newMessage == null) {
                Helper.showToast(context, alertMessage)
                return@newMessageAudio
            }

            NotificationCenter.postNotification(
                App.context,
                NotificationType.NewMessageReceived,
                hashMapOf("new_message" to newMessage)
            )
        }
    }

    private fun addMedia(v: View) {

        val popupMenu = PopupMenu(v.context, v)
        val menu = popupMenu.menu
        popupMenu.menuInflater.inflate(R.menu.chat_media_attached, menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.photoButton -> addPhoto()
                R.id.videoButton -> addVideo()
            }
            return@setOnMenuItemClickListener true
        }
//        val dialog = AlertDialog.Builder(this)
//            .setView(R.layout.dialog_chat_add)
//            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
//            .create()
//
//        dialog.show()
//
//        val photoButton = dialog.photoButton
//        val videoButton = dialog.videoButton
//
//        photoButton.setOnClickListener {
//            addPhoto()
//            dialog.dismiss()
//        }
//        videoButton.setOnClickListener {
//            addVideo()
//            dialog.dismiss()
//        }
    }

    private fun addPhoto() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true)
                        addPhotoAction()

                    if (report?.isAnyPermissionPermanentlyDenied == true)
                        Helper.showSettingsDialog(
                            this@ChatFragment,
                            "${getString(R.string.storage)} - ${getString(R.string.camera)}"
                        )
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun addPhotoAction() {
        ImagePicker.with(this)
            .maxResultSize(1000, 1000)
            .start { resultCode, data ->
                when (resultCode) {
                    RESULT_OK -> {
                        ImagePicker.getFilePath(data)?.let {
                            didPickImage(it)
                        }
                    }
                    ImagePicker.RESULT_ERROR -> Helper.showToast(
                        this,
                        ImagePicker.getError(data)
                    )
                }
            }
    }

    private fun didPickImage(path: String) {
        val context = this ?: return
        val progress = showProgress(context)
        repository.newMessagePhoto(channel._id, path) { newMessage, alertMessage ->
            progress.dismiss()
            if (newMessage == null) {
                Helper.showToast(context, alertMessage)
                return@newMessagePhoto
            }

            NotificationCenter.postNotification(
                App.context,
                NotificationType.NewMessageReceived,
                hashMapOf("new_message" to newMessage)
            )
        }
    }

    private fun addVideo() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO)
                    }

                    if (report?.isAnyPermissionPermanentlyDenied == true)
                        Helper.showSettingsDialog(
                            this@ChatFragment,
                            "${getString(R.string.storage)} - ${getString(R.string.camera)}"
                        )
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                data?.data?.let {
                    getVideoPath(it)?.let { path ->
                        didPickVideo(path)
                    }
                }
            }
        }
    }


    private fun getVideoPath(uri: Uri): String? {
        val context = this ?: return null

        val projection =
            arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val columnIndex: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

    private fun didPickVideo(path: String) {
        Log.i(TAG, "didPickVideo $path")

        val context = this ?: return
        val progress = showProgress(context)
        repository.newMessageVideo(channel._id, path) { newMessage, alertMessage ->
            progress.dismiss()
            if (newMessage == null) {
                Helper.showToast(context, alertMessage)
                return@newMessageVideo
            }

            NotificationCenter.postNotification(
                App.context,
                NotificationType.NewMessageReceived,
                hashMapOf("new_message" to newMessage)
            )
        }
    }

    companion object {
        private const val TAG = "ChatFragment"
        private const val REQUEST_TAKE_GALLERY_VIDEO = 111

        @JvmStatic
        fun newInstance(context: Context, channel: TChannel, cameFromAd: Boolean = false) =
            context.startActivity(
                Intent(context, ChatFragment::class.java)
                    .putExtra(ARG_CHANNEL, channel)
                    .putExtra(ARG_CAME_FROM_AD, cameFromAd)
            )

    }
}