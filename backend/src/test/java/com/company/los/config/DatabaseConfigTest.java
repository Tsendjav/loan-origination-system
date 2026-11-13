package com.company.los.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DatabaseConfig-ийн драйвер тодорхойлох логикийн тест.
 *
 * <p>Регресс: драйвер нь H2 хатуу бичигдсэнээс болж postgres URL-тай
 * docker профайл дээр `Driver org.h2.Driver claims to not accept jdbcUrl`
 * гэж эхлэхгүй байсан.</p>
 */
class DatabaseConfigTest {

    @Test
    void postgresUrlGetsPostgresDriver() {
        assertEquals("org.postgresql.Driver",
                DatabaseConfig.resolveDriverClassName(null,
                        "jdbc:postgresql://postgres:5432/los_db"));
        assertEquals("org.postgresql.Driver",
                DatabaseConfig.resolveDriverClassName("  ",
                        "jdbc:postgresql://localhost:5432/los_db"));
    }

    @Test
    void h2UrlGetsH2Driver() {
        assertEquals("org.h2.Driver",
                DatabaseConfig.resolveDriverClassName(null, "jdbc:h2:mem:losdb"));
    }

    @Test
    void explicitConfigurationWins() {
        assertEquals("org.postgresql.Driver",
                DatabaseConfig.resolveDriverClassName("org.postgresql.Driver",
                        "jdbc:h2:mem:losdb"));
    }

    @Test
    void unknownOrMissingUrlLeavesDriverToHikari() {
        assertNull(DatabaseConfig.resolveDriverClassName(null, null));
        assertNull(DatabaseConfig.resolveDriverClassName(null, "jdbc:oracle:thin:@db:1521:xe"));
    }
}
