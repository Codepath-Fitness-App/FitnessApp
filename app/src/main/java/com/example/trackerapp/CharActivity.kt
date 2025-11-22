package com.example.trackerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit
import java.util.Calendar

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        loadWeeklySteps { entries ->
            val dataSet = LineDataSet(entries, "Last 7 Days Steps")
            lineChart.data = LineData(dataSet)
            lineChart.invalidate()
        }
    }

    private fun loadWeeklySteps(onLoaded: (List<Entry>) -> Unit) {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val start = calendar.timeInMillis

        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(start, end, TimeUnit.MILLISECONDS)
            .build()

        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this)
        Fitness.getHistoryClient(this, account!!)
            .readData(request)
            .addOnSuccessListener { response ->
                val entries = mutableListOf<Entry>()
                var dayIndex = 1

                response.buckets.forEach { bucket ->
                    val dp = bucket.dataSets[0].dataPoints
                    val steps = if (dp.isNotEmpty()) dp[0].getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt() else 0
                    entries.add(Entry(dayIndex.toFloat(), steps.toFloat()))
                    dayIndex++
                }

                onLoaded(entries)
            }
    }
}
