package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a journal entry in the database.
 */
@Entity(tableName = "journal_entries")
public class JournalEntryEntity {
    // Primary key for the journal entry, auto-generated
    @PrimaryKey(autoGenerate = true)
    private int id;
    // Title of the journal entry
    private String title;
    // Content of the journal entry
    private String content;
    // Date of the journal entry, stored as a timestamp
    private long date;
    // List of image paths representing images added in the journal entry
    @TypeConverters(StringListConverter.class)
    private List<String> imagePaths;

    // Getters and setters
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public List<String> getImagePaths() {
        // If no image paths are found, create an empty list
        if(imagePaths == null) {
            imagePaths = new ArrayList<>();
        }
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        // If passed list is null, set an empty list
        this.imagePaths = imagePaths != null ? imagePaths : new ArrayList<>();
    }
}

