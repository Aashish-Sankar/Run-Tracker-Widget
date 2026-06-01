package com.marathon.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.marathon.tracker.data.local.entity.TrainingPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {

    @Query("SELECT * FROM training_plans ORDER BY createdAtMillis ASC")
    fun getAll(): Flow<List<TrainingPlanEntity>>

    @Query("SELECT * FROM training_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): TrainingPlanEntity?

    @Query("SELECT * FROM training_plans WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<TrainingPlanEntity?>

    @Upsert
    suspend fun upsert(plan: TrainingPlanEntity)

    @Query("UPDATE training_plans SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM training_plans WHERE id = :id")
    suspend fun deleteById(id: String)
}
