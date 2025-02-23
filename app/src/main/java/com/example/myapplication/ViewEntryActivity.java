package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * View entry activity class for viewing a saved entry in the journal app.
 * <br>
 * This activity displays the saved data, including date, title, content, and images.
 * The saved images are displayed in a carousel component.
 * The activity also provides options to edit or delete the entry being viewed.
 * */
public class ViewEntryActivity extends AppCompatActivity {
    private TextView dateArea;
    private TextView titleArea;
    private TextView contentArea;
    private int entryId;
    private long entryDate;
    private String entryTitle;
    private String entryContent;
    private List<String> entryImagePaths = new ArrayList<>();
    private CarouselAdapter carouselAdapter;
    private JournalEntryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_entry);

        // Apply window insets for proper layout padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the top app bar
        setUpTopAppBar();

        // Set up the ViewModel to observe and update changes
        setUpViewModel();

        // Initialize the RecyclerView for displaying carousel images
        RecyclerView carouselRecyclerView = findViewById(R.id.carouselRecyclerView);
        carouselRecyclerView.setLayoutManager(new CarouselLayoutManager());
        // Allow images to snap to place as the user scrolls through the carousel
        CarouselSnapHelper snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(carouselRecyclerView);
        // Load adapter with saved image paths
        carouselAdapter = new CarouselAdapter(entryImagePaths);
        carouselRecyclerView.setAdapter(carouselAdapter);
    }

    // Initialize the top app bar with the specified menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    /* Setup methods */

    /**
     * Sets up the top app bar for the view entry activity.
     * <br>
     * This displays the main purpose of the activity as a title and provides a back navigation option to return to the main activity.
     * It also includes menu items to edit or delete the entry.
     */
    private void setUpTopAppBar() {
        MaterialToolbar topAppBar = findViewById(R.id.viewEntryTopAppBar);
        setSupportActionBar(topAppBar);
        // Set click listener for the navigation icon in the toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(ViewEntryActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Set click listeners for the menu items in the top app bar
        topAppBar.setOnMenuItemClickListener(item -> {
            // Get the id of the menu item clicked
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                // Execute if the edit icon is clicked
                onEditIconClicked();
                return true;
            } else if (itemId == R.id.action_delete) {
                // Execute if the delete icon is clicked
                onDeleteIconClicked();
                return true;
            }
            return false;
        });
    }

    /**
     * Sets up a ViewModel to observe changes in the journal entry data and update the UI accordingly.
     * <br>
     * The method uses the entry ID sent by the starting activity to get the entry's data and
     * load it in the respective views.
     * */
    private void setUpViewModel() {
        // Get entry id from starting activity
        entryId = getIntent().getIntExtra("entryId", -1);

        // Get views
        dateArea = findViewById(R.id.dateArea);
        titleArea = findViewById(R.id.titleArea);
        contentArea = findViewById(R.id.contentArea);

        viewModel = new ViewModelProvider(this).get(JournalEntryViewModel.class);
        viewModel.getEntryById(entryId).observe(this, entry -> {
            if (entry != null) {

                // Get the values
                entryDate = entry.getDate();
                entryTitle = entry.getTitle();
                entryContent = entry.getContent();

                // Update the UI with the retrieved entry data
                dateArea.setText(DateFormat.format("dd-MM-yyyy", new Date(entryDate)));
                titleArea.setText(entryTitle);
                contentArea.setText(entryContent);

                // Update the carousel with the entry's image paths if available
                if(entryImagePaths != null) {
                    Log.d("Images found", "Yes");
                    entryImagePaths.clear();
                    entryImagePaths.addAll(entry.getImagePaths());
                    carouselAdapter.notifyDataSetChanged();
                }
            }
            else {
                // Show an error message if the entry cannot be loaded
                Toast.makeText(this, R.string.entry_loading_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Event handling methods */

    /**
     * Handles the click event for the edit icon in the top app bar.
     * <br>
     * This method starts the NewEntryActivity with the necessary data to edit an existing entry.
     */
    private void onEditIconClicked() {
        Intent intent = new Intent(ViewEntryActivity.this, NewEntryActivity.class);
        // Inform that the action is to edit the entry
        intent.putExtra("action", "edit");
        // Pass the entry values
        intent.putExtra("entryId", entryId);
        intent.putExtra("date", dateArea.getText().toString());
        intent.putExtra("title", entryTitle);
        intent.putExtra("content", entryContent);
        intent.putStringArrayListExtra("imagePaths", (ArrayList<String>) entryImagePaths);
        // Start the NewEntryActivity in edit mode
        startActivity(intent);
    }

    /**
     * Handles the click event for the delete icon in the top app bar.
     * <br>
     * This method shows a confirmation dialog to delete the journal entry.
     */
    private void onDeleteIconClicked() {

        // Set the details of the confirmation dialog
        new MaterialAlertDialogBuilder(ViewEntryActivity.this)
                .setTitle(R.string.delete_entry_question)
                .setMessage(R.string.delete_entry_message)
                .setNegativeButton(R.string.cancel_button, null)
                .setPositiveButton(R.string.delete_button, (dialog, which) -> {
                    JournalEntryEntity entry = new JournalEntryEntity();
                    entry.setId(entryId);
                    new Thread(() -> {
                        // Delete the entry from the database
                        viewModel.deleteEntry(entry);

                        runOnUiThread(() -> {
                            // Show a toast message indicating the entry was deleted
                            Toast.makeText(ViewEntryActivity.this, R.string.delete_info_message,
                                    Toast.LENGTH_SHORT).show();
                            finish(); // Return to MainActivity
                        });
                    }).start();
                })
                .show();
    }
}
