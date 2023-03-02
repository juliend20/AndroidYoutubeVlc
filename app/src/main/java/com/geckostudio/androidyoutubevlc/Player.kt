package com.geckostudio.androidyoutubevlc

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast


class Player(context: Context) {
    fun sendToVlc(activity: Activity, url: String, title: String) {
        val vlcRequestCode = 42
        val uri = Uri.parse(url)
        val vlcIntent = Intent(Intent.ACTION_VIEW)
        vlcIntent.setPackage("org.videolan.vlc")
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*")
        vlcIntent.putExtra("title", title)

        try {
            activity.startActivityForResult(vlcIntent, vlcRequestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "Je ne trouve pas vlc", Toast.LENGTH_SHORT).show()
        }
    }
}