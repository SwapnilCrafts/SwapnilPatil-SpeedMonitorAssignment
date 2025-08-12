package com.test.speedmonitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    /*
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(stepEntity: StepEntity)

    @Query("SELECT * FROM steps ORDER BY date DESC")
    suspend fun getAllSteps(): List<StepEntity>


    @Query("SELECT steps FROM steps WHERE date = :date LIMIT 1")
    fun getStepsForDate(date: String): Int?


     */


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stepEntity: StepEntity)

    @Query("SELECT * FROM steps ORDER BY date DESC")
    fun getAllStepsFlow(): Flow<List<StepEntity>>


}
