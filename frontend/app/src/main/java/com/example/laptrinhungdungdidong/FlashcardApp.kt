package com.example.laptrinhungdungdidong

import android.app.Application

class FlashcardApp : Application() {
    companion object {
        lateinit var instance: FlashcardApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
