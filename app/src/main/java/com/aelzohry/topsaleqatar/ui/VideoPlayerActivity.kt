package com.aelzohry.topsaleqatar.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.aelzohry.topsaleqatar.R
import kotlinx.android.synthetic.main.activity_video_player.*

private const val ARG_URL = "URL"

class VideoPlayerActivity : AppCompatActivity() {

    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        intent?.let {
            url = it.getStringExtra(ARG_URL)
        }

        play()
    }

    private fun play() {
        val url = url ?: return

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        val uri = Uri.parse(url)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    }

    companion object {
        @JvmStatic
        fun newInstance(fromContext: Context, url: String) =
            Intent(fromContext, VideoPlayerActivity::class.java).apply {
                putExtra(ARG_URL, url)
            }
    }

}