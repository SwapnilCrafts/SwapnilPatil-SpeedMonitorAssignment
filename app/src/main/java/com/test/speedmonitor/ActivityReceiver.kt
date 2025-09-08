package com.test.speedmonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityReceiver : BroadcastReceiver() {
    companion object {
        var isInVehicle = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent!!)
            val mostProbable = result?.mostProbableActivity
            val type = mostProbable?.type

            isInVehicle = (type == DetectedActivity.IN_VEHICLE ||
                    type == DetectedActivity.ON_BICYCLE)
        }
    }
}
