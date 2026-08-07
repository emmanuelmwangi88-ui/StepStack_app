package com.stepstack.app.data

import android.content.Context
import androidx.room.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: WorkoutCategory,
    val difficulty: Difficulty,
    val durationMin: Int,
    val calories: Int,
    val equipment: Equipment,
    val description: String,
    val exercisesJson: String, // List<Exercise>
    val isFavorite: Boolean = false,
    val thumbnailUrl: String = ""
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: String,
    val workoutName: String,
    val dateEpochDay: Long,
    val durationActualMin: Int,
    val completedPercent: Int,
    val exercisesCompleted: Int,
    val caloriesBurned: Int,
    val startTimeMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val epochDay: Long,
    val steps: Int,
    val workoutMinutes: Int,
    val goalForDay: Int = 8000,
    val distanceMeters: Int = 0,
    val caloriesBurned: Int = 0
)

@Entity(tableName = "step_events")
data class StepEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val epochDay: Long
)

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkouts(): List<WorkoutEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>)
    
    @Query("UPDATE workouts SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: String)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM daily_activity WHERE epochDay = :day")
    suspend fun getActivityForDay(day: Long): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity WHERE epochDay = :day")
    fun observeActivityForDay(day: Long): kotlinx.coroutines.flow.Flow<DailyActivityEntity?>
    
    @Query("SELECT * FROM daily_activity WHERE epochDay >= :startDay")
    suspend fun getActivityFrom(startDay: Long): List<DailyActivityEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: DailyActivityEntity)

    @Query("SELECT * FROM workout_sessions ORDER BY dateEpochDay DESC")
    suspend fun getAllSessions(): List<WorkoutSessionEntity>
    
    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE workoutId = :id AND dateEpochDay = :day")
    suspend fun deleteSession(id: String, day: Long)

    @Insert
    suspend fun insertStepEvent(event: StepEventEntity)

    @Query("SELECT COUNT(*) FROM step_events WHERE epochDay = :day")
    suspend fun getStepCountForDay(day: Long): Int

    @Query("SELECT timestamp FROM step_events WHERE epochDay = :day")
    suspend fun getStepEventsForDay(day: Long): List<Long>
}

class Converters {
    @TypeConverter
    fun fromWorkoutCategory(value: WorkoutCategory) = value.name
    @TypeConverter
    fun toWorkoutCategory(value: String): WorkoutCategory = 
        try { WorkoutCategory.valueOf(value) } catch (e: Exception) { WorkoutCategory.STRENGTH }
    
    @TypeConverter
    fun fromDifficulty(value: Difficulty) = value.name
    @TypeConverter
    fun toDifficulty(value: String): Difficulty = 
        try { Difficulty.valueOf(value) } catch (e: Exception) { Difficulty.BEGINNER }
    
    @TypeConverter
    fun fromEquipment(value: Equipment) = value.name
    @TypeConverter
    fun toEquipment(value: String): Equipment = 
        try { Equipment.valueOf(value) } catch (e: Exception) { Equipment.NONE }
}

@Database(
    entities = [WorkoutEntity::class, WorkoutSessionEntity::class, DailyActivityEntity::class, StepEventEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun activityDao(): ActivityDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stepstack_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
