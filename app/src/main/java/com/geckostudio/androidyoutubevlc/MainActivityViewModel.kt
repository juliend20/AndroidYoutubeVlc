package com.geckostudio.androidyoutubevlc

import androidx.lifecycle.*
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: MainActivityRepository) : ViewModel() {
    private var _videoInfos: MutableList<VideoInfoExtra> = mutableListOf()
    val videoInfosLiveData: MutableLiveData<List<VideoInfoExtra>> by lazy {
        MutableLiveData<List<VideoInfoExtra>>()
    }

    val displayHud: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private var streamingUrl: String? = null

    fun getStreamingUrl(url: String?) = apply {
        if(streamingUrl == url) {
            return@apply
        }
        streamingUrl = url
        displayHud.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            repository.getStreamingUrl(url)?.let {
                if(it.error != null) {
                    return@let
                }
                _videoInfos.add(it)
                videoInfosLiveData.postValue(_videoInfos)
            }
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