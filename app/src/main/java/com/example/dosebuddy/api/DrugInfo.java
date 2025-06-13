package com.example.dosebuddy.api;

import java.util.List;

/**
 * Data model for drug information from OpenFDA API
 */
public class DrugInfo {
    
    private String brandName;
    private String genericName;
    private String activeIngredient;
    private String purpose;
    private String dosageAndAdministration;
    private List<String> warnings;
    private List<String> sideEffects;
    private String manufacturer;
    private String description;
    
    // Default constructor
    public DrugInfo() {}
    
    // Constructor with basic info
    public DrugInfo(String brandName, String genericName, String activeIngredient) {
        this.brandName = brandName;
        this.genericName = genericName;
        this.activeIngredient = activeIngredient;
    }
    
    // Getters and Setters
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    
    public String getGenericName() {
        return genericName;
    }
    
    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }
    
    public String getActiveIngredient() {
        return activeIngredient;
    }
    
    public void setActiveIngredient(String activeIngredient) {
        this.activeIngredient = activeIngredient;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public String getDosageAndAdministration() {
        return dosageAndAdministration;
    }
    
    public void setDosageAndAdministration(String dosageAndAdministration) {
        this.dosageAndAdministration = dosageAndAdministration;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    public List<String> getSideEffects() {
        return sideEffects;
    }
    
    public void setSideEffects(List<String> sideEffects) {
        this.sideEffects = sideEffects;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get display name (brand name if available, otherwise generic name)
     */
    public String getDisplayName() {
        if (brandName != null && !brandName.trim().isEmpty()) {
            return brandName;
        }
        return genericName != null ? genericName : "Unknown";
    }
    
    /**
     * Check if drug info has essential information
     */
    public boolean hasEssentialInfo() {
        return (brandName != null && !brandName.trim().isEmpty()) || 
               (genericName != null && !genericName.trim().isEmpty());
    }
    
    @Override
    public String toString() {
        return "DrugInfo{" +
                "brandName='" + brandName + '\'' +
                ", genericName='" + genericName + '\'' +
                ", activeIngredient='" + activeIngredient + '\'' +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}
