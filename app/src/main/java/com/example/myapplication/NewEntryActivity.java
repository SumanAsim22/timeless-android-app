package com.example.myapplication;

import static android.Manifest.permission.READ_MEDIA_IMAGES;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * New entry activity class for creating a new entry in the journal app.
 * <br>
 * This activity displays relevant input fields for creating a journal entry, including title and content.
 * It also includes a date picker for selecting a date value and a FloatingActionButton to add images.
 * */
public class NewEntryActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private int entryId;
    private EditText dateEditText;
    private EditText titleEditText;
    private EditText contentEditText;
    private Button saveButton;
    private final int allowedImageSelections = 5;
    private boolean entrySaved;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private CarouselAdapter carouselAdapter;
    private String startingAction;
    private List<String> tempImagePaths;
    private ImageHandler imageHandler;
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_entry);

        // Apply window insets for proper layout padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tempImagePaths = new ArrayList<>();
        // All image handling operations are delegated to the imageHandler object
        imageHandler = new ImageHandler(getContentResolver(), getCacheDir(), getFilesDir(), tempImagePaths);

        // Set up the top app bar
        setUpTopAppBar();
        // Set up the FloatingActionButton for adding images
        setUpFloatingActionButton();

        // Initialize the RecyclerView for displaying carousel images
        RecyclerView carouselRecyclerView = findViewById(R.id.carouselRecyclerView);
        carouselRecyclerView.setLayoutManager(new CarouselLayoutManager());
        // Allow images to snap to place as the user scrolls through the carousel
        CarouselSnapHelper snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(carouselRecyclerView);
        // Load adapter with selected image paths
        carouselAdapter = new CarouselAdapter(tempImagePaths);
        carouselRecyclerView.setAdapter(carouselAdapter);

        dateEditText = findViewById(R.id.dateEditText);
        // Open date picker when date field is clicked
        dateEditText.setOnClickListener(v -> initializeDatePicker());
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        saveButton = findViewById(R.id.saveButton);
        // Set up save button functionality
        saveButton.setOnClickListener(v -> onSaveButtonClicked());

        // Get starting activity action from intent extra
        startingAction = getIntent().getStringExtra("action");
        // If activity is called to edit an entry, set up for editing
        if ("edit".equals(startingAction)) {
            setUpActivityForEdit();
        }

        // Register for activity result to get multiple media selections from photo picker
        pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(allowedImageSelections),
                this::handleMediaSelection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission is granted, proceed with photo picker
                openPhotoPicker(5);
            } else {
                // Get reference to the parent layout
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /* Clean up code for images if the entry is not saved or updated */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Delete temporary images from storage if the entry was not saved
        if (!entrySaved) {
            imageHandler.deleteTemporaryImages();
        }
    }

    /* Setup methods */

    /**
     * Sets up the top app bar for the new entry activity.
     * <br>
     * This displays the main purpose of the activity as a title and provides a back navigation option to return to the main activity.
     */
    private void setUpTopAppBar() {
        topAppBar = findViewById(R.id.newEntryTopAppBar);
        setSupportActionBar(topAppBar);
        // Set click listener for the navigation icon in the toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(NewEntryActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Sets up the FloatingActionButton for adding images.
     * <br>
     * Clicking on the button opens the PhotoPicker with the relevant selection constraints.
     */
    private void setUpFloatingActionButton() {
        FloatingActionButton addImageButton = findViewById(R.id.imageFab);
        // Set click listener to open the PhotoPicker
        addImageButton.setOnClickListener(v -> {
            // Check if the permission to read media images is granted
            if (ContextCompat.checkSelfPermission(NewEntryActivity.this, READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                // Calculate the remaining number of images that can be selected
                int remainingSelections = allowedImageSelections - tempImagePaths.size();
                /*
                * When multiple selections are enabled, the PhotoPicker component enforces a minimum of 2 selections.
                * Thus, the PhotoPicker is set to open only if at least 2 selections are remaining.
                */
                if (remainingSelections >= 2) {
                    // Open the PhotoPicker with the correct constraints
                    openPhotoPicker(remainingSelections);
                } else if (remainingSelections == 1) {
                    // Display a message if only 1 selection is remaining
                    Snackbar.make(addImageButton,
                            R.string.one_selection_remaining_error,
                                    Snackbar.LENGTH_LONG).setAnchorView(addImageButton).show();
                } else {
                    // Display a message if the maximum number of images is reached
                    Snackbar.make(addImageButton, R.string.max_selections_error,
                            Snackbar.LENGTH_LONG).setAnchorView(addImageButton).show();
                }
            } else {
                // Request the necessary permissions if not granted
                requestPermissions();
            }
        });
    }

    /**
     * Initializes the date picker for the date input field.
     * <br>
     * The date picker includes a constraint to prevent future dates to be selected.
     * The default selection is the current date.
     * If a date is found in the input field, the picker is opened with the specified date selected.
     * */
    private void initializeDatePicker() {
        // Set up a constraint in the date picker to make only dates from today backward selectable
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now());

        String dateInField = dateEditText.getText().toString();
        long dateToSet;
        // If date field is empty, set date to current date
        if (dateInField.isEmpty()) {
            dateToSet = MaterialDatePicker.todayInUtcMilliseconds();
        }
        else { // If date field is not empty, get specified date
            try {
                // Parse date string to a Date object
                Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        .parse(dateInField);
                assert date != null;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                // Set mid-day to prevent time zone shifts
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                dateToSet = calendar.getTimeInMillis();
            } catch (ParseException e) {
                Log.e("NewEntryActivity", "Parsing error occurred", e);
                // If an error occurs in parsing the date string, set date to current date
                dateToSet = MaterialDatePicker.todayInUtcMilliseconds();
            }
        }

        // Build and show the date picker
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(R.string.date_picker_title)
                        .setSelection(dateToSet)
                        .setCalendarConstraints(constraintBuilder.build())
                        .build();

        // Convert selected date to a string and set in date field
        datePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(new Date(selection));
            dateEditText.setText(date);
        });
        datePicker.show(getSupportFragmentManager(), "MaterialDatePicker");
    }

    /**
     * Modifies and opens the activity for editing the entry.
     * <br>
     * If the activity is called to edit the entry, the UI is modified to reflect the edit action.
     * All data from the entry to be edited are loaded into the respective views and made editable.
     * */
    private void setUpActivityForEdit() {
        /* UI changes */
        // Change save button text
        saveButton.setText(R.string.update_button);
        // Change top app bar text
        topAppBar.setTitle(R.string.edit_entry_title);
        // Change top app bar navigation listener to return to View Entry screen instead of main screen
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(NewEntryActivity.this, ViewEntryActivity.class);
            intent.putExtra("entryId", entryId);
            startActivity(intent);
        });

        // Get intent data from View Entry activity
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        entryId = extras.getInt("entryId", -1);
        String dateText = extras.getString("date");
        String titleText = extras.getString("title");
        String contentText = extras.getString("content");
        List<String> imagePaths = extras.getStringArrayList("imagePaths");

        // Set data to text fields
        dateEditText.setText(dateText);
        titleEditText.setText(titleText);
        contentEditText.setText(contentText);

        // Copy any existing images to temporary storage to prevent direct modifications to original data
        if (imagePaths != null) {
            tempImagePaths.clear();
            imageHandler.copyExistingImagesToTemporaryStorage(imagePaths);
            // Load images in carousel
            carouselAdapter.notifyDataSetChanged();
        }
    }

    /* Event handling methods */

    /**
     * Requests permissions to read media images.
     */
    public void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{READ_MEDIA_IMAGES}, REQUEST_CODE_READ_MEDIA_IMAGES);
    }

    /**
     * Handles media selection from the photo picker.
     * <br>
     * The selected images are stored in temporary storage until the entry is saved.
     *
     * @param uris The list of selected media URIs.
     */
    private void handleMediaSelection(List<Uri> uris) {
        if (!uris.isEmpty()) {
            boolean result = imageHandler.copyImagesToTemporaryStorage(uris);
            if (result) {
                // If images are saved, update carousel
                carouselAdapter.notifyDataSetChanged();
            }
            else { // If an image was not saved, display error message
                // Get reference to the parent layout
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Failed to save image", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    }

    /**
     * Sets up save button functionality.
     * <br>
     * In both entry creation and edit modes, clicking the save button gets the user input values from the
     * respective fields and sets up a new journal entry object.
     * Depending on the specific action, the object is then either inserted or updated in the database.
     * */
    private void onSaveButtonClicked() {
        JournalEntryViewModel viewModel = new ViewModelProvider(this).get(JournalEntryViewModel.class);

        long date;
        String dateString = dateEditText.getText().toString();
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        // Get selected date from date input field
        try {
            // Parse date string to a Date object
            Date dateLong = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString);
            assert dateLong != null;
            // Get the time in milliseconds
            date = dateLong.getTime();
        } catch (ParseException e) {
            Log.e("NewEntryActivity", "Parsing error occurred", e);
            // If an error occurs in parsing the date string, set date to current date
            date = System.currentTimeMillis();
        }

        // If title or content fields are empty, show error in fields
        if (title.isEmpty()) {
            titleEditText.setError("This field cannot be empty");
        } else if (content.isEmpty()) {
            contentEditText.setError("This field cannot be empty");
        } else {
            // Create a new entry
            JournalEntryEntity entry = new JournalEntryEntity();
            entry.setDate(date);
            entry.setTitle(title);
            entry.setContent(content);

            List<String> savedImagePaths = new ArrayList<>();
            // Move selected images from temporary cache to internal storage
            imageHandler.moveImagesToInternalStorage(savedImagePaths);
            // Set the image paths for the entry
            entry.setImagePaths(savedImagePaths);

            // If entry is being edited, update entry in database
            if ("edit".equals(startingAction)) {
                entry.setId(entryId);

                // Delete previously saved images that were removed during editing
                imageHandler.deleteImagesRemovedFromOriginal(entry.getImagePaths(), savedImagePaths);
                // Set the updated image paths for the entry
                entry.setImagePaths(savedImagePaths);

                updateEntry(viewModel, entry);
            } else { // If entry is being created, save entry in database
                saveEntry(viewModel, entry);
            }
        }
    }

    /**
     * Opens the photo picker for selecting images.
     * <br>
     * The photo picker is set up to allow multiple image selections.
     * The allowed selections are updated accordingly each time the picker is opened based on the total
     * number of selections allowed for an entry.
     *
     * @param maxSelections The maximum number of image selections to enforce in the picker.
     */
    public void openPhotoPicker(int maxSelections) {
        // Set minimum value as 2 to comply with picker constraints
        int validSelections = Math.max(maxSelections, 2);
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .setMaxItems(validSelections)
                .build());
    }

    /**
     * Saves a journal entry to the database.
     *
     * @param viewModel The ViewModel instance for database operations.
     * @param entry The journal entry to be saved.
     */
    private void saveEntry(JournalEntryViewModel viewModel, JournalEntryEntity entry) {
        // Create a new thread to perform the database operation
        new Thread(() -> {
            // Save the entry to the database
            viewModel.insertEntry(entry);
            entrySaved = true;

            runOnUiThread(() -> {
                // Inform user that the entry has been saved
                Toast.makeText(NewEntryActivity.this, R.string.save_info_message,
                        Toast.LENGTH_SHORT).show();
                finish(); // Return to MainActivity
            });
        }).start();
    }

    /**
     * Updates a journal entry in the database.
     *
     * @param viewModel The ViewModel instance for database operations.
     * @param entry The journal entry to be updated.
     */
    private void updateEntry(JournalEntryViewModel viewModel, JournalEntryEntity entry) {
        // Create a new thread to perform the database operation
        new Thread(() -> {
            // Update the entry in the database
            viewModel.updateEntry(entry);
            entrySaved = true;

            runOnUiThread(() -> {
                // Inform user that the entry has been updated
                Toast.makeText(NewEntryActivity.this, R.string.update_info_message,
                        Toast.LENGTH_SHORT).show();
                finish(); // Return to ViewEntryActivity
            });
        }).start();
    }
}