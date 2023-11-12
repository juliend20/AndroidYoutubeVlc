package com.geckostudio.androidyoutubevlc

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.geckostudio.androidyoutubevlc.databinding.ItemCellBinding


class HistoricAdapter(private val context: Context, val block: (VideoInfoExtra) -> Unit): RecyclerView.Adapter<HistoricAdapter.VideoInfoRecyclerViewHolder>() {
    var videoInfoExtraList: List<VideoInfoExtra> = emptyList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    var castDeviceIsReady = false
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    private var player: Player? = null

    init {
        player = Player()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoInfoRecyclerViewHolder {
        val binding = ItemCellBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoInfoRecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return videoInfoExtraList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VideoInfoRecyclerViewHolder, position: Int) {
        val videoInfo = videoInfoExtraList[position]
        val url = videoInfo.videoInfo?.url
        val title = videoInfo.videoInfo?.title
        val artist = videoInfo.videoInfo?.uploader
        val duration = videoInfo.videoInfo?.duration

        holder.binding.streamingUrl.setText(url)
        holder.binding.title.text = title
        if(duration == 0)
            holder.binding.artistAndDuration.text = "$artist"
        else {
            val durationMin = duration?.div(60).toString().plus(":").plus(duration?.rem(60))
            holder.binding.artistAndDuration.text = "$artist - $durationMin"
        }

        holder.binding.copyStreamingbtn.setOnClickListener {
            val clipboard: ClipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("StreamingUrl", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Le lien streaming a été copié", Toast.LENGTH_SHORT).show()
        }

        holder.binding.launchVlc.setOnClickListener {
            if(url != null && title != null) {
                player?.sendToVlc(context, url, title)
            } else {
                Toast.makeText(context, "Une erreur est survenue", Toast.LENGTH_SHORT).show()
            }
        }

        holder.binding.playTvBtn.setOnClickListener {
            block(videoInfo)
        }

        holder.binding.playTvBtn.visibility = if(castDeviceIsReady) View.VISIBLE else View.GONE
    }

    inner class VideoInfoRecyclerViewHolder(val binding: ItemCellBinding): RecyclerView.ViewHolder(binding.root)
}

