package com.example.trackerapp

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.tasks.Task

class FitManager(private val context: Context) {

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101
    }

    // ✔ Only property, NO getFitnessOptions() function
    val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)


        .build()

    // Check if user already granted permissions
    fun hasPermissions(): Boolean {
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    // Request Google Fit permissions
    fun requestPermissions(activity: Activity) {
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        GoogleSignIn.requestPermissions(
            activity,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            account,
            fitnessOptions
        )
    }

    // Read step count for today
    fun readDailySteps(): Task<Int> {
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

        return Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .continueWith { task ->
                val dataSet = task.result
                if (dataSet != null && !dataSet.isEmpty) {
                    dataSet.dataPoints.first().getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
                } else {
                    0
                }
            }
    }

}
