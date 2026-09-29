package com.leoaristocrat.cylo.data.local

import androidx.room.migration.Migration

/**
 * Room migrations for [CyloDatabase].
 *
 * Exported schema JSON lives in `app/schemas/` (configured in build.gradle.kts) —
 * commit those files; they are the reference for writing correct migrations.
 */
object CyloMigrations {
    private val MIGRATION_5_6 = Migration(5, 6) { db ->
        db.execSQL("ALTER TABLE tasks ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_5_6)
}

typealias KimonMigrations = CyloMigrations
