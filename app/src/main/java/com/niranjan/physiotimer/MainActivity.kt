package com.niranjan.physiotimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.niranjan.physiotimer.data.AppSettingsRepository
import com.niranjan.physiotimer.data.ExerciseRepository
import com.niranjan.physiotimer.data.local.PhysioDatabase
import com.niranjan.physiotimer.feedback.FeedbackController
import com.niranjan.physiotimer.ui.PhysioRepTimerApp
import com.niranjan.physiotimer.ui.theme.PhysioTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember {
                ExerciseRepository(PhysioDatabase.getInstance(applicationContext))
            }
            val appSettingsRepository = remember {
                AppSettingsRepository(applicationContext)
            }
            val feedbackController = remember {
                FeedbackController(applicationContext)
            }
            DisposableEffect(feedbackController) {
                onDispose { feedbackController.shutdown() }
            }

            PhysioTimerTheme {
                PhysioRepTimerApp(
                    repository = repository,
                    appSettingsRepository = appSettingsRepository,
                    feedbackController = feedbackController
                )
            }
        }
    }
}
