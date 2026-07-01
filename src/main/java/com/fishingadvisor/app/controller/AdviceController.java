package com.fishingadvisor.app.controller;

import com.fishingadvisor.app.dto.AdviceRequest;
import com.fishingadvisor.app.dto.AdviceResponse;
import com.fishingadvisor.app.dto.GearAdviceRequest;
import com.fishingadvisor.app.service.FishingAdvisorService;
import com.fishingadvisor.app.service.GearAdvisorService;
import com.fishingadvisor.app.service.QueryLimitService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // tighten this to your actual frontend origin before going live
public class AdviceController {

    private final FishingAdvisorService fishingAdvisorService;
    private final GearAdvisorService gearAdvisorService;
    private final QueryLimitService queryLimitService;

    public AdviceController(FishingAdvisorService fishingAdvisorService,
                             GearAdvisorService gearAdvisorService,
                             QueryLimitService queryLimitService) {
        this.fishingAdvisorService = fishingAdvisorService;
        this.gearAdvisorService = gearAdvisorService;
        this.queryLimitService = queryLimitService;
    }

    @PostMapping("/advice")
    public ResponseEntity<AdviceResponse> getAdvice(@Valid @RequestBody AdviceRequest request,
                                                      HttpSession session) {
        String sessionId = session.getId();

        if (!queryLimitService.hasFreeQueriesRemaining(sessionId)) {
            AdviceResponse paywallResponse = new AdviceResponse(
                    null,
                    0,
                    true
            );
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(paywallResponse);
        }

        String advice = fishingAdvisorService.getAdvice(request);
        int remaining = queryLimitService.recordQueryAndGetRemaining(sessionId);

        return ResponseEntity.ok(new AdviceResponse(advice, remaining, false));
    }

    @GetMapping("/advice/remaining")
    public ResponseEntity<Integer> getRemainingQueries(HttpSession session) {
        return ResponseEntity.ok(queryLimitService.getRemaining(session.getId()));
    }

    @PostMapping("/gear-advice")
    public ResponseEntity<AdviceResponse> getGearAdvice(@Valid @RequestBody GearAdviceRequest request,
                                                          HttpSession session) {
        String sessionId = session.getId();

        if (!queryLimitService.hasFreeQueriesRemaining(sessionId)) {
            AdviceResponse paywallResponse = new AdviceResponse(
                    null,
                    0,
                    true
            );
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(paywallResponse);
        }

        String advice = gearAdvisorService.getGearAdvice(request);
        int remaining = queryLimitService.recordQueryAndGetRemaining(sessionId);

        return ResponseEntity.ok(new AdviceResponse(advice, remaining, false));
    }

}
