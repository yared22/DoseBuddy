package com.example.dosebuddy.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database class for DoseBuddy application
 * Manages the local SQLite database
 */
@Database(
    entities = {User.class, Medication.class, MedicationHistory.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "dosebuddy_database";
    private static volatile AppDatabase INSTANCE;
    
    /**
     * Get UserDao instance
     * @return UserDao instance
     */
    public abstract UserDao userDao();

    /**
     * Get MedicationDao instance
     * @return MedicationDao instance
     */
    public abstract MedicationDao medicationDao();

    /**
     * Get MedicationHistoryDao instance
     * @return MedicationHistoryDao instance
     */
    public abstract MedicationHistoryDao medicationHistoryDao();
    
    /**
     * Get database instance (Singleton pattern)
     * @param context Application context
     * @return AppDatabase instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Close database instance
     */
    public static void destroyInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}
