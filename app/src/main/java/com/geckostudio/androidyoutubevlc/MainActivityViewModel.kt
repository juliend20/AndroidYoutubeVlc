package com.geckostudio.androidyoutubevlc

import androidx.lifecycle.*
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: MainActivityRepository) : ViewModel() {
    val videoInfoLiveData: MutableLiveData<VideoInfo> by lazy {
        MutableLiveData<VideoInfo>()
    }

    val displayHud: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    var streamingUrl: String? = null

    fun getStreamingUrl(url: String?) = apply {
        if(streamingUrl == url) {
            return@apply
        }
        streamingUrl = url
        displayHud.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val videoInfo = repository.getStreamingUrl(url)
            videoInfoLiveData.postValue(videoInfo?.videoInfo)
            displayHud.postValue(false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MainActivityRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(repository) as T
        }
    }
}