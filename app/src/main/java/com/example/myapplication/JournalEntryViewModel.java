package com.example.myapplication;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * ViewModel class for managing UI-related data in the lifecycle of the journal app.
 * <br>
 * Any changes to the data are observed and updated in the UI using this view model.
 */
public class JournalEntryViewModel extends AndroidViewModel {
    // Repository instance for handling the data operations
    protected JournalEntryRepository journalEntryRepository;

    /**
     * Constructor for JournalEntryViewModel.
     *
     * @param application The application context.
     */
    public JournalEntryViewModel(Application application) {
        super(application);
        journalEntryRepository = new JournalEntryRepository(application);
    }

    /**
     * Calls repository method to insert a new journal entry into the database.
     *
     * @param entry The journal entry to be inserted.
     */
    public void insertEntry(JournalEntryEntity entry) {
        journalEntryRepository.insertEntry(entry);
    }

    /**
     * Calls repository method to update an existing journal entry in the database.
     *
     * @param entry The journal entry to be updated.
     */
    public void updateEntry(JournalEntryEntity entry) {
        journalEntryRepository.updateEntry(entry);
    }

    /**
     * Calls repository method to delete a journal entry from the database.
     *
     * @param entry The journal entry to be deleted.
     */
    public void deleteEntry(JournalEntryEntity entry) {
        journalEntryRepository.deleteEntry(entry);
    }

    /**
     * Calls repository method to get the LiveData list of all journal entries.
     *
     * @return LiveData list of all journal entries.
     */
    public LiveData<List<JournalEntryEntity>> getAllEntries() {
        return journalEntryRepository.getAllEntries();
    }

    /**
     * Calls repository method to get a specific journal entry by its ID.
     *
     * @param id The ID of the journal entry.
     * @return LiveData object containing the journal entry.
     */
    public LiveData<JournalEntryEntity> getEntryById(int id) {
        return journalEntryRepository.getEntryById(id);
    }
}
