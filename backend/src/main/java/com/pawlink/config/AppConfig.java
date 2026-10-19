package com.pawlink.config;

import com.pawlink.common.db.DBConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

/**
 * Application Configuration for core infrastructure beans.
 * Configures BCrypt password encoder, binds DataSource to DBConnectionManager singleton,
 * and sets up JdbcTemplate.
 */
@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DBConnectionManager dbConnectionManager(DataSource dataSource) {
        DBConnectionManager manager = DBConnectionManager.getInstance();
        manager.initialize(dataSource);
        return manager;
    }
}
