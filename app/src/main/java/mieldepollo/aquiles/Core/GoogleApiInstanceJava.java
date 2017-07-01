package mieldepollo.aquiles.Core;

import android.content.Context;
import android.os.Bundle;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton instance manager for the GoogleApiClient connection
 */
public enum GoogleApiInstanceJava {
    INSTANCE;

    /**
     * Get the GoogleApi Instance
     *
     * @return
     */
    public static GoogleApiInstanceJava get() {
        return INSTANCE;
    }

    /**
     * Initialize the GoogleApi Client Singleton
     *
     * @param context
     * @param apis    APIs to register on the Google Api Client
     */
    public void init(Context context, Api<Api.ApiOptions.NoOptions>... apis) {
        // Setup the GoogleApiClient for this application
        GoogleApiClient.Builder gacBuilder = new GoogleApiClient.Builder(context);
        // Add requested APIs
        for (Api<Api.ApiOptions.NoOptions> api : apis) {
            gacBuilder.addApi(api);
        }
        // Build the Google Api Client
        googleApiClient = gacBuilder.build();
        // Register handlers for the connection events
        handleGoogleApiClientConnectionEvents();
        // Connect to the client
        googleApiClient.connect();
    }

    /**
     * The GoogleApiClient
     */
    private GoogleApiClient googleApiClient;

    /**
     * Get the raw GoogleApiClient object
     *
     * @return
     */
    public GoogleApiClient client() {
        return googleApiClient;
    }

    /**
     * The status of the GoogleApiClient's connection
     */
    private Status currentStatus = Status.NONE;

    /**
     * Get the current status of the GoogleApiClient's connection
     *
     * @return
     */
    public Status status() {
        return currentStatus;
    }

    /**
     * A list of Actions to run when the GoogleApiClient connects
     */
    List<Action<GoogleApiClient>> onConnectActions = new ArrayList<>();

    /**
     * A list of Actions to run when the GoogleApiClient connection is suspended
     */
    List<Action<GoogleApiClient>> onSuspendActions = new ArrayList<>();

    /**
     * A list of Actions to run when the GoogleApiClient connection fails
     */
    List<Action<GoogleApiClient>> onFailedActions = new ArrayList<>();

    /**
     * Call the actions when GoogleApiClient connection events occur
     */
    private void handleGoogleApiClientConnectionEvents() {
        googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                currentStatus = Status.CONNECTED;
                for (Action<GoogleApiClient> action : onConnectActions)
                    action.call(client());
            }

            @Override
            public void onConnectionSuspended(int i) {
                currentStatus = Status.SUSPENDED;
                for (Action<GoogleApiClient> action : onSuspendActions)
                    action.call(client());
            }
        });
        googleApiClient.registerConnectionFailedListener(connectionResult -> {
            currentStatus = Status.FAILED;
            for (Action<GoogleApiClient> action : onFailedActions)
                action.call(client());
        });
    }

    /**
     * Register an action to be invoked when the google api client connects. If useStickyStatus
     * is true, then this will immediately invoke if the current status of the client's connecition
     * is CONNECTED.
     *
     * @param useStickyStatus
     * @param connectAction
     */
    public void connect(boolean useStickyStatus, Action<GoogleApiClient> connectAction) {
        onConnectActions.add(connectAction);
        if (useStickyStatus && status() == Status.CONNECTED) {
            connectAction.call(client());
        }
    }

    /**
     * Remove an action to be executed on GoogleApiClient connect
     *
     * @param connectAction
     * @return
     */
    public boolean removeConnect(Action<GoogleApiClient> connectAction) {
        return onConnectActions.remove(connectAction);
    }

    /**
     * Register an action to be invoked when the google api client connection suspends. If useStickyStatus
     * is true, then this will immediately invoke if the current status of the client's connection
     * is SUSPENDED.
     *
     * @param useStickyStatus
     * @param suspendAction
     */
    public void suspended(boolean useStickyStatus, Action<GoogleApiClient> suspendAction) {
        onConnectActions.add(suspendAction);
        if (useStickyStatus && status() == Status.SUSPENDED) {
            suspendAction.call(client());
        }
    }

    /**
     * Remove an action to be executed on GoogleApiClient connection suspend
     *
     * @param suspendAction
     * @return
     */
    public boolean removeSuspend(Action<GoogleApiClient> suspendAction) {
        return onSuspendActions.remove(suspendAction);
    }

    /**
     * Register an action to be invoked when the google api client connection fails. If useStickyStatus
     * is true, then this will immediately invoke if the current status of the client's connection
     * is FAILED.
     *
     * @param useStickyStatus
     * @param failedAction
     */
    public void failed(boolean useStickyStatus, Action<GoogleApiClient> failedAction) {
        onConnectActions.add(failedAction);
        if (useStickyStatus && status() == Status.FAILED) {
            failedAction.call(client());
        }
    }

    /**
     * Remove an action to be executed on GoogleApiClient connection failed
     *
     * @param failedAction
     * @return
     */
    public boolean removeFailed(Action<GoogleApiClient> failedAction) {
        return onFailedActions.remove(failedAction);
    }


    public enum Status {

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
         * wait for {@link this#CONNECTED} to re-enable them.
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
     *
     * @param <T>
     */
    public interface Action<T> {
        void call(T value);
    }

}