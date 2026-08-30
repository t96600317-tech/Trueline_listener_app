package com.example.trueline_listener.audio

private object IOSAudioEngine : AudioEngine {
    override fun startRecording(onTick: (Int) -> Unit) = onTick(0)
    override fun stopRecording(): String? = null
    override fun startPlayback(filePath: String, onProgress: (Float) -> Unit, onComplete: () -> Unit) {
        onProgress(1f)
        onComplete()
    }
    override fun stopPlayback() = Unit
    override fun isPlaying(): Boolean = false
    override fun isRecording(): Boolean = false
    override fun release() = Unit
}

actual fun getAudioEngine(): AudioEngine = IOSAudioEngine
