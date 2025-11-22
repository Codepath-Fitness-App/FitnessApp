package com.example.trackerapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class ChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var fitManager: FitManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        lineChart = findViewById(R.id.lineChart)
        fitManager = FitManager(this)

        setupChart()

        // Check if we have Google Fit permissions
        if (!fitManager.hasPermissions()) {
            fitManager.requestPermissions(this)
        } else {
            loadWeeklySteps()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FitManager.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (fitManager.hasPermissions()) {
                loadWeeklySteps()
            } else {
                Toast.makeText(this, "Google Fit permissions required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
        }
    }

    private fun loadWeeklySteps() {
        // Use the same account method as FitManager
        val account = GoogleSignIn.getAccountForExtension(this, fitManager.fitnessOptions)

        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val start = calendar.timeInMillis

        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(start, end, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, account)
            .readData(request)
            .addOnSuccessListener { response ->
                val entries = mutableListOf<Entry>()
                val dayLabels = mutableListOf<String>()
                val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

                var dayIndex = 0
                val cal = Calendar.getInstance()
                cal.timeInMillis = start

                response.buckets.forEach { bucket ->
                    val dp = bucket.dataSets.firstOrNull()?.dataPoints
                    val steps = if (!dp.isNullOrEmpty()) {
                        dp[0].getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
                    } else {
                        0
                    }
                    entries.add(Entry(dayIndex.toFloat(), steps.toFloat()))
                    dayLabels.add(dateFormat.format(cal.time))

                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    dayIndex++
                }

                updateChart(entries, dayLabels)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load steps: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateChart(entries: List<Entry>, dayLabels: List<String>) {
        if (entries.isEmpty()) {
            Toast.makeText(this, "No step data available", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = LineDataSet(entries, "Steps").apply {
            color = getColor(R.color.purple_500)
            valueTextColor = getColor(R.color.black)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(getColor(R.color.purple_500))
            setDrawValues(true)
            valueTextSize = 10f
        }

        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
        lineChart.data = LineData(dataSet)
        lineChart.animateX(500)
        lineChart.invalidate()
    }
}