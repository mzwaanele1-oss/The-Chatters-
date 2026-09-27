package com.thechatters.app.di

import android.content.Context
import com.thechatters.app.data.local.AppDatabase
import com.thechatters.app.data.local.ChatDao
import com.thechatters.app.data.local.MessageDao
import com.thechatters.app.data.local.UserDao

/**
 * Dependency injection module and singleton provider for the local Room database and DAOs.
 * Provides thread-safe, double-checked locking singleton access to [AppDatabase] and its
 * respective Data Access Objects (DAOs) to support offline data caching across the app.
 */
object DatabaseModule {
    @Volatile
    private var databaseInstance: AppDatabase? = null

    /**
     * Provides the thread-safe singleton [AppDatabase] instance.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            databaseInstance ?: AppDatabase.buildDatabase(context.applicationContext).also {
                databaseInstance = it
            }
        }
    }

    /**
     * Provides [ChatDao] from the provided context (resolving singleton database).
     */
    fun provideChatDao(context: Context): ChatDao {
        return provideDatabase(context).chatDao()
    }

    /**
     * Provides [ChatDao] directly from the database instance.
     */
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }

    /**
     * Provides [MessageDao] from the provided context (resolving singleton database).
     */
    fun provideMessageDao(context: Context): MessageDao {
        return provideDatabase(context).messageDao()
    }

    /**
     * Provides [MessageDao] directly from the database instance.
     */
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    /**
     * Provides [UserDao] from the provided context (resolving singleton database).
     */
    fun provideUserDao(context: Context): UserDao {
        return provideDatabase(context).userDao()
    }

    /**
     * Provides [UserDao] directly from the database instance.
     */
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Closes and clears the singleton database instance. Useful for tests or migrations.
     */
    fun resetDatabase() {
        synchronized(this) {
            databaseInstance?.close()
            databaseInstance = null
        }
    }
}
