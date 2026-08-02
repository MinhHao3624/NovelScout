package com.minhhao.novelscout;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesCoreSchemaAndSeedsReferenceData() {
        Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
        Integer categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);

        assertThat(roleCount).isEqualTo(2);
        assertThat(categoryCount).isEqualTo(7);
    }
}
