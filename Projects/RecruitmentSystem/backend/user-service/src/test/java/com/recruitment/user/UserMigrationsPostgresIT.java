package com.recruitment.user;

import org.flywaydb.core.Flyway; import org.junit.jupiter.api.Test; import org.testcontainers.containers.PostgreSQLContainer; import org.testcontainers.junit.jupiter.Container; import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection; import java.sql.ResultSet;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class UserMigrationsPostgresIT {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");
    @Test void migratesResumeVersioningAndRelations() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())
                .schemas("user_service").defaultSchema("user_service").table("user_service_flyway_schema_history")
                .locations("classpath:db/migration").load().migrate();
        try(Connection connection=POSTGRES.createConnection("")){
            assertThat(columnExists(connection,"profile_assets","asset_version")).isTrue();
            assertThat(columnExists(connection,"profile_assets","is_current")).isTrue();
            assertThat(columnExists(connection,"candidate_cvs","candidate_id")).isTrue();
            assertThat(columnExists(connection,"candidate_cvs","content_json")).isTrue();
            try(ResultSet result=connection.createStatement().executeQuery("select count(*) from pg_indexes where schemaname='user_service' and indexname='uq_profile_assets_current_resume'")){
                assertThat(result.next()).isTrue(); assertThat(result.getInt(1)).isEqualTo(1);
            }
        }
    }
    private boolean columnExists(Connection connection,String table,String column)throws Exception{try(ResultSet result=connection.getMetaData().getColumns(null,"user_service",table,column)){return result.next();}}
}
