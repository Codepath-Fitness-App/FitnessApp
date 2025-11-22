package com.example.trackerapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationWorker(private val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val builder = NotificationCompat.Builder(ctx, "fitness_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to Walk!")
            .setContentText("Get some steps in today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        NotificationManagerCompat.from(ctx).notify(1, builder.build())


        return Result.success()
    }
}