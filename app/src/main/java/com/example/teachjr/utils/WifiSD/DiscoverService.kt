package com.example.teachjr.utils.WifiSD

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object DiscoverService {

    private val TAG = DiscoverService::class.java.simpleName

    suspend fun startServiceDiscovery(
        serviceRequest: WifiP2pDnsSdServiceRequest, manager: WifiP2pManager, channel: WifiP2pManager.Channel
    ): Int {
        return withContext(Dispatchers.IO) {
            val isServiceRequestCleared = async { removeServiceRequest(serviceRequest, manager, channel) }
            if(isServiceRequestCleared.await()) {

                val isServiceRequested = async { addServiceRequest(serviceRequest, manager, channel) }
                if(isServiceRequested.await()) {

                    val isDiscoveryInitiated = async { discoverServices(manager, channel) }
                    if(isDiscoveryInitiated.await()) {
                        1   // Discovery Initiated Successfully

                    } else {
                        2   // Failed to initiate discovery
                    }

                } else {
                    3   // Failed to add Service Request
                }

            } else {
                4   // Failed to clear Service Requests
            }
        }
    }

    suspend fun removeServiceRequest(
        serviceRequest: WifiP2pDnsSdServiceRequest, manager: WifiP2pManager, channel: WifiP2pManager.Channel
    ): Boolean {
        return suspendCoroutine { continuation ->
            manager?.removeServiceRequest(channel, serviceRequest, object: WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "removeServiceRequest: Service Request Cleared")
//                    Toast.makeText(this@StudentActivity, "Service Request Cleared", Toast.LENGTH_SHORT).show()
                    continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    Log.i(TAG, "removeServiceRequest: Clearing Request failed. Reason-$reason")
//                    Toast.makeText(this@StudentActivity, "Clearing Request failed. Reason-$reason", Toast.LENGTH_SHORT).show()
                    continuation.resume(false)
                }
            })
        }
    }

    suspend fun addServiceRequest(
        serviceRequest: WifiP2pDnsSdServiceRequest, manager: WifiP2pManager, channel: WifiP2pManager.Channel
    ): Boolean {
        return suspendCoroutine { continuation ->
            manager?.addServiceRequest(channel, serviceRequest, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "addServiceRequest: Service Request Added")
//                    Toast.makeText(this@StudentActivity, "Service Request Added", Toast.LENGTH_SHORT).show()
                    continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    Log.i(TAG, "addServiceRequest: Failed to Add Service. Reason-$reason")
//                    Toast.makeText(this@StudentActivity, "Failed to Add Service. Reason-$reason", Toast.LENGTH_SHORT).show()
                    continuation.resume(false)
                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun discoverServices(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel
    ): Boolean {
        return suspendCoroutine { continuation ->
            manager?.discoverServices(channel,  object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "discoverServices: Discovery Started")
//                    Toast.makeText(this@StudentActivity, "Discovery Started", Toast.LENGTH_SHORT).show()
                    continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    Log.i(TAG, "discoverServices: Discovery Failed. Reason-$reason")
//                    Toast.makeText(this@StudentActivity, "Discovery Failed. Reason-$reason", Toast.LENGTH_SHORT).show()

                    when (reason) {
                        WifiP2pManager.P2P_UNSUPPORTED -> {
                            Log.d(TAG, "Wi-Fi Direct isn't supported on this device.")
//                            Toast.makeText(this@StudentActivity, "Wi-Fi Direct isn't supported on this device", Toast.LENGTH_SHORT).show()
                        }
                    }
                    continuation.resume(false)
                }
            }
            )
        }
    }
}