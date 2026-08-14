package interfaces;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum AdmConexion {
    INSTANCE;

    private final Logger log = Logger.getLogger(AdmConexion.class.getName());
    private final HikariDataSource dataSource;

    AdmConexion() {
        this.dataSource = inicializarPool();
    }

    private HikariDataSource inicializarPool() {
        try {
            log.info("[DB-LOG] Iniciando configuración del pool HikariCP para Biblioteca...");

            String envUrl  = System.getenv("DB_URL");
            String envHost = System.getenv("DB_HOST");
            String envUser = System.getenv("DB_USER");
            String envPass = System.getenv("DB_PASS");
            String envName = System.getenv("MYSQL_DATABASE");

            HikariConfig config = new HikariConfig();
            config.setPoolName("BibliotecaPool");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setConnectionTestQuery("SELECT 1");

            if (envUrl != null && !envUrl.isEmpty()) {
                log.info("[DB-LOG] Modo: PRODUCCIÓN (DB_URL)");
                config.setJdbcUrl(envUrl);
                config.setUsername(envUser != null ? envUser : "root");
                config.setPassword(envPass != null ? envPass : "");
            } else if (envHost != null && !envHost.isEmpty()) {
                // Soporte nativo para Docker Compose usando DB_HOST (ej: "db")
                log.info("[DB-LOG] Modo: DOCKER COMPOSE (DB_HOST)");
                String dbName = (envName != null && !envName.isEmpty()) ? envName : "probiblioteca";
                String jdbcUrl = "jdbc:mysql://" + envHost + ":3306/" + dbName + "?useSSL=false&serverTimezone=UTC";
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(envUser != null ? envUser : "root");
                config.setPassword(envPass != null ? envPass : "");
            } else {
                log.warning("[DB-LOG] Modo: DESARROLLO (database.properties)");
                Properties props = new Properties();
                try (InputStream is = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("database.properties")) {
                    if (is == null) throw new IOException("database.properties no encontrado en classpath");
                    props.load(is);
                }
                config.setJdbcUrl(props.getProperty("db.url"));
                config.setUsername(props.getProperty("db.user", "root"));
                config.setPassword(props.getProperty("db.pass"));
            }

            // Configuraciones comunes de HikariCP
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(300_000);
            config.setMaxLifetime(600_000);

            log.info("[DB-LOG] URL de conexión establecida: " + config.getJdbcUrl());

            HikariDataSource ds = new HikariDataSource(config);
            log.info("[DB-LOG] Pool listo. Tamaño máximo: " + config.getMaximumPoolSize());
            return ds;

        } catch (IOException e) {
            log.log(Level.SEVERE, "[DB-LOG] Error leyendo database.properties", e);
            throw new ExceptionInInitializerError(e);
        } catch (Exception e) {
            log.log(Level.SEVERE, "[DB-LOG] Error crítico inicializando HikariCP", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public Connection obtenerConexion() throws SQLException {
        Connection conn = dataSource.getConnection();
        log.info("[DB-LOG] Conexión obtenida. Activas: "
                + dataSource.getHikariPoolMXBean().getActiveConnections());
        return conn;
    }

    public void cerrarPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("[DB-LOG] Pool cerrado correctamente.");
        }
    }
}