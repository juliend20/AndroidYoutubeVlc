package com.geckostudio.androidyoutubevlc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestTask
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geckostudio.androidyoutubevlc.YoutubeDLUtils.initYoutubeDL
import com.geckostudio.androidyoutubevlc.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var player: Player? = null
    private var cast: CastUtils? = null

    private val repository: MainActivityRepository by lazy {
        MainActivityRepository()
    }

    private val viewModel: MainActivityViewModel by lazy {
        val factory = MainActivityViewModel.Factory(repository = repository)
        ViewModelProviders.of(this, factory)[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        initYoutubeDL(application)

        manageShareData(intent)

        // Player
        player = Player(this)
        // Cast
        cast = CastUtils()

        viewModel.videoInfoLiveData.observeForever {
            HUDUtils.dismissDialog()
            if(it != null) {
                binding.copybtn.visibility = View.VISIBLE
                binding.playTvBtn.visibility = View.VISIBLE
                binding.castBtn.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
            }
        }

        binding.launchTV.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "http://192.168.1.90/core/api/jeeApi.php?apikey=pubtXd1qSfHSvBAaf4A6G9WT5t9Obirw&type=cmd&id=831"

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { _ ->
                    Toast.makeText(this, "La télé s'allume", Toast.LENGTH_SHORT).show()
                },
                {
                    Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
                })

            queue.add(stringRequest)
        }
        binding.urlOfYoutube.addTextChangedListener {
            HUDUtils.showDialog(this, "Chargement...")
            viewModel.getStreamingUrl(it.toString())
        }

        binding.copybtn.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("StreamingUrl", viewModel.videoInfoLiveData.value?.url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Le lien a été copié", Toast.LENGTH_SHORT).show()
        }

        binding.playTvBtn.setOnClickListener {
            val videoInfo = viewModel.videoInfoLiveData.value
            if(videoInfo?.url != null) {
                player?.sendToVlc(this, videoInfo.url, videoInfo.title)
            } else {
                Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
            }
        }

        binding.castBtn.setOnClickListener {
            cast?.displayCastMenu(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageShareData(intent)
    }

    private fun manageShareData(intent: Intent?) {
        if(intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                binding.urlOfYoutube.setText(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cast?.release()
    }
    override fun onStart() {
        super.onStart()
        cast?.init(applicationContext, object: ListenerDeviceReady {
            override fun deviceIsReady() {
                cast?.play(viewModel.videoInfoLiveData.value)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cast?.start()
    }

    override fun onPause() {
        super.onPause()
        cast?.stop()
    }

}