package com.fishingadvisor.app.dto;

import jakarta.validation.constraints.NotBlank;

public class GearAdviceRequest {

    @NotBlank(message = "Target species is required")
    private String targetSpecies;

    @NotBlank(message = "Technique is required")
    private String technique;

    @NotBlank(message = "Water type is required")
    private String waterType;

    @NotBlank(message = "Experience level is required")
    private String experienceLevel;

    @NotBlank(message = "Budget tier is required")
    private String budgetTier;

    public GearAdviceRequest() {
    }

    public String getTargetSpecies() {
        return targetSpecies;
    }

    public void setTargetSpecies(String targetSpecies) {
        this.targetSpecies = targetSpecies;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public String getWaterType() {
        return waterType;
    }

    public void setWaterType(String waterType) {
        this.waterType = waterType;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getBudgetTier() {
        return budgetTier;
    }

    public void setBudgetTier(String budgetTier) {
        this.budgetTier = budgetTier;
    }
}
