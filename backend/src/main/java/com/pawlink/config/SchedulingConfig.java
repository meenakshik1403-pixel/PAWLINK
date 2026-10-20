package com.pawlink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring task scheduling for the automated reminder observer engine.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

