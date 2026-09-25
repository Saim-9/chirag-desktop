package com.chirag.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory cache with TTL (Time To Live) for database query results.
 * Prevents redundant network round-trips when navigating between screens.
 * 
 * How it works:
 * - First visit to Dashboard/Marketplace → fetches from DB, stores in cache
 * - Navigating away and back → serves from cache instantly (no DB call)
 * - After TTL expires (30s) → next access re-fetches fresh data
 * - Explicit invalidation after mutations (purchase, top-up, etc.)
 * 
 * Use-case: Performance Optimization, UX Improvement.
 */
public class DataCache {

    private static final Logger logger = LoggerFactory.getLogger(DataCache.class);

    /** Singleton instance */
    private static final DataCache INSTANCE = new DataCache();

    /** Default TTL: 30 seconds — fresh enough for a desktop app */
    private static final long DEFAULT_TTL_MS = 30_000;

    /** Thread-safe storage for cached entries */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private DataCache() {}

    public static DataCache getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves cached data if it exists and hasn't expired.
     * Returns null if the cache miss or entry is stale.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() - entry.timestamp > DEFAULT_TTL_MS) {
            cache.remove(key);
            logger.debug("Cache expired: {}", key);
            return null;
        }
        logger.debug("Cache hit: {}", key);
        return (T) entry.data;
    }

    /**
     * Stores data in the cache with the default TTL.
     */
    public void put(String key, Object data) {
        cache.put(key, new CacheEntry(data, System.currentTimeMillis()));
        logger.debug("Cache stored: {}", key);
    }

    /**
     * Removes a specific cache entry (e.g., after a purchase).
     */
    public void invalidate(String key) {
        cache.remove(key);
        logger.debug("Cache invalidated: {}", key);
    }

    /**
     * Removes all entries matching a prefix (e.g., "dashboard_" clears all dashboard data).
     */
    public void invalidatePrefix(String prefix) {
        cache.keySet().removeIf(k -> k.startsWith(prefix));
        logger.debug("Cache invalidated prefix: {}", prefix);
    }

    /**
     * Clears the entire cache (e.g., on logout).
     */
    public void invalidateAll() {
        cache.clear();
        logger.debug("Cache fully cleared");
    }

    // ====== Cache Keys (centralized to avoid typos) ======

    public static final String MARKETPLACE_COURSES = "marketplace_courses";
    public static final String MARKETPLACE_RATINGS = "marketplace_ratings";

    public static String enrolledCourses(int userId) {
        return "dashboard_enrolled_" + userId;
    }

    public static String instructorCourses(int userId) {
        return "dashboard_instructor_" + userId;
    }

    public static String transactions(int userId) {
        return "dashboard_transactions_" + userId;
    }

    public static String lectureCounts(int userId) {
        return "dashboard_lecture_counts_" + userId;
    }

    /**
     * Internal cache entry storing data + insertion timestamp.
     */
    private static class CacheEntry {
        final Object data;
        final long timestamp;

        CacheEntry(Object data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
