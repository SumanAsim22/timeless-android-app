package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The JournalEntryViewModelTest class provides unit tests for the {@link JournalEntryViewModel} class
 * using Mockito for mocking dependencies.
 * <br>
 * These tests ensure that the ViewModel's methods correctly interact with the JournalEntryRepository,
 * and handle various scenarios such as valid, null, and non-existent entries.
 */

@RunWith(MockitoJUnitRunner.class)
public class JournalEntryViewModelTest {
    @Mock
    private JournalEntryRepository mockRepository;
    @Mock
    private Application mockApplication;
    private JournalEntryViewModel viewModel;
    private JournalEntryEntity entry;

    @Before
    public void setUp() {
        // Ensure getApplicationContext() returns the mockApplication itself
        when(mockApplication.getApplicationContext()).thenReturn(mockApplication);
        viewModel = new JournalEntryViewModel(mockApplication);
        viewModel.journalEntryRepository = mockRepository; // Injecting the mock repository

        // Create a new JournalEntryEntity instance with preset values
        entry = new JournalEntryEntity();
        entry.setTitle("Test Title");
        entry.setContent("Test Content");
        entry.setDate(System.currentTimeMillis());
        /* Image paths are not explicitly included in this setup i.e., image paths are null.
         * Image handling is tested separately in the ImageHandlerTest test file. */
    }

    @Test
    public void testInsertEntry() {
        // Act
        viewModel.insertEntry(entry);

        // Assert
        ArgumentCaptor<JournalEntryEntity> argumentCaptor = ArgumentCaptor.forClass(JournalEntryEntity.class);
        verify(mockRepository).insertEntry(argumentCaptor.capture());

        JournalEntryEntity capturedEntry = argumentCaptor.getValue();
        assertEquals("Entry should be created with correct title",
                "Test Title", capturedEntry.getTitle());
        assertEquals("Entry should be created with correct content",
                "Test Content", capturedEntry.getContent());
    }

    @Test
    public void testUpdateEntry() {
        // New entry created in setUp with title = Test title

        // Act
        entry.setTitle("Updated Title"); // Update title in entry
        viewModel.updateEntry(entry); // Update entry
        // Assert
        ArgumentCaptor<JournalEntryEntity> argumentCaptor = ArgumentCaptor.forClass(JournalEntryEntity.class);
        verify(mockRepository).updateEntry(argumentCaptor.capture());

        JournalEntryEntity capturedEntry = argumentCaptor.getValue();
        assertEquals("Entry title should be updated", "Updated Title", capturedEntry.getTitle());
    }

    @Test
    public void testDeleteEntry() {
        // Arrange
        entry.setId(1);

        // Act
        viewModel.deleteEntry(entry);

        // Assert
        ArgumentCaptor<JournalEntryEntity> argumentCaptor = ArgumentCaptor.forClass(JournalEntryEntity.class);
        verify(mockRepository).deleteEntry(argumentCaptor.capture());

        JournalEntryEntity capturedEntry = argumentCaptor.getValue();
        assertEquals("Entry should be deleted", 1, capturedEntry.getId());
    }

    /*
     * Edge case test for insert operation on a null entry
     */
    @Test
    public void testInsertNullEntry() {
        // Arrange
        JournalEntryEntity entry = null;

        // Act
        viewModel.insertEntry(entry);

        // Assert
        verify(mockRepository, never()).insertEntry(any(JournalEntryEntity.class));
    }

    /*
     * Edge case test for update operation on an invalid id
     */
    @Test
    public void testUpdateNonExistentEntry() {
        // Arrange
        entry.setId(999999); // Set non-existent id

        // Act
        viewModel.updateEntry(entry);

        // Assert
        ArgumentCaptor<JournalEntryEntity> argumentCaptor = ArgumentCaptor.forClass(JournalEntryEntity.class);
        verify(mockRepository).updateEntry(argumentCaptor.capture());

        JournalEntryEntity capturedEntry = argumentCaptor.getValue();
        assertNotEquals("Entry should not be updated", 9999, capturedEntry.getId());
    }
}

