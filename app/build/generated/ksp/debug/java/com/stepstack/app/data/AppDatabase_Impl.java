package com.stepstack.app.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile WorkoutDao _workoutDao;

  private volatile ActivityDao _activityDao;

  private volatile AchievementDao _achievementDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `workouts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `calories` INTEGER NOT NULL, `equipment` TEXT NOT NULL, `description` TEXT NOT NULL, `exercisesJson` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `thumbnailUrl` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workoutId` TEXT NOT NULL, `workoutName` TEXT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `durationActualMin` INTEGER NOT NULL, `completedPercent` INTEGER NOT NULL, `exercisesCompleted` INTEGER NOT NULL, `caloriesBurned` INTEGER NOT NULL, `startTimeMillis` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_activity` (`epochDay` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `workoutMinutes` INTEGER NOT NULL, `goalForDay` INTEGER NOT NULL, `distanceMeters` INTEGER NOT NULL, `caloriesBurned` INTEGER NOT NULL, PRIMARY KEY(`epochDay`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `step_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievements` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `icon` TEXT NOT NULL, `unlocked` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3cb7c40f85a69b66811b09c92ed71625')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `workouts`");
        db.execSQL("DROP TABLE IF EXISTS `workout_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `daily_activity`");
        db.execSQL("DROP TABLE IF EXISTS `step_events`");
        db.execSQL("DROP TABLE IF EXISTS `achievements`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsWorkouts = new HashMap<String, TableInfo.Column>(11);
        _columnsWorkouts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("durationMin", new TableInfo.Column("durationMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("equipment", new TableInfo.Column("equipment", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("exercisesJson", new TableInfo.Column("exercisesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkouts.put("thumbnailUrl", new TableInfo.Column("thumbnailUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWorkouts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWorkouts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWorkouts = new TableInfo("workouts", _columnsWorkouts, _foreignKeysWorkouts, _indicesWorkouts);
        final TableInfo _existingWorkouts = TableInfo.read(db, "workouts");
        if (!_infoWorkouts.equals(_existingWorkouts)) {
          return new RoomOpenHelper.ValidationResult(false, "workouts(com.stepstack.app.data.WorkoutEntity).\n"
                  + " Expected:\n" + _infoWorkouts + "\n"
                  + " Found:\n" + _existingWorkouts);
        }
        final HashMap<String, TableInfo.Column> _columnsWorkoutSessions = new HashMap<String, TableInfo.Column>(9);
        _columnsWorkoutSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("workoutId", new TableInfo.Column("workoutId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("workoutName", new TableInfo.Column("workoutName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("dateEpochDay", new TableInfo.Column("dateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("durationActualMin", new TableInfo.Column("durationActualMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("completedPercent", new TableInfo.Column("completedPercent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("exercisesCompleted", new TableInfo.Column("exercisesCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("caloriesBurned", new TableInfo.Column("caloriesBurned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkoutSessions.put("startTimeMillis", new TableInfo.Column("startTimeMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWorkoutSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWorkoutSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWorkoutSessions = new TableInfo("workout_sessions", _columnsWorkoutSessions, _foreignKeysWorkoutSessions, _indicesWorkoutSessions);
        final TableInfo _existingWorkoutSessions = TableInfo.read(db, "workout_sessions");
        if (!_infoWorkoutSessions.equals(_existingWorkoutSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "workout_sessions(com.stepstack.app.data.WorkoutSessionEntity).\n"
                  + " Expected:\n" + _infoWorkoutSessions + "\n"
                  + " Found:\n" + _existingWorkoutSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsDailyActivity = new HashMap<String, TableInfo.Column>(6);
        _columnsDailyActivity.put("epochDay", new TableInfo.Column("epochDay", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("steps", new TableInfo.Column("steps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("workoutMinutes", new TableInfo.Column("workoutMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("goalForDay", new TableInfo.Column("goalForDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("distanceMeters", new TableInfo.Column("distanceMeters", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyActivity.put("caloriesBurned", new TableInfo.Column("caloriesBurned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDailyActivity = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDailyActivity = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDailyActivity = new TableInfo("daily_activity", _columnsDailyActivity, _foreignKeysDailyActivity, _indicesDailyActivity);
        final TableInfo _existingDailyActivity = TableInfo.read(db, "daily_activity");
        if (!_infoDailyActivity.equals(_existingDailyActivity)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_activity(com.stepstack.app.data.DailyActivityEntity).\n"
                  + " Expected:\n" + _infoDailyActivity + "\n"
                  + " Found:\n" + _existingDailyActivity);
        }
        final HashMap<String, TableInfo.Column> _columnsStepEvents = new HashMap<String, TableInfo.Column>(3);
        _columnsStepEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStepEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStepEvents.put("epochDay", new TableInfo.Column("epochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStepEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStepEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStepEvents = new TableInfo("step_events", _columnsStepEvents, _foreignKeysStepEvents, _indicesStepEvents);
        final TableInfo _existingStepEvents = TableInfo.read(db, "step_events");
        if (!_infoStepEvents.equals(_existingStepEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "step_events(com.stepstack.app.data.StepEventEntity).\n"
                  + " Expected:\n" + _infoStepEvents + "\n"
                  + " Found:\n" + _existingStepEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsAchievements = new HashMap<String, TableInfo.Column>(5);
        _columnsAchievements.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("icon", new TableInfo.Column("icon", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("unlocked", new TableInfo.Column("unlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAchievements = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAchievements = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAchievements = new TableInfo("achievements", _columnsAchievements, _foreignKeysAchievements, _indicesAchievements);
        final TableInfo _existingAchievements = TableInfo.read(db, "achievements");
        if (!_infoAchievements.equals(_existingAchievements)) {
          return new RoomOpenHelper.ValidationResult(false, "achievements(com.stepstack.app.data.AchievementEntity).\n"
                  + " Expected:\n" + _infoAchievements + "\n"
                  + " Found:\n" + _existingAchievements);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "3cb7c40f85a69b66811b09c92ed71625", "947d72d11969e802100241868e82f10b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "workouts","workout_sessions","daily_activity","step_events","achievements");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `workouts`");
      _db.execSQL("DELETE FROM `workout_sessions`");
      _db.execSQL("DELETE FROM `daily_activity`");
      _db.execSQL("DELETE FROM `step_events`");
      _db.execSQL("DELETE FROM `achievements`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(WorkoutDao.class, WorkoutDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ActivityDao.class, ActivityDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AchievementDao.class, AchievementDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public WorkoutDao workoutDao() {
    if (_workoutDao != null) {
      return _workoutDao;
    } else {
      synchronized(this) {
        if(_workoutDao == null) {
          _workoutDao = new WorkoutDao_Impl(this);
        }
        return _workoutDao;
      }
    }
  }

  @Override
  public ActivityDao activityDao() {
    if (_activityDao != null) {
      return _activityDao;
    } else {
      synchronized(this) {
        if(_activityDao == null) {
          _activityDao = new ActivityDao_Impl(this);
        }
        return _activityDao;
      }
    }
  }

  @Override
  public AchievementDao achievementDao() {
    if (_achievementDao != null) {
      return _achievementDao;
    } else {
      synchronized(this) {
        if(_achievementDao == null) {
          _achievementDao = new AchievementDao_Impl(this);
        }
        return _achievementDao;
      }
    }
  }
}
