package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO (Data Access Object) interface for journal entries.
 * <br>
 * This interface provides methods for basic database operations related to the journal entries.
 */
@Dao
public interface JournalEntryDao {

    /**
     * Inserts a new journal entry into the database.
     *
     * @param entry The journal entry to be inserted.
     */
    @Insert
    void insertEntry(JournalEntryEntity entry);

    /**
     * Updates an existing journal entry in the database.
     *
     * @param entry The journal entry to be updated.
     */
    @Update
    void updateEntry(JournalEntryEntity entry);

    /**
     * Deletes a journal entry from the database.
     *
     * @param entry The journal entry to be deleted.
     */
    @Delete
    void deleteEntry(JournalEntryEntity entry);

    /**
     * Gets the LiveData list of all journal entries.
     *
     * @return LiveData list of all journal entries.
     */
    @Query("SELECT * FROM journal_entries")
    LiveData<List<JournalEntryEntity>> getAllEntries();

    /**
     * Retrieves a specific journal entry by its ID.
     *
     * @param id The ID of the journal entry to retrieve.
     * @return LiveData object containing the journal entry.
     */
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    LiveData<JournalEntryEntity> getEntryById(int id);
}
