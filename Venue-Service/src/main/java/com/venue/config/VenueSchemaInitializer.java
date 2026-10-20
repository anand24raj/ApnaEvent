package com.venue.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class VenueSchemaInitializer {
    private final JdbcTemplate jdbc;
    public VenueSchemaInitializer(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @PostConstruct
    void repairIdentityColumn(){
        try { jdbc.execute("ALTER TABLE venue MODIFY COLUMN venue_id BIGINT NOT NULL AUTO_INCREMENT"); }
        catch (Exception ignored) { }
    }
}
