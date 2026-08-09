package com.recruitment.ai;

import org.flywaydb.core.Flyway; import org.junit.jupiter.api.Test; import org.testcontainers.containers.PostgreSQLContainer; import org.testcontainers.junit.jupiter.Container; import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection; import java.sql.ResultSet;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AiMigrationsPostgresIT {
    @Container static final PostgreSQLContainer<?> POSTGRES=new PostgreSQLContainer<>("postgres:17");
    @Test void migratesPostgresJsonbColumns()throws Exception{
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword()).schemas("ai_service").defaultSchema("ai_service").table("ai_service_flyway_schema_history").locations("classpath:db/migration").load().migrate();
        try(Connection c=POSTGRES.createConnection("");ResultSet r=c.createStatement().executeQuery("select count(*) from information_schema.columns where table_schema='ai_service' and data_type='jsonb'")){assertThat(r.next()).isTrue();assertThat(r.getInt(1)).isGreaterThan(0);assertThat(c.getMetaData().getColumns(null,"ai_service","resume_documents","deleted_at").next()).isTrue();}
    }
}
