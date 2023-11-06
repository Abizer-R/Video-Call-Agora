package com.example.teachjr.utils.WifiSD

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object BroadcastService {

    private val TAG = BroadcastService::class.java.simpleName

    suspend fun startServiceBroadcast(
        serviceInfo: WifiP2pDnsSdServiceInfo, manager: WifiP2pManager, channel: WifiP2pManager.Channel
    ): Int {

        return withContext(Dispatchers.IO) {
            val isServiceCleared = async { clearLocalServices(manager, channel) }
            if(isServiceCleared.await()) {

                val isServiceAdded = async { addLocalService(serviceInfo, manager, channel) }
                if(isServiceAdded.await()) {
                    1   // Service Added Successfully

                } else {
                    2   // Failed to add service
                }

            } else {
                3   // Failed to Clear Service
            }
        }
    }

    suspend fun clearLocalServices(manager: WifiP2pManager, channel: WifiP2pManager.Channel): Boolean {
        return suspendCoroutine { continuation ->
            manager?.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "WIFI_SD: clearLocalServices: Services Cleared")
                    continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    Log.i(TAG, "WIFI_SD: clearLocalServices: Failed to clear service: Reason-$reason")
                    continuation.resume(false)
                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun addLocalService(
        serviceInfo: WifiP2pDnsSdServiceInfo, manager: WifiP2pManager, channel: WifiP2pManager.Channel
    ): Boolean {
        return suspendCoroutine { continuation ->
            manager?.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "WIFI_SD: addLocalService: Service Added")
                    continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    Log.i(TAG, "WIFI_SD: addLocalService: Failed to add service: Reason-$reason")
                    continuation.resume(false)
                }
            })
        }
    }
}