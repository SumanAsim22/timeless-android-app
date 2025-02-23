package com.example.myapplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * The ImageHandlerTest class provides unit tests for the {@link ImageHandler} class.
 * <br>
 * These tests ensure that the ImageHandler methods function correctly and handle various
 * scenarios related to image file operations, including copying, moving, and deleting images.
 * */
public class ImageHandlerTest {

    private ContentResolver contentResolver;
    private File filesDir;
    private List<String> tempImagePaths;
    private ImageHandler imageHandler;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        contentResolver = mock(ContentResolver.class);
        File cacheDir = tempFolder.newFolder("cache");
        filesDir = tempFolder.newFolder("files");
        tempImagePaths = new ArrayList<>();
        imageHandler = new ImageHandler(contentResolver, cacheDir, filesDir, tempImagePaths);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up temporary files and directories
        tempFolder.delete();
    }

    /**
     * Creates a temporary image file with dummy content for testing purposes.
     * <br>
     * A temporary image file is generated in the temporary folder with a name based
     * on the provided file number. The file will contain dummy content that simulates image data.
     *
     * @param fileNumber The number used to create the file name. For example, if fileNumber is 2,
     *                   the file name will be "image2.jpg".
     * @return A File object representing the created temporary image file.
     * @throws IOException If an I/O error occurs during the creation of the file or while writing dummy content to it.
     * */
    public File imageSetUp(int fileNumber) throws IOException {
        // Set up file name based on passed number
        String fileName = "image" + fileNumber + ".jpg";

        // Create temporary image file
        File tempFile = tempFolder.newFile(fileName);

        // Write dummy content to simulate image data
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(("Test Image " + fileNumber).getBytes());
        }
        return tempFile;
    }

    @Test
    public void testCopyImagesToTemporaryStorage() throws Exception {
        // Set up image files
        File tempImageFile1 = imageSetUp(1);
        File tempImageFile2 = imageSetUp(2);

        // Create mock Uris
        Uri uri1 = mock(Uri.class);
        Uri uri2 = mock(Uri.class);
        List<Uri> uris = Arrays.asList(uri1, uri2);

        // Mock ContentResolver to return InputStreams for the Uris
        when(contentResolver.openInputStream(uri1)).thenReturn(new FileInputStream(tempImageFile1));
        when(contentResolver.openInputStream(uri2)).thenReturn(new FileInputStream(tempImageFile2));

        // Get result of method call
        boolean result = imageHandler.copyImagesToTemporaryStorage(uris);

        // Assert that the method returned true
        assertTrue("copyImagesToTemporaryStorage should return true", result);

        // Assert that images have been added to the temporary list
        assertEquals("Two images should be added to tempImagePaths", 2, tempImagePaths.size());

        // Verify that the content in the temp files matches the original files
        for (String tempImagePath : tempImagePaths) {
            File copiedImageFile = new File(tempImagePath);
            String content = new String(Files.readAllBytes(copiedImageFile.toPath()));
            assertTrue("Copied content should match original content",
                    content.equals("Test Image 1") || content.equals("Test Image 2"));
        }
    }

    @Test
    public void testCopyExistingImagesToTemporaryStorage() throws Exception {
        // Set up image files
        File originalImage1 = imageSetUp(1);
        File originalImage2 = imageSetUp(2);

        // Create a list of image paths
        List<String> existingImagePaths = new ArrayList<>();
        existingImagePaths.add(originalImage1.getAbsolutePath());
        existingImagePaths.add(originalImage2.getAbsolutePath());

        imageHandler.copyExistingImagesToTemporaryStorage(existingImagePaths);

        // Verify that the temp files exist
        for (String tempImagePath : tempImagePaths) {
            File tempImageFile = new File(tempImagePath);
            assertTrue(tempImageFile.exists());

            // Verify the content matches the original files
            String content = new String(Files.readAllBytes(tempImageFile.toPath()));
            assertTrue("Copied content should match original content",
                    content.equals("Test Image 1") || content.equals("Test Image 2"));
        }

    }

    @Test
    public void testMoveImagesToInternalStorage() throws Exception {
        File tempImageFile1 = imageSetUp(1);

        // Add the path of the temp image to tempImagePaths
        tempImagePaths.add(tempImageFile1.getAbsolutePath());

        // Create an empty list to hold the paths of moved images
        List<String> imagePaths = new ArrayList<>();

        imageHandler.moveImagesToInternalStorage(imagePaths);

        // Verify that the temp image file has been deleted
        assertFalse("Temp image file should be deleted", tempImageFile1.exists());

        // Verify that the image has been moved to filesDir
        assertEquals("One image should be moved", 1, imagePaths.size());
        String movedImagePath = imagePaths.get(0);
        File movedImageFile = new File(movedImagePath);
        assertTrue("Moved image file should exist", movedImageFile.exists());
    }

    @Test
    public void testDeleteTemporaryImages() throws Exception {
        // Set up image files
        File tempImageFile1 = imageSetUp(1);
        File tempImageFile2 = imageSetUp(2);

        // Add the paths of the temp images to tempImagePaths
        tempImagePaths.add(tempImageFile1.getAbsolutePath());
        tempImagePaths.add(tempImageFile2.getAbsolutePath());

        imageHandler.deleteTemporaryImages();

        // Verify that the temp image files have been deleted
        assertFalse("Temp image file 1 should be deleted", tempImageFile1.exists());
        assertFalse("Temp image file 2 should be deleted", tempImageFile2.exists());

        // Verify that tempImagePaths is now empty
        assertEquals("tempImagePaths should be empty after deletion", 0, tempImagePaths.size());
    }

    @Test
    public void testDeleteImagesRemovedFromOriginal() throws Exception {
        // Set up original image files
        File originalFile1 = imageSetUp(1);
        File originalFile2 = imageSetUp(2);
        File originalFile3 = imageSetUp(3);

        // Create list for originalImagePaths
        List<String> originalImagePaths = new ArrayList<>();
        originalImagePaths.add(originalFile1.getAbsolutePath());
        originalImagePaths.add(originalFile2.getAbsolutePath());
        originalImagePaths.add(originalFile3.getAbsolutePath());

        // Create list for savedImagePaths containing only two of the original files
        List<String> savedImagePaths = new ArrayList<>();
        savedImagePaths.add(originalFile1.getAbsolutePath());
        savedImagePaths.add(originalFile3.getAbsolutePath());

        imageHandler.deleteImagesRemovedFromOriginal(originalImagePaths, savedImagePaths);

        // Verify that originalFile2 has been deleted
        assertFalse("originalFile2 should be deleted", originalFile2.exists());

        // Verify that originalFile1 and originalFile3 still exist
        assertTrue("originalFile1 should still exist", originalFile1.exists());
        assertTrue("originalFile3 should still exist", originalFile3.exists());
    }

    /*
    * Edge test case for moving images to internal storage when tempImagePaths is empty
    * */
    @Test
    public void testMoveImagesToInternalStorageWithEmptyTempPaths() throws Exception {
        // Ensure tempImagePaths is empty
        tempImagePaths.clear();

        // Create an empty list for moved image paths
        List<String> imagePaths = new ArrayList<>();

        imageHandler.moveImagesToInternalStorage(imagePaths);

        // Verify that imagePaths remains empty
        assertTrue("imagePaths should remain empty when tempImagePaths is empty", imagePaths.isEmpty());
    }

    /*
    * Edge test case for deleting when all images have been removed
    * */
    @Test
    public void testDeleteImagesRemovedFromOriginalWithAllImagesToDelete() throws Exception {
        // Set up original image files
        File originalFile1 = imageSetUp(1);
        File originalFile2 = imageSetUp(2);

        // Create list for originalImagePaths
        List<String> originalImagePaths = new ArrayList<>();
        originalImagePaths.add(originalFile1.getAbsolutePath());
        originalImagePaths.add(originalFile2.getAbsolutePath());

        // Set savedImagePaths list as empty, indicating all images have been removed
        List<String> savedImagePaths = new ArrayList<>();

        // Verify that original files exist before deletion
        assertTrue("originalFile1 should exist", originalFile1.exists());
        assertTrue("originalFile2 should exist", originalFile2.exists());

        imageHandler.deleteImagesRemovedFromOriginal(originalImagePaths, savedImagePaths);

        // Verify that both original files have been deleted
        assertFalse("originalFile1 should be deleted", originalFile1.exists());
        assertFalse("originalFile2 should be deleted", originalFile2.exists());
    }
}
