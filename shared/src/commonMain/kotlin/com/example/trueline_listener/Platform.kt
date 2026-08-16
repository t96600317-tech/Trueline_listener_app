package com.example.trueline_listener

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform