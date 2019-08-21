package com.easy.trip.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easy.trip.databases.dao.BookmarkDAO
import com.easy.trip.models.Bookmark

@Database(entities = [Bookmark::class], version = 2)
abstract class EasyTripDatabase: RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDAO

    companion object {
        private var instance: EasyTripDatabase? = null
        fun getInstance(context: Context): EasyTripDatabase{
            if(instance == null) {
                instance = Room.
                    databaseBuilder(context.applicationContext, EasyTripDatabase::class.java, "EasyTripDatabase")
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
            return instance as EasyTripDatabase
        }
        // 如果APP已經上架，且有需要更改資料庫欄位時，必須作遷移的動作
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Bookmarks ADD COLUMN comment TEXT")
            }
        }
    }
}