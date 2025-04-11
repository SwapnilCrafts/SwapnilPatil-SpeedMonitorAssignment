package com.test.speedmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.test.speedmonitor.model.Car
import com.test.speedmonitor.model.RentalSession
import com.test.speedmonitor.model.Renter
import com.test.speedmonitor.service.FirebaseNotifier
import com.test.speedmonitor.service.SpeedMonitorService
import com.test.speedmonitor.ui.theme.SpeedMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedMonitorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Speed Monitor Assignment",
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                val renter = Renter(id = "R1", name = "Swapnil Patil", maxSpeedLimit = 80)
                val car = Car(id = "C1", licensePlate = "MH01AB1234")
                val session = RentalSession(
                    renter = renter,
                    car = car,
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    speedLimit = renter.maxSpeedLimit
                )

                val notifier = FirebaseNotifier()
                val speedMonitorService = SpeedMonitorService(notifier)

                val currentSpeed = 180
                speedMonitorService.processSpeedUpdate(session, currentSpeed)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpeedMonitorTheme {
        Greeting("Speed Monitor Assignment")
    }
}