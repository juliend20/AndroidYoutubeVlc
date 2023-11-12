package com.geckostudio.androidyoutubevlc

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import io.reactivex.Observable

object YoutubeDLUtils {
    private var updating = false
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    /**********************************************************************************************/
    /*                                         Extractor                                          */
    /**********************************************************************************************/

    fun initYoutubeDL(application: Application) {
        try {
            YoutubeDL.getInstance().init(application)
        } catch (e: YoutubeDLException) {
            Log.e("Utils", "failed to initialize youtubedl-android", e)
        }
    }

    fun extractVideoInfo(url: String): VideoInfoExtra? {
        if(url.isEmpty()) return null

        Log.e("Utils", "extractVideoInfo URL $url")
        var error: String? = null
        repeat(2) {
            val request = YoutubeDLRequest(url)
            request.addOption("-f", "best")

            try {
                val videoInfo = YoutubeDL.getInstance().getInfo(request)
                Log.e("Utils", "extractVideoInfo title ${videoInfo.title}")
                return VideoInfoExtra(videoInfo)
            } catch (re: RuntimeException) {
                Log.e("Utils", "extractVideoInfo Error $url $re")
            } catch (e: YoutubeDLException) {
                Log.e("Utils", "failed to extractVideoInfo", e)
                val error = if(e.message?.contains("This video is not available") == true) {
                    "La musique n'est pas disponible"
                } else {
                    e.message
                }
                return VideoInfoExtra(null, error)
            } catch (e: Exception) {
                Log.e("Utils", "failed to extractVideoInfo", e)
                if(e.message?.contains("playlist exist?") == true) {
                    error = "Une erreur est survenue dans l'extraction de la vidéo. (Playlist?)"
                } else {
                    error = "Une erreur est survenue dans l'extraction de la vidéo"
                }
            }
            Thread.sleep(50)
        }

        return VideoInfoExtra(null, error)
    }

    fun updateYoutubeDL(context: Context) {
        if(updating) return
        updating = true
        compositeDisposable.add(
            Observable.fromCallable {
                YoutubeDL.getInstance().updateYoutubeDL(context)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(context, "Youtube-dl mis à jour", Toast.LENGTH_SHORT).show()
                    updating = false
                }, {
                    Toast.makeText(context, "Erreur lors de la mise à jour de youtube-dl", Toast.LENGTH_SHORT).show()
                    updating = false
                })
        )
    }
}

data class VideoInfoExtra(val videoInfo: VideoInfo?, val error: String? = null)