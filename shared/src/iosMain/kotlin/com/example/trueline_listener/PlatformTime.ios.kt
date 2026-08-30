package com.example.trueline_listener

import kotlin.time.Clock

actual fun currentPlatformTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
