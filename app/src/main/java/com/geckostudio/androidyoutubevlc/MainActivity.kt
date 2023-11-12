package com.geckostudio.androidyoutubevlc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestTask
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geckostudio.androidyoutubevlc.YoutubeDLUtils.initYoutubeDL
import com.geckostudio.androidyoutubevlc.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isPause = false
    private var cast: CastUtils? = null

    private val repository: MainActivityRepository by lazy {
        MainActivityRepository()
    }

    private val viewModel: MainActivityViewModel by lazy {
        val factory = MainActivityViewModel.Factory(repository = repository)
        ViewModelProviders.of(this, factory)[MainActivityViewModel::class.java]
    }

    private var adapter: HistoricAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        Log.e("MainActivityLog", "onCreate $savedInstanceState $adapter")

        initYoutubeDL(application)
        manageShareData(intent)

        cast = CastUtils()

        adapter = HistoricAdapter(this) {
            cast?.play(it.videoInfo)
        }

        viewModel.videoInfosLiveData.observe(this) {
            adapter?.videoInfoExtraList = it
        }

        viewModel.displayHud.observe(this) {
            if(it) {
                HUDUtils.showDialog(this, "Chargement...")
            } else {
                HUDUtils.dismissDialog()
            }
        }

        binding.UpdateYoutubeDl.setOnClickListener {
            YoutubeDLUtils.updateYoutubeDL(this)
        }

        binding.launchTV.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "http://192.168.1.90/core/api/jeeApi.php?apikey=pubtXd1qSfHSvBAaf4A6G9WT5t9Obirw&type=cmd&id=831"

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { _ ->
                    Toast.makeText(this, "La télé s'allume/éteint", Toast.LENGTH_SHORT).show()
                },
                {
                    Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
                })

            queue.add(stringRequest)
        }

        binding.urlOfYoutube.addTextChangedListener {
            val pattern = "https://(.*)"
            val match = Regex(pattern).find(it.toString())
            println(match) // MatchResult(value=runn, range=3..7)
            Log.e("MainActivityLog", "addTextChangedListener $it, url: ${match?.groups?.get(0)?.value}")
            viewModel.getStreamingUrl(match?.groups?.get(0)?.value)
        }

        binding.copybtn.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("YoutubeUrl", binding.urlOfYoutube.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Le lien a été copié", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager = LinearLayoutManager(this)


        binding.castBtn.setOnClickListener {
            cast?.displayCastMenu(this)
        }

        binding.rewind.setOnClickListener {
            cast?.rewind()
        }

        binding.playPause.setOnClickListener {
            if(isPause) {
                binding.playPause.setImageResource(R.drawable.ic_pause)
                cast?.resume()
            } else {
                binding.playPause.setImageResource(R.drawable.ic_play)
                cast?.pause()
            }
            isPause = !isPause
        }

        binding.stop.setOnClickListener {
            cast?.stop()
        }

        binding.forward.setOnClickListener {
            cast?.forward()
        }

        binding.close.setOnClickListener {
            cast?.close()
            cast?.stop()
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("MainActivityLog", "onNewIntent")
        manageShareData(intent)
    }

    private fun manageShareData(intent: Intent?) {
        Log.e("MainActivityLog", "manageShareData ${intent.toString()}")
        if(intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                binding.urlOfYoutube.setText(it)
                viewModel.getStreamingUrl(it)
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
                adapter?.castDeviceIsReady = true
                binding.castBtn.setImageResource(R.drawable.ic_cast_connected)
                Toast.makeText(this@MainActivity, "Connecté", Toast.LENGTH_SHORT).show()
            }

            override fun deviceDisplayControl() {
                binding.layoutBtnTV.visibility = View.VISIBLE
            }

            override fun deviceIsDisconnected() {
                adapter?.castDeviceIsReady = false
                binding.castBtn.setImageResource(R.drawable.ic_cast)
                Toast.makeText(this@MainActivity, "Déconnecté", Toast.LENGTH_SHORT).show()
            }

            override fun launchError(message: String) {
                Toast.makeText(this@MainActivity, "Error $message", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cast?.start()
    }

    override fun onPause() {
        super.onPause()
        cast?.stopManager()
    }
}