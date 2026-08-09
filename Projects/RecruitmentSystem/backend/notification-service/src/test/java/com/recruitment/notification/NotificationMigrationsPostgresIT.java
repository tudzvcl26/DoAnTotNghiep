package com.recruitment.notification;

import org.flywaydb.core.Flyway; import org.junit.jupiter.api.Test; import org.testcontainers.containers.PostgreSQLContainer; import org.testcontainers.junit.jupiter.Container; import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection; import java.sql.ResultSet;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class NotificationMigrationsPostgresIT {
    @Container static final PostgreSQLContainer<?> POSTGRES=new PostgreSQLContainer<>("postgres:17");
    @Test void migratesIdempotencyReceiptVersion()throws Exception{
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword()).schemas("notification_service").defaultSchema("notification_service").table("notification_service_flyway_schema_history").locations("classpath:db/migration").load().migrate();
        try(Connection c=POSTGRES.createConnection("");ResultSet r=c.getMetaData().getColumns(null,"notification_service","notification_event_receipts","event_version")){assertThat(r.next()).isTrue();}
    }
}
