package top.vulpine.virtualBackpacks.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.instance.StorageMethod;
import top.vulpine.virtualBackpacks.util.logger.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StorageManager {

    private final VirtualBackpacks plugin;

    private HikariDataSource dataSource;

    public StorageManager(VirtualBackpacks plugin, StorageMethod method) {

        this.plugin = plugin;

        if (method == null) {
            Logger.warn("Storage method is null, defaulting to H2");
            method = StorageMethod.H2;
        }

        setup(method);

    }

    private void setup(StorageMethod method) {

        HikariConfig config = new HikariConfig();

        if (method == StorageMethod.H2) {

            String databasePath = plugin.getDataFolder().getAbsolutePath();
            config.setJdbcUrl("jdbc:h2:file:" + databasePath + "/database;MODE=MYSQL;AUTO_RECONNECT=TRUE");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(60000);
            config.setLeakDetectionThreshold(3000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);

        } else if (method == StorageMethod.MYSQL) {

            config.setJdbcUrl("jdbc:mysql://" +
                    plugin.getMainConfig().getString("storage.mysql.host") + ":" +
                    plugin.getMainConfig().getString("storage.mysql.port") + "/" +
                    plugin.getMainConfig().getString("storage.mysql.database") +
                    "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setUsername(plugin.getMainConfig().getString("storage.mysql.username"));
            config.setPassword(plugin.getMainConfig().getString("storage.mysql.password"));

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setLeakDetectionThreshold(3000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);

        }

        config.setAutoCommit(true);
        config.setValidationTimeout(3000);
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);

    }

    public CompletableFuture<ResultSet> executeQuery(String query, Object... params) {

        return CompletableFuture.supplyAsync(() -> {

            Connection connection;
            PreparedStatement statement;
            ResultSet rs;

            try {

                connection = dataSource.getConnection();
                statement = connection.prepareStatement(query);

                for (int i = 0; i < params.length; i++) {

                    statement.setObject(i + 1, params[i]);

                }

                rs = statement.executeQuery();

                return rs;

            } catch (SQLException e) {

                throw new RuntimeException("Error while executing query: ", e);

            }

        });

    }

    public CompletableFuture<Integer> executeUpdate(String query, Object... params) {

        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < params.length; i++) {

                    statement.setObject(i + 1, params[i]);

                }

                return statement.executeUpdate();

            } catch (SQLException e) {

                throw new RuntimeException("Error while executing query: ", e);

            }

        });

    }

    public CompletableFuture<Map<String, Object>> fetchOne(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) return null;
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        String label = md.getColumnLabel(i);
                        row.put(label, rs.getObject(i));
                    }
                    return row;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error while fetching row: ", e);
            }
        });
    }

    public void closeResources(AutoCloseable... resources) {

        for (AutoCloseable resource : resources) {

            if (resource != null) {

                try {

                    resource.close();

                } catch (Exception e) {

                    Logger.error("Error while closing resource: " + resource);

                }

            }
        }

    }

    public void close() {

        if (dataSource != null) {

            dataSource.close();

        }

    }

}
