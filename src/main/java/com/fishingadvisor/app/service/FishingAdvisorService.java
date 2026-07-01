package com.fishingadvisor.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishingadvisor.app.dto.AdviceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class FishingAdvisorService {

    private static final String SYSTEM_PROMPT = """
            You are an expert fishing advisor with deep knowledge of freshwater and saltwater \
            fishing across North America. Your job is to give anglers specific, \
            confidence-inspiring advice based on their exact conditions.

            When a user provides their location, target species, season, water temp, water \
            clarity, time of day, sky conditions, wind conditions, and weather - respond with:

            Top Techniques (2-3 max):
            For each technique, include: technique name, specific lure type and color, \
            retrieve style, and depth/presentation. Be concrete - say "chartreuse 3/8oz \
            spinnerbait, slow roll along the bottom" not "try a spinnerbait."

            Why This Works Right Now:
            2-3 sentences explaining the biology or behavior behind the recommendation. \
            Make the angler feel like they understand the fish, not just following orders.

            One Thing to Avoid:
            One common mistake anglers make in these exact conditions.

            Rules:
            - Never give vague advice. If you don't have enough info, ask one clarifying question.
            - Always tailor to the species. Bass advice is not walleye advice.
            - Saltwater and freshwater are different worlds - treat them that way.
            - Keep the total response under 250 words. Anglers are on the water, not reading essays.
            """;

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public FishingAdvisorService(WebClient anthropicWebClient,
                                  ObjectMapper objectMapper,
                                  @Value("${anthropic.api.model}") String model) {
        this.anthropicWebClient = anthropicWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public String getAdvice(AdviceRequest request) {
        String userPrompt = buildUserPrompt(request);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 500,
                "system", SYSTEM_PROMPT,
                "messages", List.of(
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        JsonNode response = anthropicWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return extractText(response);
    }

    private String buildUserPrompt(AdviceRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Location: ").append(request.getLocation()).append("\n");
        sb.append("Target species: ").append(request.getTargetSpecies()).append("\n");
        sb.append("Season: ").append(request.getSeason()).append("\n");
        sb.append("Water clarity: ").append(request.getWaterClarity()).append("\n");
        sb.append("Time of day: ").append(request.getTimeOfDay()).append("\n");

        if (request.getWaterTemp() != null && !request.getWaterTemp().isBlank()) {
            sb.append("Water temp: ").append(request.getWaterTemp()).append("\n");
        }
        if (request.getSkyCondition() != null && !request.getSkyCondition().isBlank()) {
            sb.append("Sky conditions: ").append(request.getSkyCondition()).append("\n");
        }
        if (request.getWindCondition() != null && !request.getWindCondition().isEmpty()) {
            sb.append("Wind conditions: ")
                    .append(String.join(", ", request.getWindCondition()))
                    .append("\n");
        }
        if (request.getWeatherNotes() != null && !request.getWeatherNotes().isBlank()) {
            sb.append("Weather notes: ").append(request.getWeatherNotes()).append("\n");
        }

        sb.append("\nWhat should I throw right now?");
        return sb.toString();
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException("Empty response from Anthropic API");
        }

        JsonNode content = response.get("content");
        if (content == null || !content.isArray() || content.isEmpty()) {
            // Surface API-level errors clearly instead of failing silently
            JsonNode error = response.get("error");
            if (error != null) {
                throw new IllegalStateException("Anthropic API error: " + error.toString());
            }
            throw new IllegalStateException("Unexpected response shape from Anthropic API: " + response);
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }
        return text.toString();
    }

}
