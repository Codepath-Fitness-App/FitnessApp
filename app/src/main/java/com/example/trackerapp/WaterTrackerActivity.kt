package com.example.trackerapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.trackerapp.databinding.ActivityWaterTrackerBinding

class WaterTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterTrackerBinding

    private var currentWaterMl = 0
    private val goalWaterMl = 2000
    private val bottleMaxHeight = 280 // matches bottle height in dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updateWaterDisplay()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addThirdBottleButton.setOnClickListener {
            addWater(167)
        }

        binding.addHalfBottleButton.setOnClickListener {
            addWater(250)
        }

        binding.addFullBottleButton.setOnClickListener {
            addWater(500)
        }

        binding.customAddButton.setOnClickListener {
            showCustomAmountDialog()
        }

        binding.resetWaterButton.setOnClickListener {
            currentWaterMl = 0
            updateWaterDisplay()
        }
    }

    private fun addWater(amountMl: Int) {
        currentWaterMl += amountMl
        if (currentWaterMl > goalWaterMl * 2) {
            currentWaterMl = goalWaterMl * 2 // cap at 200%
        }
        updateWaterDisplay()
    }

    private fun updateWaterDisplay() {
        // Update text
        binding.waterAmountTextView.text = "$currentWaterMl ml"
        binding.waterGoalTextView.text = "of $goalWaterMl ml goal"

        // Calculate percentage
        val percentage = (currentWaterMl.toFloat() / goalWaterMl * 100).toInt().coerceAtMost(100)
        binding.percentageTextView.text = "$percentage%"

        // Update water fill height
        binding.bottleContainer.post {
            val maxHeight = binding.bottleContainer.height
            val fillHeight = (maxHeight * (currentWaterMl.toFloat() / goalWaterMl)).coerceAtMost(maxHeight.toFloat())

            val params = binding.waterFillView.layoutParams
            params.height = fillHeight.toInt()
            binding.waterFillView.layoutParams = params
        }
    }

    private fun showCustomAmountDialog() {
        val input = EditText(this).apply {
            hint = "Enter ml"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Custom Amount")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toIntOrNull() ?: 0
                if (amount > 0) addWater(amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}