package com.loopers.testcontainers;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class MySqlTestContainersConfig {

    private static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("loopers")
                .withUsername("test")
                .withPassword("test")
                .withExposedPorts(3306)
                .withCommand(
                        "--character-set-server=utf8mb4",
                        "--collation-server=utf8mb4_general_ci",
                        "--skip-character-set-client-handshake");
        MYSQL_CONTAINER.start();

        String mySqlJdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s",
                MYSQL_CONTAINER.getHost(), MYSQL_CONTAINER.getFirstMappedPort(), MYSQL_CONTAINER.getDatabaseName());

        System.setProperty("datasource.mysql-jpa.main.jdbc-url", mySqlJdbcUrl);
        System.setProperty("datasource.mysql-jpa.main.username", MYSQL_CONTAINER.getUsername());
        System.setProperty("datasource.mysql-jpa.main.password", MYSQL_CONTAINER.getPassword());
    }
}
