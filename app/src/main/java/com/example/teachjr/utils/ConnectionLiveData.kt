package com.example.teachjr.utils

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData

class ConnectionLiveData(
    private val connectivityManager: ConnectivityManager): LiveData<Boolean>() {

    constructor(context: Context): this(
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    )

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i(ContentValues.TAG, "TESTING_onAvailable: Network ${network} is Available")
            postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(ContentValues.TAG, "TESTING_onLost: ${network} Network Lost")
            postValue(false)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities) {
            val isInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            Log.d(ContentValues.TAG, "TESTING_networkCapabilities: ${network} $networkCapabilities")

            val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            if (isValidated){
                Log.d(ContentValues.TAG, "TESTING_hasCapability: ${network} $networkCapabilities")
            } else{
                Log.d(ContentValues.TAG, "TESTING: Network has No Connection Capability: ${network} $networkCapabilities")
            }
            postValue(isInternet && isValidated)
        }

    }

    override fun onActive() {
        super.onActive()
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(
            builder.addCapability(NET_CAPABILITY_INTERNET).build(),
            networkCallback
        )
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


}