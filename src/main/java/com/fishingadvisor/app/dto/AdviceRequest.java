package com.fishingadvisor.app.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class AdviceRequest {

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Target species is required")
    private String targetSpecies;

    @NotBlank(message = "Season is required")
    private String season;

    @NotBlank(message = "Water clarity is required")
    private String waterClarity;

    @NotBlank(message = "Time of day is required")
    private String timeOfDay;

    // Optional - if you wire up a weather API later you can populate this automatically
    private String waterTemp;

    private String weatherNotes;

    // Multi-select: e.g. ["Windy", "Gusty"] - can co-occur with sky/weather conditions
    private List<String> windCondition;

    // Single-select: Sunny, Partly Cloudy, Overcast, Rain, Snow
    private String skyCondition;

    public AdviceRequest() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTargetSpecies() {
        return targetSpecies;
    }

    public void setTargetSpecies(String targetSpecies) {
        this.targetSpecies = targetSpecies;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getWaterClarity() {
        return waterClarity;
    }

    public void setWaterClarity(String waterClarity) {
        this.waterClarity = waterClarity;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public String getWaterTemp() {
        return waterTemp;
    }

    public void setWaterTemp(String waterTemp) {
        this.waterTemp = waterTemp;
    }

    public String getWeatherNotes() {
        return weatherNotes;
    }

    public void setWeatherNotes(String weatherNotes) {
        this.weatherNotes = weatherNotes;
    }

    public List<String> getWindCondition() {
        return windCondition;
    }

    public void setWindCondition(List<String> windCondition) {
        this.windCondition = windCondition;
    }

    public String getSkyCondition() {
        return skyCondition;
    }

    public void setSkyCondition(String skyCondition) {
        this.skyCondition = skyCondition;
    }
}
