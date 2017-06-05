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
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.result.DataSourcesResult
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.util.concurrent.TimeUnit


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
            .addScope(Scope(Scopes.FITNESS_LOCATION_READ))
            .addConnectionCallbacks(
                    object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(p0: Bundle?) {
                            Log.i(TagFitClient, "Connected !!!")
                            findFitnessDataSources(mainActivity = mainActivity)
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
fun findFitnessDataSources(mainActivity: MainActivity){
    if(checkPermissions(mainActivity))
    mainActivity.mClient.let { client ->
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.SensorsApi.findDataSources(client, DataSourcesRequest.Builder()
                // At least one dataType must be specified.
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE)
                // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback {
                    ResultCallback<DataSourcesResult> { dataSourcesResult ->
                        Log.i(TagFitClient, "Result " + dataSourcesResult.status.toString())
                        dataSourcesResult.dataSources.forEach { dataSource ->
                            Log.i(TagFitClient, "Result: " + dataSource.toString())
                            Log.i(TagFitClient, "Data Source Type: " + dataSource.dataType.name)
                            if(dataSource.dataType == DataType.TYPE_LOCATION_SAMPLE){
                                mainActivity.mListener ?: registerFitnessDataListener(
                                        dataSource = dataSource,
                                        dataType = DataType.TYPE_LOCATION_SAMPLE,
                                        mainActivity = mainActivity)
                            }
                        }
                    }
                }
    }
}

/**
 * Register a listener with the Sensors API for the provided {@link DataSource} and
 * {@link DataType} combo.
 */
private fun registerFitnessDataListener(dataSource: DataSource, dataType: DataType, mainActivity: MainActivity){
    mainActivity.mListener = OnDataPointListener {dataPoint ->
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
