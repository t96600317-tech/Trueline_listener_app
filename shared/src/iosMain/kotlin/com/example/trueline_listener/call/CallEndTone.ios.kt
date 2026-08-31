package com.example.trueline_listener.call

import platform.AudioToolbox.AudioServicesPlaySystemSound

actual fun playCallEndedTone() {
    AudioServicesPlaySystemSound(1057u)
}
