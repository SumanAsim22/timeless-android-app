package com.example.myapplication;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

/**
 * Adapter class for displaying journal entries in a summarized form within a RecyclerView.
 * */
public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    // List of journal entries to be loaded in the RecyclerView
    private final List<JournalEntryEntity> journalEntries;

    @NonNull
    @Override
    public JournalEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_journal_entry_item, parent, false);
        return new JournalEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryViewHolder holder, int position) {

        JournalEntryEntity journalEntry = journalEntries.get(position);

        // For each entry in the list, set the corresponding title and date in the RecyclerView item
        holder.titleView.setText(journalEntry.getTitle());
        holder.dateView.setText(DateFormat.format("dd-MM-yyyy", new Date(journalEntry.getDate())));

        // Set click listener to display the full entry with all its details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ViewEntryActivity.class);
            intent.putExtra("entryId", journalEntry.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return journalEntries.size();
    }

    /**
     * Initializes the adapter with a list of journal entries to load.
     *
     * @param journalEntries List of journal entries.
     */
    public JournalEntryAdapter(List<JournalEntryEntity> journalEntries) {
        this.journalEntries = journalEntries;
    }

    /**
     * ViewHolder class for the journal entries.
     */
    public static class JournalEntryViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView dateView;

        public JournalEntryViewHolder(@NonNull View view){
            super(view);
            titleView = view.findViewById(R.id.titleView);
            dateView = view.findViewById(R.id.dateView);
        }
    }
}
