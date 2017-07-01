package mieldepollo.aquiles.Core

import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient
import java.util.ArrayList

/**
 * Singleton instance manager for the GoogleApiClient connection
 */
enum class GoogleApiInstance {
    INSTANCE;

    /**
     * Initialize the GoogleApi Client Singleton

     * @param context
     * *
     * @param apis    APIs to register on the Google Api Client
     */
    fun init(context: Context, vararg apis: Api<Api.ApiOptions.NoOptions>) {
        // Setup the GoogleApiClient for this application
        val gacBuilder = GoogleApiClient.Builder(context)
        // Add requested APIs
        for (api in apis) {
            gacBuilder.addApi(api)
        }
        // Build the Google Api Client
        googleApiClient = gacBuilder.build()
        // Register handlers for the connection events
        handleGoogleApiClientConnectionEvents()
        // Connect to the client
        googleApiClient!!.connect()
    }

    /**
     * The GoogleApiClient
     */
    private var googleApiClient: GoogleApiClient? = null

    /**
     * Get the raw GoogleApiClient object

     * @return
     */
    /**
     * TODO this is a biohazard type risk
     * why you might ask?, well this function @throws NullPointerException when the client is null WHICH WILL HAPPEN
     * but we are going to try to port GoogleApiInstance and use kotlin null pointer handling to soften the ride
     * we are going to be switching between the Java and Kotlin version of this singleton to learn how the behavior changes across the codebase
     *
     * stay tuned ...
     *
     */
    fun client(): GoogleApiClient {
        return googleApiClient ?: throw NullPointerException("Null googleApiClient, F*ck")
    }

    /**
     * The status of the GoogleApiClient's connection
     */
    private var currentStatus = Status.NONE

    /**
     * Get the current status of the GoogleApiClient's connection

     * @return
     */
    fun status(): Status {
        return currentStatus
    }

    /**
     * A list of Actions to run when the GoogleApiClient connects
     */
    internal var onConnectActions: MutableList<Action<GoogleApiClient>> = ArrayList()

    /**
     * A list of Actions to run when the GoogleApiClient connection is suspended
     */
    internal var onSuspendActions: MutableList<Action<GoogleApiClient>> = ArrayList()

    /**
     * A list of Actions to run when the GoogleApiClient connection fails
     */
    internal var onFailedActions: MutableList<Action<GoogleApiClient>> = ArrayList()

    /**
     * Call the actions when GoogleApiClient connection events occur
     */
    private fun handleGoogleApiClientConnectionEvents() {
        googleApiClient!!.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(bundle: Bundle?) {
                currentStatus = Status.CONNECTED
                for (action in onConnectActions)
                    action.call(client())
            }

            override fun onConnectionSuspended(i: Int) {
                currentStatus = Status.SUSPENDED
                for (action in onSuspendActions)
                    action.call(client())
            }
        })
        googleApiClient!!.registerConnectionFailedListener { connectionResult ->
            currentStatus = Status.FAILED
            for (action in onFailedActions)
                action.call(client())
        }
    }

    /**
     * Register an action to be invoked when the google api client connects. If useStickyStatus
     * is true, then this will immediately invoke if the current status of the client's connecition
     * is CONNECTED.

     * @param useStickyStatus
     * *
     * @param connectAction
     */
    fun connect(useStickyStatus: Boolean, connectAction: Action<GoogleApiClient>) {
        onConnectActions.add(connectAction)
        if (useStickyStatus && status() == Status.CONNECTED) {
            connectAction.call(client())
        }
    }

    /**
     * Remove an action to be executed on GoogleApiClient connect

     * @param connectAction
     * *
     * @return
     */
    fun removeConnect(connectAction: Action<GoogleApiClient>): Boolean {
        return onConnectActions.remove(connectAction)
    }

    /**
     * Register an action to be invoked when the google api client connection suspends. If useStickyStatus
     * is true, then this will immediately invoke if the current status of the client's connection
     * is SUSPENDED.

     * @param useStickyStatus
     * *
     * @param suspendAction
     */
    fun suspended(useStickyStatus: Boolean, suspendAction: Action<GoogleApiClient>) {
        onConnectActions.add(suspendAction)
        if (useStickyStatus && status() == Status.SUSPENDED) {
            suspendAction.call(client())
        }
    }

    /**
     * Remove an action to be executed on GoogleApiClient connection suspend

     * @param suspendAction
     * *
     * @return
     */
    fun removeSuspend(suspendAction: Action<GoogleApiClient>): Boolean {
        return onSuspendActions.remove(suspendAction)
    }

    /**
     * Register an action to be invoked when the google api client connection fails. If useStickyStatus
     * is true, then this will immediately invoke if the current status of the client's connection
     * is FAILED.

     * @param useStickyStatus
     * *
     * @param failedAction
     */
    fun failed(useStickyStatus: Boolean, failedAction: Action<GoogleApiClient>) {
        onConnectActions.add(failedAction)
        if (useStickyStatus && status() == Status.FAILED) {
            failedAction.call(client())
        }
    }

    /**
     * Remove an action to be executed on GoogleApiClient connection failed

     * @param failedAction
     * *
     * @return
     */
    fun removeFailed(failedAction: Action<GoogleApiClient>): Boolean {
        return onFailedActions.remove(failedAction)
    }


    enum class Status {

        /**
         * Prior to any events occurring
         */
        NONE,

        /**
         * After calling connect(), this event will be invoked asynchronously when the connect
         * request has successfully completed. After this callback, the application can make
         * requests on other methods provided by the client and expect that no user intervention
         * is required to call methods that use account and scopes provided to the client
         * constructor.
         */
        CONNECTED,

        /**
         * Called when the client is temporarily in a disconnected state. This can happen if there
         * is a problem with the remote service (e.g. a crash or resource problem causes it to be
         * killed by the system). When called, all requests have been canceled and no outstanding
         * listeners will be executed. GoogleApiClient will automatically attempt to restore the
         * connection. Applications should disable UI components that require the service, and
         * wait for [this.CONNECTED] to re-enable them.
         */
        SUSPENDED,

        /**
         * Provides callbacks for scenarios that result in a failed attempt to connect the client
         * to the service. See ConnectionResult for a list of error codes and suggestions for resolution.
         */
        FAILED
        // TODO expand failed to include these details
    }

    /**
     * An interface for running logic on connection events

     * @param <T>
    </T> */
    interface Action<T> {
        fun call(value: T)
    }

    companion object {

        /**
         * Get the GoogleApi Instance

         * @return
         */
        fun get(): GoogleApiInstance {
            return INSTANCE
        }
    }

}
