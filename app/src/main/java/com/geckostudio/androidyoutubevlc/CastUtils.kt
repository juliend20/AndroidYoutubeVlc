package com.geckostudio.androidyoutubevlc

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.AdapterView
import com.connectsdk.core.MediaInfo
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.device.DevicePicker
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.service.DeviceService
import com.connectsdk.service.capability.MediaControl
import com.connectsdk.service.capability.MediaPlayer
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.sessions.LaunchSession
import com.connectsdk.service.sessions.WebAppSession
import com.yausername.youtubedl_android.mapper.VideoInfo
import java.net.SocketException

class CastUtils {
    private var mDiscoveryManager: DiscoveryManager? = null
    private var mDevice: ConnectableDevice? = null
    private var mWebAppSession: WebAppSession? = null
    private var listenerDeviceReady: ListenerDeviceReady? = null

    private var mLaunchSession: LaunchSession? = null
    private var mMediaControl: MediaControl? = null

    fun init(applicationContext: Context, listenerDeviceReady: ListenerDeviceReady) {
        this.listenerDeviceReady = listenerDeviceReady
        try {
            // DLNA
            DiscoveryManager.init(applicationContext)
            // This step could even happen in your app's delegate
            mDiscoveryManager = DiscoveryManager.getInstance()
        } catch (_: SocketException) {

        }
    }

    fun start() {
        mDiscoveryManager?.start()
    }

    fun stopManager() {
        mDiscoveryManager?.stop()
    }

    fun release() {
        mWebAppSession?.disconnectFromWebApp()
    }

    fun displayCastMenu(activity: Activity) {
        val selectDevice = AdapterView.OnItemClickListener { adapterView, _, position, _ ->
            mDevice = adapterView.getItemAtPosition(position) as ConnectableDevice
            mDevice?.addListener(listener)
            if(mDevice?.isConnected == true)
                mDevice?.disconnect()
            mDevice?.connect()
        }

        val devicePicker = DevicePicker(activity)
        val dialog: AlertDialog = devicePicker.getPickerDialog("Périphériques detectés", selectDevice)
        dialog.show()
    }

    fun play(videoinfo: VideoInfo?) {
        videoinfo?.let { videoInfo ->
            val mediaURL = videoInfo.url
            val iconURL = videoInfo.thumbnail
            val title = videoInfo.title
            val description = videoInfo.description
            val mimeType = "video/mp4"

            val mediaInfo = MediaInfo.Builder(mediaURL, mimeType)
                .setTitle(title)
                .setDescription(description)
                .setIcon(iconURL)
                .build()

            mDevice?.mediaPlayer?.playMedia(mediaInfo, false, mLaunchListener)
        }
    }

    fun resume() {
        mMediaControl?.play(null)
    }
    fun pause() {
        mMediaControl?.pause(null)
    }

    fun stop() {
        mMediaControl?.stop(null)
    }

    fun close() {
        mDevice?.removeListener(listener)
        mDevice?.disconnect()
        mDevice = null
    }

    private val mLaunchListener: MediaPlayer.LaunchListener = object : MediaPlayer.LaunchListener {
        override fun onError(error: ServiceCommandError) {
            Log.e("MainActivityLog", "Could not launch image: $error")
        }

        override fun onSuccess(mediaLaunchObject: MediaPlayer.MediaLaunchObject) {
            mLaunchSession = mediaLaunchObject.launchSession
            mMediaControl = mediaLaunchObject.mediaControl

            listenerDeviceReady?.deviceDisplayControl()

            Log.e("MainActivityLog", "Successfully launched video! $mLaunchSession $mMediaControl")
        }
    }

    private val listener = object: ConnectableDeviceListener {
        override fun onDeviceReady(device: ConnectableDevice?) {
            Log.e("MainActivityLog", "onDeviceReady $device")

            mDevice = device

            listenerDeviceReady?.deviceIsReady()
        }

        override fun onDeviceDisconnected(device: ConnectableDevice?) {
            Log.e("MainActivityLog", "onDeviceDisconnected $device")
        }

        override fun onPairingRequired(
            device: ConnectableDevice?,
            service: DeviceService?,
            pairingType: DeviceService.PairingType?
        ) {
            Log.e("MainActivityLog", "onPairingRequired $device $service $pairingType")
        }

        override fun onCapabilityUpdated(
            device: ConnectableDevice?,
            added: MutableList<String>?,
            removed: MutableList<String>?
        ) {
            Log.e("MainActivityLog", "onCapabilityUpdated $device $added $removed")
        }

        override fun onConnectionFailed(device: ConnectableDevice?, error: ServiceCommandError?) {
            Log.e("MainActivityLog", "onConnectionFailed $device $error")
        }
    }
}

interface ListenerDeviceReady {
    fun deviceIsReady()
    fun deviceDisplayControl()
}