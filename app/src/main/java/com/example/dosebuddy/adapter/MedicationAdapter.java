package com.example.dosebuddy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dosebuddy.R;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationFrequency;
import com.example.dosebuddy.utils.DateTimeUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying medications in a list
 */
public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    
    private List<Medication> medications;
    private List<Medication> filteredMedications;
    private Context context;
    private OnMedicationClickListener listener;
    
    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
        void onMedicationLongClick(Medication medication);
        void onMoreOptionsClick(Medication medication, View view);
        void onMarkTakenClick(Medication medication);
        void onDrugInfoClick(Medication medication);
    }
    
    public MedicationAdapter(Context context) {
        this.context = context;
        this.medications = new ArrayList<>();
        this.filteredMedications = new ArrayList<>();
    }
    
    public void setOnMedicationClickListener(OnMedicationClickListener listener) {
        this.listener = listener;
    }
    
    public void setMedications(List<Medication> medications) {
        this.medications = medications;
        this.filteredMedications = new ArrayList<>(medications);
        notifyDataSetChanged();
    }
    
    public void filterMedications(String query) {
        filteredMedications.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredMedications.addAll(medications);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Medication medication : medications) {
                if (medication.getName().toLowerCase().contains(lowerCaseQuery) ||
                    medication.getDosage().toLowerCase().contains(lowerCaseQuery)) {
                    filteredMedications.add(medication);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = filteredMedications.get(position);
        holder.bind(medication);
    }
    
    @Override
    public int getItemCount() {
        return filteredMedications.size();
    }
    
    public class MedicationViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivMedicationIcon;
        private TextView tvMedicationName;
        private TextView tvDosage;
        private TextView tvFrequency;
        private TextView tvNextDoseTime;
        private MaterialButton btnMarkTaken;
        private MaterialButton btnDrugInfo;
        private ImageButton btnMoreOptions;
        private View statusIndicator;
        
        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivMedicationIcon = itemView.findViewById(R.id.iv_medication_icon);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvNextDoseTime = itemView.findViewById(R.id.tv_next_dose_time);
            btnMarkTaken = itemView.findViewById(R.id.btn_mark_taken);
            btnDrugInfo = itemView.findViewById(R.id.btn_drug_info);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMedicationClick(filteredMedications.get(position));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMedicationLongClick(filteredMedications.get(position));
                        return true;
                    }
                }
                return false;
            });
            
            btnMoreOptions.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMoreOptionsClick(filteredMedications.get(position), v);
                    }
                }
            });
            
            btnMarkTaken.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMarkTakenClick(filteredMedications.get(position));
                    }
                }
            });

            btnDrugInfo.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDrugInfoClick(filteredMedications.get(position));
                    }
                }
            });
        }
        
        public void bind(Medication medication) {
            // Set medication name
            tvMedicationName.setText(medication.getName());
            
            // Set dosage
            tvDosage.setText(medication.getDosage());
            
            // Set frequency information
            String frequencyText = getFrequencyDisplayText(medication);
            tvFrequency.setText(frequencyText);
            
            // Set next dose time
            String nextDoseText = getNextDoseText(medication);
            tvNextDoseTime.setText(nextDoseText);
            
            // Set status indicator color based on medication status
            updateStatusIndicator(medication);
            
            // Update mark taken button visibility
            updateMarkTakenButton(medication);
        }
        
        private String getFrequencyDisplayText(Medication medication) {
            MedicationFrequency frequency = medication.getFrequencyEnum();
            
            switch (frequency) {
                case ONCE_DAILY:
                    return "Once daily";
                case TWICE_DAILY:
                    return "Twice daily";
                case THREE_TIMES_DAILY:
                    return "3 times daily";
                case FOUR_TIMES_DAILY:
                    return "4 times daily";
                case EVERY_OTHER_DAY:
                    return "Every other day";
                case WEEKLY:
                    return "Weekly";
                case AS_NEEDED:
                    return "As needed";
                case CUSTOM:
                    return medication.getTimesPerDay() + " times daily";
                default:
                    return frequency.getDisplayName();
            }
        }
        
        private String getNextDoseText(Medication medication) {
            // For now, show a simple next dose time
            // In a real app, this would calculate based on specific times and last taken
            MedicationFrequency frequency = medication.getFrequencyEnum();
            
            if (frequency == MedicationFrequency.AS_NEEDED) {
                return "Take as needed";
            }
            
            // Simple logic: show next dose as 8:00 AM today
            // TODO: Implement proper next dose calculation based on specific times
            long nextDoseTime = DateTimeUtils.createTimeTimestamp(8, 0);
            
            if (DateTimeUtils.isToday(nextDoseTime)) {
                return "Today at " + DateTimeUtils.formatTime(nextDoseTime);
            } else {
                return "Tomorrow at " + DateTimeUtils.formatTime(nextDoseTime);
            }
        }
        
        private void updateStatusIndicator(Medication medication) {
            // Set status indicator color based on medication status
            // Green: On time, Yellow: Due soon, Red: Overdue
            // For now, default to primary color
            statusIndicator.setBackgroundColor(0xFF2196F3); // Blue color
        }
        
        private void updateMarkTakenButton(Medication medication) {
            // Show/hide mark taken button based on frequency
            if (medication.getFrequencyEnum() == MedicationFrequency.AS_NEEDED) {
                btnMarkTaken.setText("Take");
            } else {
                btnMarkTaken.setText("Take");
            }
            btnMarkTaken.setVisibility(View.VISIBLE);
        }
    }
}
