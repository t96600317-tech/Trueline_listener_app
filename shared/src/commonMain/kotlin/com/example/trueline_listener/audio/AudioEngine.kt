package com.example.trueline_listener.audio

interface AudioEngine {
    fun startRecording(onTick: (Int) -> Unit)
    fun stopRecording(): String?
    fun startPlayback(filePath: String, onProgress: (Float) -> Unit, onComplete: () -> Unit)
    fun stopPlayback()
    fun isPlaying(): Boolean
    fun isRecording(): Boolean
    fun release()
}

expect fun getAudioEngine(): AudioEngine
