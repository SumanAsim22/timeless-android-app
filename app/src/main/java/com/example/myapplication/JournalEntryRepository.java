package com.example.myapplication;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository class for abstracting data operations from the UI.
 * <br>
 * This class accesses the database operations from the JournalEntryDao, allowing the ViewModel
 * to focus on preparing data for the UI.
 * */
public class JournalEntryRepository {
    // DAO instance for accessing database operations
    private final JournalEntryDao journalEntryDao;
    // Executor instance for controlling thread execution
    private final Executor executor;

    /**
     * Constructor for JournalEntryRepository.
     *
     * @param application The application context.
     */
    public JournalEntryRepository(Application application) {
        // Get singleton database instance
        AppDatabase db = AppDatabase.getDatabase(application.getApplicationContext());
        journalEntryDao = db.journalEntryDao();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Inserts a new journal entry into the database.
     *
     * @param entry The journal entry to be inserted.
     */
    public void insertEntry(JournalEntryEntity entry) {
        executor.execute(() -> journalEntryDao.insertEntry(entry));
    }

    /**
     * Updates an existing journal entry in the database.
     *
     * @param entry The journal entry to be updated.
     */
    public void updateEntry(JournalEntryEntity entry) {
        executor.execute(() -> journalEntryDao.updateEntry(entry));
    }

    /**
     * Deletes a journal entry from the database.
     *
     * @param entry The journal entry to be deleted.
     */
    public void deleteEntry(JournalEntryEntity entry) {
        executor.execute(() -> journalEntryDao.deleteEntry(entry));
    }

    /**
     * Gets the LiveData list of all journal entries.
     *
     * @return LiveData list of all journal entries.
     */
    public LiveData<List<JournalEntryEntity>> getAllEntries() {
        return journalEntryDao.getAllEntries();
    }

    /**
     * Retrieves a specific journal entry by its ID.
     *
     * @param id The ID of the journal entry to retrieve.
     * @return LiveData object containing the journal entry.
     */
    public LiveData<JournalEntryEntity> getEntryById(int id) {
        return journalEntryDao.getEntryById(id);
    }
}
