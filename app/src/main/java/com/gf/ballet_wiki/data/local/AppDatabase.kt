package com.gf.ballet_wiki.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
        entities = [
            NoteEntity::class,
            EuropeanaEntity::class,
            ComposerEntity::class,
            MultimediaEntity::class
        ],
        version = 2,
        exportSchema = false
         )
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun europeanaDao(): EuropeanaDao
    abstract fun composerDao(): ComposerDao
    abstract fun multimediaDao(): MultimediaDao

    companion object {
        private const val DATABASE_NAME = "ballet_wiki.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                                                )
                        .fallbackToDestructiveMigration()
                        .build().also { instance = it }
            }
        }
    }
}
