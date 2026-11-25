package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TheoryDossierEntity::class,
        BookmarkedDossierEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dossierDao(): DossierDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bookmarked_dossiers` (
                        `id` TEXT NOT NULL,
                        `caseCode` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `originYear` INTEGER NOT NULL,
                        `chronologicalDateLabel` TEXT NOT NULL,
                        `sortTimestamp` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `tractionStatus` TEXT NOT NULL,
                        `summary` TEXT NOT NULL,
                        `originStory` TEXT NOT NULL,
                        `recentUpdates` TEXT NOT NULL,
                        `keyClaimsJson` TEXT NOT NULL,
                        `factualContext` TEXT NOT NULL,
                        `dangersAndLawsJson` TEXT NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `locationName` TEXT,
                        `imageUrl` TEXT,
                        `sourcesJson` TEXT NOT NULL,
                        `searchQueriesJson` TEXT NOT NULL,
                        `isWeeklySpotlight` INTEGER NOT NULL,
                        `weeklySpotlightNote` TEXT,
                        `fetchedAtMillis` INTEGER NOT NULL,
                        `savedLiveNotes` TEXT,
                        `bookmarkedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `bookmarked_dossiers` (
                        `id`, `caseCode`, `title`, `originYear`, `chronologicalDateLabel`,
                        `sortTimestamp`, `category`, `tractionStatus`, `summary`, `originStory`,
                        `recentUpdates`, `keyClaimsJson`, `factualContext`, `dangersAndLawsJson`,
                        `latitude`, `longitude`, `locationName`, `imageUrl`, `sourcesJson`,
                        `searchQueriesJson`, `isWeeklySpotlight`, `weeklySpotlightNote`,
                        `fetchedAtMillis`, `savedLiveNotes`, `bookmarkedAtMillis`
                    )
                    SELECT
                        `id`, `caseCode`, `title`, `originYear`, `chronologicalDateLabel`,
                        `sortTimestamp`, `category`, `tractionStatus`, `summary`, `originStory`,
                        `recentUpdates`, `keyClaimsJson`, `factualContext`, `dangersAndLawsJson`,
                        `latitude`, `longitude`, `locationName`, `imageUrl`, `sourcesJson`,
                        `searchQueriesJson`, `isWeeklySpotlight`, `weeklySpotlightNote`,
                        `fetchedAtMillis`, NULL, `fetchedAtMillis`
                    FROM `dossiers` WHERE `isBookmarked` = 1
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cipherfeed_dossiers.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
