package com.thechatters.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.thechatters.app.data.remote.GeminiService
import com.thechatters.app.data.repository.ChatRepository

class ChattersApplication : Application() {
    lateinit var chatRepository: ChatRepository
        private set

    lateinit var geminiService: GeminiService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.d("ChattersApp", "Firebase auto-initialization skipped or already done: ${e.message}")
        }

        chatRepository = ChatRepository(this)
        geminiService = GeminiService()
    }

    companion object {
        lateinit var instance: ChattersApplication
            private set
    }
}
