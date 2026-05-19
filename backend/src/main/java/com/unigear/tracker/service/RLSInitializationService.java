package com.unigear.tracker.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to initialize Row-Level Security (RLS) policies on database tables
 * Note: RLS policies should be configured manually via Supabase console or migration scripts
 */
@Service
public class RLSInitializationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RLSInitializationService.class);
    
    /**
     * RLS initialization disabled - configure RLS policies manually in Supabase console
     */
    public void initializeRLS() {
        logger.info("RLS initialization disabled - configure policies manually in Supabase console");
    }
}
