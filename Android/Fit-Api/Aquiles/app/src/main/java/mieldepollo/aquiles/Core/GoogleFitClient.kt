package mieldepollo.aquiles.Core

import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import mieldepollo.aquiles.MainActivity
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.result.DataSourcesResult
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.util.concurrent.TimeUnit
import android.content.ContentValues.TAG
import com.google.android.gms.fitness.FitnessStatusCodes



//https://github.com/googlesamples/android-fit/blob/master/StepCounter/app/src/main/java/com/google/android/gms/fit/samples/stepcounter/MainActivity.java


val TagFitClient = "GoogleFitClient"

/**
 *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
 *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
 *  (see documentation for details). Authentication will occasionally fail intentionally,
 *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
 *  can address. Examples of this include the user never having signed in before, or having
 *  multiple accounts on the device and needing to specify which account to use, etc.
 */
fun buildFitnessClient(mainActivity: MainActivity): GoogleApiClient{
    return GoogleApiClient.Builder(mainActivity)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.RECORDING_API)
            .addApi(Fitness.HISTORY_API)
            .addScope(Scope(Scopes.FITNESS_LOCATION_READ))
            .addScope(Scope(Scopes.FITNESS_BODY_READ_WRITE))
            .addScope(Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
            .addScope(Scope(Scopes.FITNESS_NUTRITION_READ_WRITE))
            .addConnectionCallbacks(
                    object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(p0: Bundle?) {
                            Log.i(TagFitClient, "Connected !!!")
                            findFitnessDataSource(mainActivity = mainActivity)
                            subscribeToStepCounter(mainActivity = mainActivity)
                        }

                        override fun onConnectionSuspended(p0: Int) {
                           when(p0){
                               ConnectionCallbacks.CAUSE_NETWORK_LOST -> Log.i(TagFitClient, "Connection Lost")
                               ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED -> Log.i(TagFitClient, "Service Disconnected")
                           }
                        }

                    }
            )
            .enableAutoManage(mainActivity, 0) {
                GoogleApiClient.OnConnectionFailedListener { result ->
                    Log.i(TagFitClient, "Google Play services connection failed. Cause: " +
                            result.toString())
                }
            }
            .build()
}

/**
 * Find available data sources and attempt to register on a specific {@link DataType}.
 * If the application cares about a data type but doesn't care about the source of the data,
 * this can be skipped entirely, instead calling
 *     {@link com.google.android.gms.fitness.SensorsApi
 *     #register(GoogleApiClient, SensorRequest, DataSourceListener)},
 * where the {@link SensorRequest} contains the desired data type.
 */

private fun findFitnessDataSource(mainActivity: MainActivity) {
    // [START find_data_sources]
    // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
    Fitness.SensorsApi.findDataSources(mainActivity.mClient, DataSourcesRequest.Builder()
            // At least one datatype must be specified.
            .setDataTypes(DataType.TYPE_LOCATION_SAMPLE)
            // Can specify whether data type is raw or derived.
            .setDataSourceTypes(DataSource.TYPE_RAW)
            .build())
            .setResultCallback { dataSourcesResult ->
                Log.i(TagFitClient, "Result: " + dataSourcesResult.status.toString())
                for (dataSource in dataSourcesResult.dataSources) {
                    Log.i(TagFitClient, "Data source found: " + dataSource.toString())
                    Log.i(TagFitClient, "Data Source type: " + dataSource.dataType.name)

                    //Let's register a listener to receive Activity data!
                    if (dataSource.dataType == DataType.TYPE_LOCATION_SAMPLE && mainActivity.mListener == null) {
                        Log.i(TagFitClient, "Data source for LOCATION_SAMPLE found!  Registering.")
                        //registerFitnessDataListener(dataSource, DataType.TYPE_LOCATION_SAMPLE)
                    }
                }
            }
    // [END find_data_sources]
}

/**
 * Record step data by requesting a subscription to background step data.
 */
private fun subscribeToStepCounter(mainActivity: MainActivity) {
    // To create a subscription, invoke the Recording API. As soon as the subscription is
    // active, fitness data will start recording.
    Fitness.RecordingApi.subscribe(mainActivity.mClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .setResultCallback { status ->
                if (status.isSuccess) {
                    if (status.statusCode == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                        Log.i(TagFitClient, "Existing subscription for activity detected.")
                    } else {
                        Log.i(TagFitClient, "Successfully subscribed!")
                    }
                } else {
                    Log.w(TagFitClient, "There was a problem subscribing."
                    )
                }
            }
}

/**
 * Register a listener with the Sensors API for the provided {@link DataSource} and
 * {@link DataType} combo.
 */
private fun registerFitnessDataListener(dataSource: DataSource, dataType: DataType, mainActivity: MainActivity){
    mainActivity.mListener = OnDataPointListener { dataPoint ->
        dataPoint.dataType.fields.forEach{ field ->
            Log.i(TagFitClient, "Detected DataPoint field: " + field.name)
            Log.i(TagFitClient, "Detected DataPoint value : " + dataPoint.getValue(field))
        }
    }

    Fitness.SensorsApi.add(
            mainActivity.mClient,
            SensorRequest.Builder()
                    .setDataSource(dataSource) // Optional but recommended for custom data sets
                    .setDataType(dataType) // Can't be omitted
                    .setSamplingRate(10, TimeUnit.SECONDS)
                    .build(),
            mainActivity.mListener
    )
            .setResultCallback { ResultCallback<Status> { status ->
                    if(status.isSuccess){
                        Log.i(TagFitClient, "Listener Register")
                    }else{
                        Log.e(TagFitClient, "Listener Not Register")
                    }
                }
            }
}
