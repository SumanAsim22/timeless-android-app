package com.example.myapplication;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * The ImageHandler class provides utility methods for managing image files within the application.
 * <br>
 * It handles copying images from external sources to temporary storage, moving images to internal storage,
 * and cleaning up temporary images to maintain storage efficiency.
 * */
public class ImageHandler {
    private final ContentResolver contentResolver;
    private final File cacheDir;
    private final File filesDir;
    private final List<String> tempImagePaths;

    public ImageHandler(ContentResolver contentResolver, File cacheDir, File filesDir, List<String> tempImagePaths) {
        this.contentResolver = contentResolver;
        this.cacheDir = cacheDir;
        this.filesDir = filesDir;
        this.tempImagePaths = tempImagePaths;
    }

    /**
     * Copies selected images to temporary cache storage.
     * <br>
     * The images are stored temporarily until the entry is saved.
     *
     * @param uris The list of image URIs to be copied to cache storage.
     * @return Boolean result indicating operation success
     * */
    public boolean copyImagesToTemporaryStorage(List<Uri> uris) {
        boolean result = false;

        for (Uri uri : uris) {
            try {
                InputStream inputStream = contentResolver.openInputStream(uri);
                String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(cacheDir, fileName);

                OutputStream outputStream = new FileOutputStream(imageFile);

                byte[] buffer = new byte[1024];
                assert inputStream != null;
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();

                // Save the file path to a temporary list to track images during the session
                tempImagePaths.add(imageFile.getAbsolutePath());

                result = true;

            } catch (IOException e) {
                Log.e("ImageHandler", "File operation error occurred", e);
                result = false;
            }
        }
        return result;
    }

    /**
     * Copies image paths retrieved from a saved entry to temporary cache storage.
     * <br>
     * The images are stored and used in a temporary list to prevent permanent modifications
     * to existing data before the entry is updated.
     *
     * @param existingImagePaths The list of existing image paths to copy.
     * */
    public void copyExistingImagesToTemporaryStorage(List<String> existingImagePaths) {
        try {
            for (String originalPath : existingImagePaths) {
                File originalFile = new File(originalPath);
                if (originalFile.exists()) {
                    String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
                    File tempFile = new File(cacheDir, fileName);

                    try (InputStream in = new FileInputStream(originalFile);
                         OutputStream out = new FileOutputStream(tempFile)) {

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }

                        tempImagePaths.add(tempFile.getAbsolutePath());
                    }
                }
            }
        } catch (IOException e) {
            Log.e("ImageHandler", "File operation error occurred", e);
        }
    }

    /**
     * Moves images from temporary storage to internal storage.
     * <br>
     * This ensures persistent storage of selected images for the entry.
     *
     * @param imagePaths The list of image paths to store for the entry.
     * */
    public void moveImagesToInternalStorage(List<String> imagePaths) {
        Iterator<String> iterator = tempImagePaths.iterator();
        while (iterator.hasNext()) {
            String tempImagePath = iterator.next();
            File tempFile = new File(tempImagePath);
            if (tempFile.exists()) {
                String fileName = "image_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(filesDir, fileName);

                boolean success = false;

                try (FileInputStream in = new FileInputStream(tempFile);
                     FileOutputStream out = new FileOutputStream(imageFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }

                    // Indicate that copying was successful
                    success = true;

                } catch (IOException e) {
                    Log.e("ImageHandler", "Error moving file: " + tempImagePath, e);
                }

                // After successfully copying, delete the original file
                if (success) {
                    // After streams are closed, attempt to delete the original file
                    if (tempFile.delete()) {
                        imagePaths.add(imageFile.getAbsolutePath());
                        iterator.remove(); // Remove the path from tempImagePaths to keep it updated
                    } else {
                        Log.e("ImageHandler", "Failed to delete temp file: " + tempImagePath);
                    }
                }
            }
        }
    }

    /**
     * Deletes temporary images from storage.
     * <br>
     * If changes are not saved when creating or editing an entry, temporarily saved images are deleted
     * to clear cache storage.
     * */
    public void deleteTemporaryImages() {
        for (String tempImagePath : tempImagePaths) {
            File tempFile = new File(tempImagePath);
            if (tempFile.exists() && !tempFile.delete()) {
                Log.e("ImageHandler", "Failed to delete temp image: " + tempImagePath);
            }
        }
        tempImagePaths.clear();
    }

    /**
     * Deletes previously saved images that were removed in an update.
     * <br>
     * The method compares the original image paths with the image paths set after an update.
     * Image files that are not found in the new path list are removed from storage.
     *
     * @param originalImagePaths The list of original image paths of an entry prior to editing.
     * @param savedImagePaths The list of image paths saved temporarily after editing.
     * */
    public void deleteImagesRemovedFromOriginal(List<String> originalImagePaths, List<String> savedImagePaths) {
        for (String originalPath : originalImagePaths) {
            if (!savedImagePaths.contains(originalPath)) {
                File originalFile = new File(originalPath);
                if (originalFile.exists() && !originalFile.delete()) {
                    Log.e("ImageHandler", "Failed to delete image: " + originalPath);
                }
            }
        }
    }
}

