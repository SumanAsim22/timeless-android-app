package com.example.myapplication;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

/**
 * Adapter class for displaying images in a Material 3 Carousel within a RecyclerView.
 * */
public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {

    // List of image paths to be loaded in the carousel
    private final List<String> imagePaths;

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_carousel_item, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {

        String imagePath = imagePaths.get(position);
        File imgFile = new File(imagePath);

        // Load each image with the corresponding URI
        if (imgFile.exists()) {
            holder.carouselImageView.setImageURI(Uri.fromFile(imgFile));
        } else {
            // If image file is not found, replace with placeholder image
            holder.carouselImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener to allow image removal from carousel
        holder.itemView.setOnClickListener(v -> {
            // Do not enable click listener for carousel images when viewing entry
            if (v.getContext().getClass() == ViewEntryActivity.class) {
                return;
            }
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle(R.string.remove_image_question)
                    .setMessage(R.string.remove_image_message)
                    .setNegativeButton(R.string.cancel_button, null)
                    .setPositiveButton(R.string.remove_button, (dialog, which) -> {
                        // Remove image and update carousel
                        String imageRemoved = imagePaths.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imagePaths.size());
                        // Remove image file from temporary storage
                        deleteImageFile(imageRemoved);
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() { return imagePaths.size(); }

    /**
     * Initializes the adapter with a list of image paths to load.
     *
     * @param imagePaths List of image paths.
     */
    public CarouselAdapter(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    /**
     * Deletes the temporary image file when it is removed from the carousel.
     *
     * @param imagePath Path of the image file to be deleted.
     */
    private void deleteImageFile(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d("ImageRemoved", imagePath);
            if (!deleted) {
                Log.e("ImageDeletion", "Failed to delete image: " + imagePath);
            }
        }
    }

    /**
     * ViewHolder class for the carousel images.
     */
    public static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView carouselImageView;

        public CarouselViewHolder(@NonNull View view) {
            super(view);
            carouselImageView = itemView.findViewById(R.id.carouselImageView);
        }
    }
}

