package com.fishingadvisor.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks free query usage per session in memory.
 *
 * This is intentionally simple for the MVP: no database, resets on server
 * restart, and doesn't survive across browser sessions. Swap this for a
 * Postgres-backed table once you add real user accounts + Stripe.
 */
@Service
public class QueryLimitService {

    private final ConcurrentHashMap<String, AtomicInteger> usageBySession = new ConcurrentHashMap<>();
    private final int freeQueryLimit;

    public QueryLimitService(@Value("${app.free-query-limit}") int freeQueryLimit) {
        this.freeQueryLimit = freeQueryLimit;
    }

    public boolean hasFreeQueriesRemaining(String sessionId) {
        return usageBySession.getOrDefault(sessionId, new AtomicInteger(0)).get() < freeQueryLimit;
    }

    public int recordQueryAndGetRemaining(String sessionId) {
        int used = usageBySession.computeIfAbsent(sessionId, id -> new AtomicInteger(0))
                .incrementAndGet();
        return Math.max(0, freeQueryLimit - used);
    }

    public int getRemaining(String sessionId) {
        int used = usageBySession.getOrDefault(sessionId, new AtomicInteger(0)).get();
        return Math.max(0, freeQueryLimit - used);
    }

}
