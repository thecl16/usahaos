package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ActiveSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSession(session: ActiveSessionEntity)

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<ActiveSessionEntity?>

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): ActiveSessionEntity?

    @Query("UPDATE active_session SET currentBusinessId = :businessId WHERE id = 1")
    suspend fun updateCurrentBusiness(businessId: String?)

    @Query("DELETE FROM active_session")
    suspend fun clearSession()
}
