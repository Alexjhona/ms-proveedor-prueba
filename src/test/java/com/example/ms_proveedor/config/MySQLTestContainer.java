package com.example.ms_proveedor.config;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class MySQLTestContainer {

    private static final String DATABASE = "chinito_proveedor_test";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final int MYSQL_PORT = 3306;

    private static final MySQLContainer<?> INSTANCE =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName(DATABASE)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)
                    .withStartupAttempts(3);

    static {
        INSTANCE.start();
    }

    private MySQLTestContainer() {
    }

    public static String getJdbcUrl() {
        String host = INSTANCE.getHost();
        int port = INSTANCE.getMappedPort(MYSQL_PORT);

        try {
            InetAddress.getByName(host);
            return buildJdbcUrl(host, port);
        } catch (UnknownHostException exception) {
            String containerIp = INSTANCE.getContainerInfo()
                    .getNetworkSettings()
                    .getNetworks()
                    .values()
                    .stream()
                    .map(network -> network.getIpAddress())
                    .filter(ip -> ip != null && !ip.isBlank())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No se pudo determinar la direccion del contenedor MySQL",
                            exception));

            return buildJdbcUrl(containerIp, MYSQL_PORT);
        }
    }

    public static String getUsername() {
        return INSTANCE.getUsername();
    }

    public static String getPassword() {
        return INSTANCE.getPassword();
    }

    private static String buildJdbcUrl(String host, int port) {
        return "jdbc:mysql://" + host + ":" + port + "/" + DATABASE
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC";
    }
}
