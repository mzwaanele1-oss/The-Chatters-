package com.thechatters.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.thechatters.app.data.local.AppDatabase
import com.thechatters.app.data.remote.GeminiService
import com.thechatters.app.data.repository.ChatRepository
import com.thechatters.app.di.DatabaseModule

class ChattersApplication : Application() {
    lateinit var database: AppDatabase
        private set

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

        // Initialize local Room database instance via DI module
        database = DatabaseModule.provideDatabase(this)

        // Inject database and DAOs into repository
        chatRepository = ChatRepository(
            context = this,
            database = database,
            chatDao = DatabaseModule.provideChatDao(database),
            messageDao = DatabaseModule.provideMessageDao(database),
            userDao = DatabaseModule.provideUserDao(database)
        )
        geminiService = GeminiService()
    }

    companion object {
        lateinit var instance: ChattersApplication
            private set
    }
}
