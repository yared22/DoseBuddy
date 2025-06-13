package com.example.dosebuddy.api;

import java.util.List;

/**
 * Response model for OpenFDA drug search API
 */
public class DrugSearchResponse {
    
    private Meta meta;
    private List<DrugResult> results;
    
    public Meta getMeta() {
        return meta;
    }
    
    public void setMeta(Meta meta) {
        this.meta = meta;
    }
    
    public List<DrugResult> getResults() {
        return results;
    }
    
    public void setResults(List<DrugResult> results) {
        this.results = results;
    }
    
    /**
     * Meta information about the API response
     */
    public static class Meta {
        private String disclaimer;
        private String terms;
        private String license;
        private Results results;
        
        public String getDisclaimer() {
            return disclaimer;
        }
        
        public void setDisclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
        }
        
        public String getTerms() {
            return terms;
        }
        
        public void setTerms(String terms) {
            this.terms = terms;
        }
        
        public String getLicense() {
            return license;
        }
        
        public void setLicense(String license) {
            this.license = license;
        }
        
        public Results getResults() {
            return results;
        }
        
        public void setResults(Results results) {
            this.results = results;
        }
        
        public static class Results {
            private int skip;
            private int limit;
            private int total;
            
            public int getSkip() {
                return skip;
            }
            
            public void setSkip(int skip) {
                this.skip = skip;
            }
            
            public int getLimit() {
                return limit;
            }
            
            public void setLimit(int limit) {
                this.limit = limit;
            }
            
            public int getTotal() {
                return total;
            }
            
            public void setTotal(int total) {
                this.total = total;
            }
        }
    }
    
    /**
     * Individual drug result from the API
     */
    public static class DrugResult {
        private List<String> brand_name;
        private List<String> generic_name;
        private List<String> active_ingredient;
        private List<String> purpose;
        private List<String> dosage_and_administration;
        private List<String> warnings;
        private List<String> adverse_reactions;
        private List<String> openfda_manufacturer_name;
        private List<String> description;
        
        public List<String> getBrand_name() {
            return brand_name;
        }
        
        public void setBrand_name(List<String> brand_name) {
            this.brand_name = brand_name;
        }
        
        public List<String> getGeneric_name() {
            return generic_name;
        }
        
        public void setGeneric_name(List<String> generic_name) {
            this.generic_name = generic_name;
        }
        
        public List<String> getActive_ingredient() {
            return active_ingredient;
        }
        
        public void setActive_ingredient(List<String> active_ingredient) {
            this.active_ingredient = active_ingredient;
        }
        
        public List<String> getPurpose() {
            return purpose;
        }
        
        public void setPurpose(List<String> purpose) {
            this.purpose = purpose;
        }
        
        public List<String> getDosage_and_administration() {
            return dosage_and_administration;
        }
        
        public void setDosage_and_administration(List<String> dosage_and_administration) {
            this.dosage_and_administration = dosage_and_administration;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
        
        public List<String> getAdverse_reactions() {
            return adverse_reactions;
        }
        
        public void setAdverse_reactions(List<String> adverse_reactions) {
            this.adverse_reactions = adverse_reactions;
        }
        
        public List<String> getOpenfda_manufacturer_name() {
            return openfda_manufacturer_name;
        }
        
        public void setOpenfda_manufacturer_name(List<String> openfda_manufacturer_name) {
            this.openfda_manufacturer_name = openfda_manufacturer_name;
        }
        
        public List<String> getDescription() {
            return description;
        }
        
        public void setDescription(List<String> description) {
            this.description = description;
        }
        
        /**
         * Convert API result to DrugInfo object
         */
        public DrugInfo toDrugInfo() {
            DrugInfo drugInfo = new DrugInfo();
            
            // Set brand name (first one if multiple)
            if (brand_name != null && !brand_name.isEmpty()) {
                drugInfo.setBrandName(brand_name.get(0));
            }
            
            // Set generic name (first one if multiple)
            if (generic_name != null && !generic_name.isEmpty()) {
                drugInfo.setGenericName(generic_name.get(0));
            }
            
            // Set active ingredient (first one if multiple)
            if (active_ingredient != null && !active_ingredient.isEmpty()) {
                drugInfo.setActiveIngredient(active_ingredient.get(0));
            }
            
            // Set purpose (first one if multiple)
            if (purpose != null && !purpose.isEmpty()) {
                drugInfo.setPurpose(purpose.get(0));
            }
            
            // Set dosage (first one if multiple)
            if (dosage_and_administration != null && !dosage_and_administration.isEmpty()) {
                drugInfo.setDosageAndAdministration(dosage_and_administration.get(0));
            }
            
            // Set warnings (all of them)
            drugInfo.setWarnings(warnings);
            
            // Set side effects (adverse reactions)
            drugInfo.setSideEffects(adverse_reactions);
            
            // Set manufacturer (first one if multiple)
            if (openfda_manufacturer_name != null && !openfda_manufacturer_name.isEmpty()) {
                drugInfo.setManufacturer(openfda_manufacturer_name.get(0));
            }
            
            // Set description (first one if multiple)
            if (description != null && !description.isEmpty()) {
                drugInfo.setDescription(description.get(0));
            }
            
            return drugInfo;
        }
    }
}
