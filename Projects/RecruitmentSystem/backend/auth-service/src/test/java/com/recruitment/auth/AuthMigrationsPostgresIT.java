package com.recruitment.auth;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AuthMigrationsPostgresIT {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Test void migratesRefreshTokenHashAndDisablesLegacyAdmin() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration").load().migrate();
        try (Connection connection = POSTGRES.createConnection("")) {
            assertThat(columnExists(connection, "public", "refresh_tokens", "token_hash")).isTrue();
            assertThat(columnExists(connection, "public", "refresh_tokens", "token")).isFalse();
            try (ResultSet result = connection.createStatement().executeQuery(
                    "select enabled from users where email = 'admin@recruitment.local'")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getBoolean(1)).isFalse();
            }
        }
    }

    private boolean columnExists(Connection connection, String schema, String table, String column) throws Exception {
        try (ResultSet result = connection.getMetaData().getColumns(null, schema, table, column)) { return result.next(); }
    }
}
