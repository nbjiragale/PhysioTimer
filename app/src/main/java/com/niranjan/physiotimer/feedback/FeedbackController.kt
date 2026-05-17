package com.niranjan.physiotimer.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.niranjan.physiotimer.timer.TimerCue
import java.util.ArrayDeque
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedbackController(context: Context) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var ttsReady = false
    private var voicePlaybackActive = false
    private var motivationPlaybackActive = false
    private val _motivationPlayback = MutableStateFlow(false)
    val motivationPlayback: StateFlow<Boolean> = _motivationPlayback.asStateFlow()
    private var currentVoiceChannel = VoiceChannel.Standard
    private var currentPlayer: MediaPlayer? = null
    private val voiceQueue = ArrayDeque<VoiceRequest>()
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)

    override fun onInit(status: Int) {
        mainHandler.post {
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.language = Locale.getDefault()
                tts?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit

                        override fun onDone(utteranceId: String?) {
                            finishVoiceRequest()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            finishVoiceRequest()
                        }
                    }
                )
                playNextVoiceRequest()
            } else {
                voiceQueue.removeAll { it is VoiceRequest.Tts }
                playNextVoiceRequest()
            }
        }
    }

    fun handleCue(
        cue: TimerCue,
        beepEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        handleCues(listOf(cue), beepEnabled, vibrationEnabled)
    }

    fun handleCues(
        cues: List<TimerCue>,
        beepEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        cues.filterIsInstance<TimerCue.Feedback>().forEach {
            if (beepEnabled) toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            if (vibrationEnabled) vibrate()
        }

        val exactCounterAsset = cues
            .filterIsInstance<TimerCue.PlayExactCounter>()
            .lastOrNull()
            ?.let { ExactCounterAssetCatalog.assetFor(it.durationSeconds) }

        val speakTexts = synchronizedSpeakTexts(cues.filterIsInstance<TimerCue.Speak>().map { it.text })
        if (speakTexts.isNotEmpty() || exactCounterAsset != null) {
            mainHandler.post {
                val onlyNumericSpeak = speakTexts.isNotEmpty() && speakTexts.all { it.toIntOrNull() != null }
                if (motivationPlaybackActive && (exactCounterAsset != null || onlyNumericSpeak)) {
                    return@post
                }

                val requests = buildList {
                    val hasCounterPauseCue = speakTexts.any { text ->
                        VoicePromptCatalog.shouldPauseCounterDuringCue(text)
                    }
                    speakTexts.forEach { text ->
                        val channel = when {
                            text.toIntOrNull() != null -> VoiceChannel.Counting
                            hasCounterPauseCue -> VoiceChannel.Motivation
                            VoicePromptCatalog.shouldPauseCounterDuringCue(text) -> VoiceChannel.Motivation
                            else -> VoiceChannel.Standard
                        }
                        val assetRequests = VoicePromptCatalog.assetsFor(text).map { VoiceRequest.Asset(it, channel) }
                        addAll(assetRequests.ifEmpty { listOf(VoiceRequest.Tts(text, channel)) })
                    }
                    if (exactCounterAsset != null) {
                        add(VoiceRequest.Asset(exactCounterAsset, VoiceChannel.Counting))
                    }
                }
                val hasMotivationRequest = requests.any { it.channel() == VoiceChannel.Motivation }
                val normalizedRequests = if (hasMotivationRequest) {
                    requests.filterNot { it.channel() == VoiceChannel.Counting }
                } else {
                    requests
                }

                if (normalizedRequests.isEmpty()) {
                    return@post
                }
                if (!hasMotivationRequest && exactCounterAsset == null && onlyNumericSpeak && voicePlaybackActive) {
                    return@post
                }
                if (hasMotivationRequest || exactCounterAsset != null) {
                    stopCurrentVoiceRequest()
                }

                voiceQueue.clear()
                voiceQueue.addAll(normalizedRequests)
                playNextVoiceRequest()
            }
        }
    }

    private fun synchronizedSpeakTexts(texts: List<String>): List<String> {
        if (texts.isEmpty()) return emptyList()
        val hasNonNumericCue = texts.any { it.toIntOrNull() == null }
        return if (hasNonNumericCue) {
            texts.filter { it.toIntOrNull() == null }
        } else {
            listOf(texts.last())
        }
    }

    private fun playNextVoiceRequest() {
        if (voicePlaybackActive || voiceQueue.isEmpty()) return

        when (val request = voiceQueue.removeFirst()) {
            is VoiceRequest.Asset -> playAsset(request)
            is VoiceRequest.Tts -> playTts(request)
        }
    }

    private fun playAsset(request: VoiceRequest.Asset) {
        val player = MediaPlayer()
        currentPlayer = player
        voicePlaybackActive = true
        currentVoiceChannel = request.channel
        setMotivationPlaybackActive(request.channel == VoiceChannel.Motivation)
        try {
            appContext.assets.openFd(request.assetPath).use { descriptor ->
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build()
                )
                player.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length
                )
            }
            player.setOnCompletionListener {
                releaseCurrentPlayer()
                voicePlaybackActive = false
                setMotivationPlaybackActive(false)
                currentVoiceChannel = VoiceChannel.Standard
                playNextVoiceRequest()
            }
            player.setOnErrorListener { _, _, _ ->
                releaseCurrentPlayer()
                voicePlaybackActive = false
                setMotivationPlaybackActive(false)
                currentVoiceChannel = VoiceChannel.Standard
                playNextVoiceRequest()
                true
            }
            player.prepare()
            player.start()
        } catch (_: Exception) {
            releaseCurrentPlayer()
            voicePlaybackActive = false
            setMotivationPlaybackActive(false)
            currentVoiceChannel = VoiceChannel.Standard
            playNextVoiceRequest()
        }
    }

    private fun playTts(request: VoiceRequest.Tts) {
        if (!ttsReady) {
            voiceQueue.addFirst(request)
            return
        }

        voicePlaybackActive = true
        currentVoiceChannel = request.channel
        setMotivationPlaybackActive(request.channel == VoiceChannel.Motivation)
        val utteranceId = "physio-${System.nanoTime()}"
        val result = tts?.speak(request.text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            voicePlaybackActive = false
            setMotivationPlaybackActive(false)
            currentVoiceChannel = VoiceChannel.Standard
            playNextVoiceRequest()
        }
    }

    private fun finishVoiceRequest() {
        mainHandler.post {
            voicePlaybackActive = false
            setMotivationPlaybackActive(false)
            currentVoiceChannel = VoiceChannel.Standard
            playNextVoiceRequest()
        }
    }

    private fun releaseCurrentPlayer() {
        currentPlayer?.release()
        currentPlayer = null
    }

    private fun stopCurrentVoiceRequest() {
        currentPlayer?.setOnCompletionListener(null)
        releaseCurrentPlayer()
        tts?.stop()
        voicePlaybackActive = false
        setMotivationPlaybackActive(false)
        currentVoiceChannel = VoiceChannel.Standard
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Vibrator::class.java)
        } ?: return

        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    }

    fun stopAllAudio() {
        mainHandler.post {
            voiceQueue.clear()
            stopCurrentVoiceRequest()
        }
    }

    fun shutdown() {
        stopAllAudio()
        tts?.shutdown()
        tts = null
        toneGenerator.release()
        setMotivationPlaybackActive(false)
    }

    private sealed interface VoiceRequest {
        data class Asset(
            val assetPath: String,
            val channel: VoiceChannel
        ) : VoiceRequest

        data class Tts(
            val text: String,
            val channel: VoiceChannel
        ) : VoiceRequest
    }

    private fun VoiceRequest.channel(): VoiceChannel {
        return when (this) {
            is VoiceRequest.Asset -> channel
            is VoiceRequest.Tts -> channel
        }
    }

    private fun setMotivationPlaybackActive(active: Boolean) {
        motivationPlaybackActive = active
        _motivationPlayback.value = active
    }

    private enum class VoiceChannel {
        Standard,
        Counting,
        Motivation
    }
}

private object ExactCounterAssetCatalog {
    fun assetFor(durationSeconds: Int): String? {
        return when (durationSeconds) {
            10 -> "voices/10sec counter from 1 to 10.mp3"
            20 -> "voices/20sec counter from 1 to 10.mp3"
            else -> null
        }
    }
}
