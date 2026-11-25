package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_dossiers ORDER BY sortTimestamp DESC, bookmarkedAtMillis DESC")
    fun observeAllBookmarkedDesc(): Flow<List<BookmarkedDossierEntity>>

    @Query("SELECT * FROM bookmarked_dossiers ORDER BY sortTimestamp ASC, bookmarkedAtMillis ASC")
    fun observeAllBookmarkedAsc(): Flow<List<BookmarkedDossierEntity>>

    @Query("SELECT id FROM bookmarked_dossiers")
    fun observeBookmarkedIds(): Flow<List<String>>

    @Query("SELECT id FROM bookmarked_dossiers")
    suspend fun getAllBookmarkedIds(): List<String>

    @Query("SELECT * FROM bookmarked_dossiers WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<BookmarkedDossierEntity?>

    @Query("SELECT COUNT(*) FROM bookmarked_dossiers")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedDossierEntity)

    @Query("UPDATE bookmarked_dossiers SET savedLiveNotes = :notes WHERE id = :id")
    suspend fun updateLiveNotes(id: String, notes: String)

    @Query("UPDATE bookmarked_dossiers SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: String, category: String)

    @Query("DELETE FROM bookmarked_dossiers WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
}
