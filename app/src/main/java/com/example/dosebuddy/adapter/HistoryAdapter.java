package com.example.dosebuddy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dosebuddy.R;
import com.example.dosebuddy.database.MedicationHistory;
import com.example.dosebuddy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying medication history
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    
    private List<MedicationHistory> historyList;
    
    public HistoryAdapter() {
        this.historyList = new ArrayList<>();
    }
    
    /**
     * Set history data
     */
    public void setHistory(List<MedicationHistory> history) {
        this.historyList = history != null ? history : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        MedicationHistory history = historyList.get(position);
        holder.bind(history);
    }
    
    @Override
    public int getItemCount() {
        return historyList.size();
    }
    
    /**
     * ViewHolder for history items
     */
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView tvMedicationName;
        private final TextView tvDosage;
        private final TextView tvTakenTime;
        private final TextView tvScheduledTime;
        private final TextView tvTimingBadge;
        private final TextView tvTakenMethod;
        private final TextView tvNotes;
        
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvTakenTime = itemView.findViewById(R.id.tv_taken_time);
            tvScheduledTime = itemView.findViewById(R.id.tv_scheduled_time);
            tvTimingBadge = itemView.findViewById(R.id.tv_timing_badge);
            tvTakenMethod = itemView.findViewById(R.id.tv_taken_method);
            tvNotes = itemView.findViewById(R.id.tv_notes);
        }
        
        /**
         * Bind history data to views
         */
        public void bind(MedicationHistory history) {
            // Set medication name and dosage
            tvMedicationName.setText(history.getMedicationName());
            tvDosage.setText(history.getMedicationDosage());
            
            // Set taken time
            String takenTimeText = itemView.getContext().getString(R.string.taken_on,
                    DateTimeUtils.formatDateTime(history.getTakenAt()));
            tvTakenTime.setText(takenTimeText);
            
            // Set scheduled time if available
            if (history.getScheduledTime() != null) {
                String scheduledTimeText = itemView.getContext().getString(R.string.scheduled_for,
                        DateTimeUtils.formatDateTime(history.getScheduledTime()));
                tvScheduledTime.setText(scheduledTimeText);
                tvScheduledTime.setVisibility(View.VISIBLE);
                
                // Show timing badge
                setupTimingBadge(history);
                tvTimingBadge.setVisibility(View.VISIBLE);
            } else {
                tvScheduledTime.setVisibility(View.GONE);
                tvTimingBadge.setVisibility(View.GONE);
            }
            
            // Set taken method
            String methodText = history.getTakenMethodEnum().getDisplayName();
            tvTakenMethod.setText(methodText);
            
            // Set notes if available
            if (history.getNotes() != null && !history.getNotes().trim().isEmpty()) {
                tvNotes.setText(history.getNotes());
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }
        }
        
        /**
         * Setup timing badge (on time, late, early)
         */
        private void setupTimingBadge(MedicationHistory history) {
            if (history.getScheduledTime() == null) {
                tvTimingBadge.setVisibility(View.GONE);
                return;
            }
            
            long timeDifferenceMinutes = history.getTimeDifferenceMinutes();
            
            if (history.isOnTime()) {
                // On time (within 30 minutes)
                tvTimingBadge.setText(itemView.getContext().getString(R.string.on_time_badge));
                tvTimingBadge.setBackgroundResource(R.drawable.badge_on_time);
            } else if (timeDifferenceMinutes > 0) {
                // Late
                String lateText = itemView.getContext().getString(R.string.minutes_late, 
                        Math.abs(timeDifferenceMinutes));
                tvTimingBadge.setText(lateText);
                tvTimingBadge.setBackgroundResource(R.drawable.badge_late);
            } else {
                // Early
                String earlyText = itemView.getContext().getString(R.string.minutes_early, 
                        Math.abs(timeDifferenceMinutes));
                tvTimingBadge.setText(earlyText);
                tvTimingBadge.setBackgroundResource(R.drawable.badge_early);
            }
        }
    }
}
