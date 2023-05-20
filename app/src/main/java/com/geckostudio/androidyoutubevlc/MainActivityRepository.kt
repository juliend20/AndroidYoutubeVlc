package com.geckostudio.androidyoutubevlc

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.yausername.youtubedl_android.mapper.VideoInfo

class MainActivityRepository {

    fun getStreamingUrl(url: String?): VideoInfoExtra? {
        Log.e("MainActivity", "getStreamingUrl: $url")
        if(url != null) {
            return YoutubeDLUtils.extractVideoInfo(url)
        }
        return null
    }
}