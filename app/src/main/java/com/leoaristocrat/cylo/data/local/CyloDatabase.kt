package com.leoaristocrat.cylo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leoaristocrat.cylo.data.local.dao.FocusSessionDao
import com.leoaristocrat.cylo.data.local.dao.SleepSessionDao
import com.leoaristocrat.cylo.data.local.dao.TagDao
import com.leoaristocrat.cylo.data.local.dao.TaskDao
import com.leoaristocrat.cylo.data.local.entity.FocusSessionEntity
import com.leoaristocrat.cylo.data.local.entity.SleepSessionEntity
import com.leoaristocrat.cylo.data.local.entity.TagEntity
import com.leoaristocrat.cylo.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FocusSessionEntity::class, TagEntity::class, TaskEntity::class, SleepSessionEntity::class],
    version = 6,
    exportSchema = true
)
abstract class CyloDatabase : RoomDatabase() {

    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun tagDao(): TagDao
    abstract fun taskDao(): TaskDao
    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile
        private var INSTANCE: CyloDatabase? = null

        fun getInstance(context: Context): CyloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CyloDatabase::class.java,
                    // Intentionally preserved internal persistent database name to maintain
                    // complete backward compatibility and avoid wiping existing user data.
                    "kimon_database.db"
                )
                    .addMigrations(*CyloMigrations.ALL)
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with default tags
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).tagDao().insertAll(
                                    listOf(
                                        TagEntity(name = "Study", colorHex = "#7C4DFF", iconName = "ic_sparkles"),
                                        TagEntity(name = "Work", colorHex = "#2979FF", iconName = "ic_briefcase"),
                                        TagEntity(name = "Coding", colorHex = "#00B0FF", iconName = "ic_terminal"),
                                        TagEntity(name = "Reading", colorHex = "#00E676", iconName = "ic_book"),
                                        TagEntity(name = "Design", colorHex = "#FF9100", iconName = "ic_palette")
                                    )
                                )
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    getInstance(context).sleepSessionDao().removeDuplicateSessions()
                                } catch (_: Exception) {}
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

typealias KimonDatabase = CyloDatabase
