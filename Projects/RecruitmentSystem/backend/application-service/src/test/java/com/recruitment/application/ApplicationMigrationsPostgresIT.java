package com.recruitment.application;

import org.flywaydb.core.Flyway; import org.junit.jupiter.api.Test; import org.testcontainers.containers.PostgreSQLContainer; import org.testcontainers.junit.jupiter.Container; import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection; import java.sql.ResultSet;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ApplicationMigrationsPostgresIT {
    @Container static final PostgreSQLContainer<?> POSTGRES=new PostgreSQLContainer<>("postgres:17");
    @Test void migratesJsonbSnapshotsAndOutbox()throws Exception{
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword()).schemas("application_service").defaultSchema("application_service").table("application_service_flyway_schema_history").locations("classpath:db/migration").load().migrate();
        try(Connection connection=POSTGRES.createConnection("")){
            assertThat(type(connection,"job_snapshots","snapshot_data")).isEqualTo("jsonb");
            assertThat(type(connection,"resume_snapshots","snapshot_data")).isEqualTo("jsonb");
            assertThat(tableExists(connection,"application_outbox_events")).isTrue();
            assertThat(tableExists(connection,"candidate_profile_snapshots")).isTrue();
            assertThat(type(connection,"applications","candidate_profile_snapshot_id")).isEqualTo("uuid");
            assertThat(type(connection,"applications","applied_at_instant")).isEqualTo("timestamp with time zone");
            assertThat(type(connection,"application_status_histories","changed_at_instant")).isEqualTo("timestamp with time zone");
        }
    }
    private String type(Connection c,String table,String column)throws Exception{try(var ps=c.prepareStatement("select data_type from information_schema.columns where table_schema='application_service' and table_name=? and column_name=?")){ps.setString(1,table);ps.setString(2,column);try(ResultSet r=ps.executeQuery()){assertThat(r.next()).isTrue();return r.getString(1);}}}
    private boolean tableExists(Connection c,String table)throws Exception{try(ResultSet r=c.getMetaData().getTables(null,"application_service",table,new String[]{"TABLE"})){return r.next();}}
}
