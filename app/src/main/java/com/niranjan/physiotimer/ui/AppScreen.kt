package com.niranjan.physiotimer.ui

import com.niranjan.physiotimer.data.Exercise

internal sealed interface AppScreen {
    data object Home : AppScreen
    data object Progress : AppScreen
    data object Settings : AppScreen
    data class Editor(val initial: Exercise) : AppScreen
    data class Timer(val exercise: Exercise, val sessionId: Long) : AppScreen
    data class Complete(val exercise: Exercise) : AppScreen
}
