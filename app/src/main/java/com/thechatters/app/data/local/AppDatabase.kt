package com.thechatters.app.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thechatters.app.data.model.Chat
import com.thechatters.app.data.model.Message
import com.thechatters.app.data.model.User
import com.thechatters.app.di.DatabaseModule

@Database(
    entities = [Chat::class, Message::class, User::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "the_chatters_database"

        /**
         * Builds and configures the Room database with fallback migration and lifecycle callbacks.
         */
        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("AppDatabase", "Database created: $DATABASE_NAME")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d("AppDatabase", "Database opened: $DATABASE_NAME")
                    }
                })
                .build()
        }

        /**
         * Returns the singleton instance managed by [DatabaseModule].
         */
        fun getDatabase(context: Context): AppDatabase {
            return DatabaseModule.provideDatabase(context)
        }
    }
}
