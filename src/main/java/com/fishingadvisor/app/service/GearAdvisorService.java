package com.fishingadvisor.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishingadvisor.app.dto.GearAdviceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GearAdvisorService {

    private static final String SYSTEM_PROMPT = """
            You are an expert fishing tackle advisor. Your job is to recommend a complete, \
            realistic rod-and-reel combo setup based on the angler's target species, \
            technique, water type, experience level, and - most importantly - their budget.

            When a user provides their target species, technique, water type, experience \
            level, and budget tier, respond with:

            Rod:
            Specific rod type, length, power/action, and a realistic price range that fits \
            the stated budget tier. Explain briefly why this spec suits the technique and species.

            Reel:
            Specific reel type and size (e.g. "2500 spinning reel" or "7.1:1 baitcaster"), \
            and a realistic price range within budget.

            Line:
            Line type (mono, fluoro, braid), test/pound rating, and why it fits the water \
            type and technique.

            Leader (if ideal):
            Only include this section if a leader actually improves the setup for this \
            species/technique/water type. If not needed, write "Not necessary for this setup" \
            instead of forcing a recommendation.

            Why This Setup Works:
            2-3 sentences connecting the gear choices to the technique, species, and experience \
            level.

            Budget Rules (critical):
            - Budget tier means: "Budget" = entry-level, most affordable functional gear. \
            "Mid-Range" = solid intermediate gear, better components, moderate price. \
            "Premium" = high-end gear, top components, price is not the primary constraint.
            - NEVER recommend gear that exceeds the stated budget tier. If trade-offs are \
            necessary to stay in budget, say so plainly rather than quietly upgrading.
            - Give realistic approximate price ranges (e.g. "$40-60") for each component so \
            the angler knows what to expect to spend.

            Other rules:
            - Tailor entirely to the target species and technique. A crappie setup is not a \
            musky setup.
            - Adjust complexity and terminology to the stated experience level - keep it \
            approachable for beginners, more technical for advanced anglers.
            - Freshwater and saltwater require different corrosion resistance and gear - \
            treat them differently.
            - Always give a complete, confident recommendation. Never ask a clarifying \
            question or leave a section blank - this page has no way for the angler to \
            reply, so any request for more info is a dead end for them.
            - If the inputs seem unusual or contradictory (e.g. a species not typically \
            found in the stated water type, or an inland species paired with saltwater), \
            don't stop and ask - make the most reasonable interpretation, note the \
            assumption in one brief sentence, and still deliver a full setup.
            - Keep the total response under 300 words.
            """;

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GearAdvisorService(WebClient anthropicWebClient,
                               ObjectMapper objectMapper,
                               @Value("${anthropic.api.model}") String model) {
        this.anthropicWebClient = anthropicWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public String getGearAdvice(GearAdviceRequest request) {
        String userPrompt = buildUserPrompt(request);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 600,
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

    private String buildUserPrompt(GearAdviceRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Target species: ").append(request.getTargetSpecies()).append("\n");
        sb.append("Technique: ").append(request.getTechnique()).append("\n");
        sb.append("Water type: ").append(request.getWaterType()).append("\n");
        sb.append("Experience level: ").append(request.getExperienceLevel()).append("\n");
        sb.append("Budget tier: ").append(request.getBudgetTier()).append("\n");

        sb.append("\nWhat rod, reel, line, and leader (if ideal) should I use?");
        return sb.toString();
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException("Empty response from Anthropic API");
        }

        JsonNode content = response.get("content");
        if (content == null || !content.isArray() || content.isEmpty()) {
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
