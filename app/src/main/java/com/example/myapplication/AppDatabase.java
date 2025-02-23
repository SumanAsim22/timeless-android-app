package com.example.myapplication;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Database class for the journal app.
 * <br>
 * This class initializes a single database instance to be used and synchronized throughout the application life cycle.
 * It is implemented as a singleton to ensure that only one instance of the database exists at any given time.
 *
 * @version 3
 */
@Database(entities = {JournalEntryEntity.class}, version = 3)
@TypeConverters(StringListConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    // Define singleton database instance
    private static volatile AppDatabase INSTANCE;

    // Abstract method to access the JournalEntryDao
    public abstract JournalEntryDao journalEntryDao();

    /**
     * Gets the singleton database instance.
     *
     * @param context The application context.
     * @return The singleton AppDatabase instance.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "journal_database")
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration object to handle the schema change from version 2 to 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Rename the imageUris column to imagePaths
            database.execSQL("ALTER TABLE journal_entries RENAME COLUMN imageUris to imagePaths");
        }
    };
}

