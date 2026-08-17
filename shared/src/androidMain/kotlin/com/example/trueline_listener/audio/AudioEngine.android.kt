package com.example.trueline_listener.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File

class AndroidAudioEngine(private val context: Context) : AudioEngine {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var isCurrentlyRecording = false
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun startRecording(onTick: (Int) -> Unit) {
        stopPlayback()
        stopRecording()

        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        try {
            val audioFile = File(context.cacheDir, "voice_intro_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = audioFile

            if (hasMicPermission) {
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(audioFile.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                Log.d("AudioEngine", "Native MediaRecorder started successfully at ${audioFile.absolutePath}")
            } else {
                Log.w("AudioEngine", "RECORD_AUDIO permission not granted yet - fallback simulation mode")
            }

            isCurrentlyRecording = true
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to start native audio recorder: ${e.message}", e)
            isCurrentlyRecording = true
        }
    }

    override fun stopRecording(): String? {
        isCurrentlyRecording = false

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                    Log.d("AudioEngine", "Native MediaRecorder stopped successfully")
                } catch (e: Exception) {
                    Log.e("AudioEngine", "Error stopping recorder: ${e.message}")
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Exception releasing recorder: ${e.message}")
        } finally {
            mediaRecorder = null
        }

        val file = currentRecordingFile
        if (file != null && file.exists() && file.length() > 0) {
            try {
                val bytes = file.readBytes()
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val dataUri = "data:audio/mp4;base64,$base64"
                Log.d("AudioEngine", "Recorded audio encoded to Data URI (${bytes.size} bytes)")
                return dataUri
            } catch (e: Exception) {
                Log.e("AudioEngine", "Failed to encode audio to base64: ${e.message}")
                return file.absolutePath
            }
        }
        return file?.absolutePath ?: (context.cacheDir.absolutePath + "/voice_intro_sample.m4a")
    }

    override fun startPlayback(filePath: String, onProgress: (Float) -> Unit, onComplete: () -> Unit) {
        stopPlayback()

        try {
            val playbackPath: String = if (filePath.startsWith("data:")) {
                val base64Data = filePath.substringAfter(",")
                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                val tempFile = File(context.cacheDir, "playback_temp.m4a")
                tempFile.writeBytes(bytes)
                tempFile.absolutePath
            } else {
                filePath
            }

            val file = File(playbackPath)
            if (file.exists() && file.length() > 0) {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(playbackPath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        progressJob?.cancel()
                        onProgress(1f)
                        onComplete()
                    }
                }
                mediaPlayer = player

                val duration = player.duration.coerceAtLeast(1)
                progressJob?.cancel()
                progressJob = scope.launch {
                    while (player.isPlaying) {
                        val currentPos = player.currentPosition
                        val progress = (currentPos.toFloat() / duration).coerceIn(0f, 1f)
                        onProgress(progress)
                        delay(40)
                    }
                }
            } else {
                // If file is not present on disk, simulate playback progression
                progressJob?.cancel()
                progressJob = scope.launch {
                    val totalSteps = 80
                    for (i in 1..totalSteps) {
                        delay(100)
                        onProgress(i.toFloat() / totalSteps)
                    }
                    onProgress(1f)
                    onComplete()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Playback failed for $filePath", e)
            onComplete()
        }
    }

    override fun stopPlayback() {
        progressJob?.cancel()
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    override fun isRecording(): Boolean = isCurrentlyRecording

    override fun release() {
        stopRecording()
        stopPlayback()
        scope.cancel()
    }
}

private var globalAudioEngine: AudioEngine? = null

fun initAudioEngine(context: Context) {
    globalAudioEngine = AndroidAudioEngine(context.applicationContext)
}

actual fun getAudioEngine(): AudioEngine {
    return globalAudioEngine ?: error("AudioEngine must be initialized with Context in Android MainActivity")
}
