package com.example.trackerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.trackerapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fitManager: FitManager

    private val activityPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) requestGoogleFit()
            else Toast.makeText(this, "Activity Recognition permission required.", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fitManager = FitManager(this)

        checkActivityRecognitionPermission()

        // View Weekly Chart button
        binding.openChartButton.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        // View Charts button
        binding.chartButton.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        // Refresh Steps button
        binding.refreshButton.setOnClickListener {
            loadDailySteps()
        }

        // Water tracker button
        binding.waterButton.setOnClickListener {
            startActivity(Intent(this, WaterTrackerActivity::class.java))
        }
    }

    // STEP 1: Check physical activity permission
    private fun checkActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            requestGoogleFit()
        }
    }

    // STEP 2: Request Google Fit OAuth
    private fun requestGoogleFit() {
        if (!fitManager.hasPermissions()) {
            fitManager.requestPermissions(this)
        } else {
            loadDailySteps()
        }
    }

    // STEP 3: Load today's step count
    private fun loadDailySteps() {
        fitManager.readDailySteps()
            .addOnSuccessListener { steps ->
                binding.stepCountTextView.text = "$steps"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Google Fit error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // STEP 4: Handle OAuth dialog response
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FitManager.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (fitManager.hasPermissions()) loadDailySteps()
        }
    }
}