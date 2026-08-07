package com.stepstack.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ActivityDao_Impl implements ActivityDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DailyActivityEntity> __insertionAdapterOfDailyActivityEntity;

  private final EntityInsertionAdapter<WorkoutSessionEntity> __insertionAdapterOfWorkoutSessionEntity;

  private final EntityInsertionAdapter<StepEventEntity> __insertionAdapterOfStepEventEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSession;

  public ActivityDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDailyActivityEntity = new EntityInsertionAdapter<DailyActivityEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `daily_activity` (`epochDay`,`steps`,`workoutMinutes`,`goalForDay`,`distanceMeters`,`caloriesBurned`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DailyActivityEntity entity) {
        statement.bindLong(1, entity.getEpochDay());
        statement.bindLong(2, entity.getSteps());
        statement.bindLong(3, entity.getWorkoutMinutes());
        statement.bindLong(4, entity.getGoalForDay());
        statement.bindLong(5, entity.getDistanceMeters());
        statement.bindLong(6, entity.getCaloriesBurned());
      }
    };
    this.__insertionAdapterOfWorkoutSessionEntity = new EntityInsertionAdapter<WorkoutSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `workout_sessions` (`id`,`workoutId`,`workoutName`,`dateEpochDay`,`durationActualMin`,`completedPercent`,`exercisesCompleted`,`caloriesBurned`,`startTimeMillis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getWorkoutId());
        statement.bindString(3, entity.getWorkoutName());
        statement.bindLong(4, entity.getDateEpochDay());
        statement.bindLong(5, entity.getDurationActualMin());
        statement.bindLong(6, entity.getCompletedPercent());
        statement.bindLong(7, entity.getExercisesCompleted());
        statement.bindLong(8, entity.getCaloriesBurned());
        statement.bindLong(9, entity.getStartTimeMillis());
      }
    };
    this.__insertionAdapterOfStepEventEntity = new EntityInsertionAdapter<StepEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `step_events` (`id`,`timestamp`,`epochDay`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StepEventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindLong(3, entity.getEpochDay());
      }
    };
    this.__preparedStmtOfDeleteSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM workout_sessions WHERE workoutId = ? AND dateEpochDay = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertActivity(final DailyActivityEntity activity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailyActivityEntity.insert(activity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSession(final WorkoutSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWorkoutSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStepEvent(final StepEventEntity event,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStepEventEntity.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSession(final String id, final long day,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, day);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getActivityForDay(final long day,
      final Continuation<? super DailyActivityEntity> $completion) {
    final String _sql = "SELECT * FROM daily_activity WHERE epochDay = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DailyActivityEntity>() {
      @Override
      @Nullable
      public DailyActivityEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "epochDay");
          final int _cursorIndexOfSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "steps");
          final int _cursorIndexOfWorkoutMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutMinutes");
          final int _cursorIndexOfGoalForDay = CursorUtil.getColumnIndexOrThrow(_cursor, "goalForDay");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final DailyActivityEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpEpochDay;
            _tmpEpochDay = _cursor.getLong(_cursorIndexOfEpochDay);
            final int _tmpSteps;
            _tmpSteps = _cursor.getInt(_cursorIndexOfSteps);
            final int _tmpWorkoutMinutes;
            _tmpWorkoutMinutes = _cursor.getInt(_cursorIndexOfWorkoutMinutes);
            final int _tmpGoalForDay;
            _tmpGoalForDay = _cursor.getInt(_cursorIndexOfGoalForDay);
            final int _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getInt(_cursorIndexOfDistanceMeters);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _result = new DailyActivityEntity(_tmpEpochDay,_tmpSteps,_tmpWorkoutMinutes,_tmpGoalForDay,_tmpDistanceMeters,_tmpCaloriesBurned);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<DailyActivityEntity> observeActivityForDay(final long day) {
    final String _sql = "SELECT * FROM daily_activity WHERE epochDay = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_activity"}, new Callable<DailyActivityEntity>() {
      @Override
      @Nullable
      public DailyActivityEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "epochDay");
          final int _cursorIndexOfSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "steps");
          final int _cursorIndexOfWorkoutMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutMinutes");
          final int _cursorIndexOfGoalForDay = CursorUtil.getColumnIndexOrThrow(_cursor, "goalForDay");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final DailyActivityEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpEpochDay;
            _tmpEpochDay = _cursor.getLong(_cursorIndexOfEpochDay);
            final int _tmpSteps;
            _tmpSteps = _cursor.getInt(_cursorIndexOfSteps);
            final int _tmpWorkoutMinutes;
            _tmpWorkoutMinutes = _cursor.getInt(_cursorIndexOfWorkoutMinutes);
            final int _tmpGoalForDay;
            _tmpGoalForDay = _cursor.getInt(_cursorIndexOfGoalForDay);
            final int _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getInt(_cursorIndexOfDistanceMeters);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _result = new DailyActivityEntity(_tmpEpochDay,_tmpSteps,_tmpWorkoutMinutes,_tmpGoalForDay,_tmpDistanceMeters,_tmpCaloriesBurned);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActivityFrom(final long startDay,
      final Continuation<? super List<DailyActivityEntity>> $completion) {
    final String _sql = "SELECT * FROM daily_activity WHERE epochDay >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DailyActivityEntity>>() {
      @Override
      @NonNull
      public List<DailyActivityEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "epochDay");
          final int _cursorIndexOfSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "steps");
          final int _cursorIndexOfWorkoutMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutMinutes");
          final int _cursorIndexOfGoalForDay = CursorUtil.getColumnIndexOrThrow(_cursor, "goalForDay");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final List<DailyActivityEntity> _result = new ArrayList<DailyActivityEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyActivityEntity _item;
            final long _tmpEpochDay;
            _tmpEpochDay = _cursor.getLong(_cursorIndexOfEpochDay);
            final int _tmpSteps;
            _tmpSteps = _cursor.getInt(_cursorIndexOfSteps);
            final int _tmpWorkoutMinutes;
            _tmpWorkoutMinutes = _cursor.getInt(_cursorIndexOfWorkoutMinutes);
            final int _tmpGoalForDay;
            _tmpGoalForDay = _cursor.getInt(_cursorIndexOfGoalForDay);
            final int _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getInt(_cursorIndexOfDistanceMeters);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            _item = new DailyActivityEntity(_tmpEpochDay,_tmpSteps,_tmpWorkoutMinutes,_tmpGoalForDay,_tmpDistanceMeters,_tmpCaloriesBurned);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllSessions(final Continuation<? super List<WorkoutSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM workout_sessions ORDER BY dateEpochDay DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WorkoutSessionEntity>>() {
      @Override
      @NonNull
      public List<WorkoutSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfWorkoutName = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutName");
          final int _cursorIndexOfDateEpochDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpochDay");
          final int _cursorIndexOfDurationActualMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationActualMin");
          final int _cursorIndexOfCompletedPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "completedPercent");
          final int _cursorIndexOfExercisesCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "exercisesCompleted");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfStartTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMillis");
          final List<WorkoutSessionEntity> _result = new ArrayList<WorkoutSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WorkoutSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWorkoutId;
            _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            final String _tmpWorkoutName;
            _tmpWorkoutName = _cursor.getString(_cursorIndexOfWorkoutName);
            final long _tmpDateEpochDay;
            _tmpDateEpochDay = _cursor.getLong(_cursorIndexOfDateEpochDay);
            final int _tmpDurationActualMin;
            _tmpDurationActualMin = _cursor.getInt(_cursorIndexOfDurationActualMin);
            final int _tmpCompletedPercent;
            _tmpCompletedPercent = _cursor.getInt(_cursorIndexOfCompletedPercent);
            final int _tmpExercisesCompleted;
            _tmpExercisesCompleted = _cursor.getInt(_cursorIndexOfExercisesCompleted);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final long _tmpStartTimeMillis;
            _tmpStartTimeMillis = _cursor.getLong(_cursorIndexOfStartTimeMillis);
            _item = new WorkoutSessionEntity(_tmpId,_tmpWorkoutId,_tmpWorkoutName,_tmpDateEpochDay,_tmpDurationActualMin,_tmpCompletedPercent,_tmpExercisesCompleted,_tmpCaloriesBurned,_tmpStartTimeMillis);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getStepCountForDay(final long day,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM step_events WHERE epochDay = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getStepEventsForDay(final long day,
      final Continuation<? super List<Long>> $completion) {
    final String _sql = "SELECT timestamp FROM step_events WHERE epochDay = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Long> _result = new ArrayList<Long>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Long _item;
            _item = _cursor.getLong(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
