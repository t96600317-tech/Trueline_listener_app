package com.example.trueline_listener.call

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

actual fun playCallEndedTone() {
    val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
    Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 220)
}
