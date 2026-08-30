package com.example.trueline_listener

import platform.Foundation.NSDate

actual fun currentPlatformTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1_000).toLong()
