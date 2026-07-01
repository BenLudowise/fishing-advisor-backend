package com.fishingadvisor.app.dto;

public class AdviceResponse {

    private String advice;
    private int remainingFreeQueries;
    private boolean paywalled;

    public AdviceResponse() {
    }

    public AdviceResponse(String advice, int remainingFreeQueries, boolean paywalled) {
        this.advice = advice;
        this.remainingFreeQueries = remainingFreeQueries;
        this.paywalled = paywalled;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public int getRemainingFreeQueries() {
        return remainingFreeQueries;
    }

    public void setRemainingFreeQueries(int remainingFreeQueries) {
        this.remainingFreeQueries = remainingFreeQueries;
    }

    public boolean isPaywalled() {
        return paywalled;
    }

    public void setPaywalled(boolean paywalled) {
        this.paywalled = paywalled;
    }
}
