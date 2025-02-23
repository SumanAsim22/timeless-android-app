package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Main activity class for the journal app.
 * <br>
 * This is the starting activity which displays a summarized list of journal entries and provides a
 * FloatingActionButton for adding new entries.
 */
public class MainActivity extends AppCompatActivity {

    // Adapter for the RecyclerView
    private JournalEntryAdapter adapter;
    // RecyclerView for displaying journal entries
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets for proper layout padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the top app bar
        setUpTopAppBar();
        // Set up the FloatingActionButton for adding new entries
        setUpFloatingActionButton();
        // Initialize the ViewModel and observe journal entries
        initializeViewModel();

        // Initialize the RecyclerView for displaying journal entries
        recyclerView = findViewById(R.id.journalEntryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Sets up the top app bar for the main activity.
     * <br>
     * This displays the application title and subtitle in a Material 3 top app bar.
     */
    private void setUpTopAppBar() {
        MaterialToolbar topAppBar = findViewById(R.id.mainTopAppBar);
        setSupportActionBar(topAppBar);
    }

    /**
     * Sets up the FloatingActionButton for adding new entries.
     * <br>
     * Clicking on the button leads to the new entry screen.
     */
    private void setUpFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewEntryActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Initializes the ViewModel and observe journal entries.
     * <br>
     * The ViewModel is used to update data in the UI in case of changes in the database.
     * */
    private void initializeViewModel() {
        JournalEntryViewModel viewModel = new ViewModelProvider(this)
                .get(JournalEntryViewModel.class);
        viewModel.getAllEntries().observe(this, journalEntries -> {
            adapter = new JournalEntryAdapter(journalEntries);
            recyclerView.setAdapter(adapter);
        });
    }
}