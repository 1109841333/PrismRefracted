package network.darkhelmet.prism.database;

import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.database.mysql.MySqlPrismDataSource;
import network.darkhelmet.prism.database.mysql.PrismHikariDataSource;
import network.darkhelmet.prism.database.sql.SqlPrismDataSource;
import network.darkhelmet.prism.database.sql.SqlPrismDataSourceUpdater;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.Locale;

public class PrismDatabaseFactory {

    private static PrismDataSource database = null;

    /**
     * Create a config.
     * @param configuration ConfigurationSection
     */
    public static void createDefaultConfig(final ConfigurationSection configuration) {
        ConfigurationSection dataSourceSection;
        ConfigurationSection  dataSourceProperties;
        if (configuration.isConfigurationSection("datasource")) {
            dataSourceSection = configuration.getConfigurationSection("datasource");
            dataSourceSection.addDefault("type","mysql");
            if (!dataSourceSection.isConfigurationSection("properties")) {
                dataSourceProperties = dataSourceSection.createSection("properties");
            } else {
                dataSourceProperties = dataSourceSection.getConfigurationSection("properties");
            }
        } else {
            String type = configuration.getString("datasource");//gets the old datasource.
            dataSourceSection = configuration.createSection("datasource");
            if (type != null) {
                dataSourceSection.set("type", type);
            } else {
                dataSourceSection.addDefault("type", "mysql");
            }
            dataSourceProperties = dataSourceSection.createSection("properties");
        }
        String dataType = dataSourceSection.getString("type","mysql");
        updateDataSourceProperties(dataType, dataSourceProperties);
        addDatabaseDefaults(configuration);
    }

    private static void updateDataSourceProperties(@Nullable final String type,
                                                   final ConfigurationSection configuration) {
        String test = type;
        if (test == null) {
            test = "mysql";
        }
        switch (test.toUpperCase(Locale.ROOT)) {
            case "MYSQL":
                MySqlPrismDataSource.updateDefaultConfig(configuration);
                break;
            case "HIKARI":
            default:
                SqlPrismDataSource.updateDefaultConfig(configuration);
        }
    }

    private static void addDatabaseDefaults(ConfigurationSection section) {
        upgradeEntry(section, "query.max-failures-before-wait", "prism.query.max-failures-before-wait", 3);
        upgradeEntry(section, "query.actions-per-insert-batch", "prism.query.actions-per-insert-batch", 1000);
        upgradeEntry(section, "query.force-write-queue-on-shutdown", "prism.query.force-write-queue-on-shutdown", true);
        upgradeEntry(section, "prism.queue-empty-tick-delay", "prism.query.queue-empty-tick-delay", 3);
    }

    private static void upgradeEntry(ConfigurationSection section, String oldPath, String newPath, Object def) {
        Object old = section.get(oldPath);
        section.set(oldPath, null);
        section.addDefault(newPath, old == null ? def : old);
    }

    /**
     * Constuct Data source.
     * @param configuration ConfigurationSection
     * @return PrismDataSource
     */
    public static PrismDataSource createDataSource(ConfigurationSection configuration) {
        if (configuration == null) {
            return null;
        }

        String dataSource = configuration.getString("datasource.type");
        ConfigurationSection dataSourceProperties = configuration.getConfigurationSection("datasource.properties");
        switch (dataSource.toUpperCase(Locale.ROOT)) {
            case "MYSQL":
                Prism.log("正在尝试以 MySQL 配置数据源.");
                database = new MySqlPrismDataSource(dataSourceProperties);
                break;
            case "HIKARI":
                Prism.log("正在尝试使用 Hikari 参数配置数据源.");
                database = new PrismHikariDataSource(dataSourceProperties);
                break;
            case "SQLITE":
                Prism.warn("错误: 该版本的 Prism 已不再支持 SQLite.");
                break;
            case "derby":
                Prism.warn("错误: 该版本的 Prism 已不再支持 Derby. 请使用 Hikari.");
                break;
            default:
                Prism.warn("错误: Prism 没有针对 " + dataSource + " 数据源类型的规则. 请使用 MySQL 或 Hikari.");
                break;
        }
        return database;
    }

    /**
     * Create updater for datasource.
     * @param configuration ConfigurationSection
     * @return PrismDataSourceUpdater
     */
    public static PrismDataSourceUpdater createUpdater(ConfigurationSection configuration) {
        if (configuration == null) {
            return null;
        }
        switch (configuration.getString("datasource.type").toUpperCase(Locale.ROOT)) {
            case "MYSQL":
            case "HIKARI":
                return new SqlPrismDataSourceUpdater(database);
            default:
                return null;
        }
    }

    public static Connection getConnection() {
        return database.getConnection();
    }

}
