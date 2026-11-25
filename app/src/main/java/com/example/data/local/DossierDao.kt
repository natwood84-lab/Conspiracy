package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DossierDao {
    @Query("SELECT * FROM dossiers ORDER BY sortTimestamp DESC, fetchedAtMillis DESC")
    fun observeAllChronologicalDesc(): Flow<List<TheoryDossierEntity>>

    @Query("SELECT * FROM dossiers ORDER BY sortTimestamp ASC, fetchedAtMillis ASC")
    fun observeAllChronologicalAsc(): Flow<List<TheoryDossierEntity>>

    @Query("SELECT * FROM dossiers WHERE isBookmarked = 1 ORDER BY sortTimestamp DESC, fetchedAtMillis DESC")
    fun observeBookmarked(): Flow<List<TheoryDossierEntity>>

    @Query("SELECT * FROM dossiers WHERE isWeeklySpotlight = 1 ORDER BY fetchedAtMillis DESC LIMIT 1")
    fun observeWeeklySpotlight(): Flow<TheoryDossierEntity?>

    @Query("SELECT * FROM dossiers WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<TheoryDossierEntity?>

    @Query("SELECT id FROM dossiers WHERE isBookmarked = 1")
    suspend fun getBookmarkedIds(): List<String>

    @Query("SELECT COUNT(*) FROM dossiers")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TheoryDossierEntity>)

    @Query("UPDATE dossiers SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Query("UPDATE dossiers SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: String, category: String)

    @Query("UPDATE dossiers SET isWeeklySpotlight = 0")
    suspend fun clearWeeklySpotlightFlag()

    @Query("DELETE FROM dossiers WHERE isBookmarked = 0 AND isWeeklySpotlight = 0")
    suspend fun deleteUnbookmarked()
}
