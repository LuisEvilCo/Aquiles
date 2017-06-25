package mieldepollo.aquiles.Core

import android.content.Context
import android.os.Bundle
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

class GoogleApiHelper(internal var context: Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object {
        private val TAG = GoogleApiHelper::class.java.simpleName
    }

    var googleApiClient: GoogleApiClient? = null
        internal set

    init {
        buildGoogleApiClient()
        connect()
    }

    fun connect() {
        googleApiClient?.connect()
    }

    fun disconnect() {
        if(googleApiClient?.isConnected ?: false) {
            googleApiClient?.disconnect()
        }
    }

    val isConnected: Boolean
        get() {
            return googleApiClient?.isConnected ?: false
        }

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()

    }

    override fun onConnected(bundle: Bundle?) {
        //You are connected do what ever you want
        //Like i get last known location
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "onConnectionSuspended: googleApiClient.connect()")
        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed: connectionResult.toString() = " + connectionResult.toString())
    }
}
