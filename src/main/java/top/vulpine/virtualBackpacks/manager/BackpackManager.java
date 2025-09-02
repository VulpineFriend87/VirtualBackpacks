package top.vulpine.virtualBackpacks.manager;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.instance.Backpack;
import top.vulpine.virtualBackpacks.util.logger.Logger;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BackpackManager {

    @Getter
    private final VirtualBackpacks plugin;

    @Getter
    private final List<Backpack> backpacks;

    public BackpackManager(VirtualBackpacks plugin) {
        this.plugin = plugin;

        backpacks = new ArrayList<>();

        createTables();
    }

    private void createTables() {

        String createBackpacksTable = "CREATE TABLE IF NOT EXISTS `backpacks` (" +
                " owner VARCHAR(36) NOT NULL," +
                " inventory TEXT NOT NULL," +
                " PRIMARY KEY (owner)" +
                ")";

        plugin.getStorageManager().executeUpdate(createBackpacksTable);

    }

    public CompletableFuture<Backpack> loadBackpack(UUID owner, boolean createIfNotFound) {

        Logger.info("Loading Backpack for " + owner);

        for (Backpack backpack : backpacks) {

            if (backpack.getOwner().equals(owner)) {

                Logger.info("Backpack for " + owner + " is already loaded, returning existing instance.");

                return CompletableFuture.completedFuture(backpack);

            }

        }

        CompletableFuture<Backpack> future = new CompletableFuture<>();

        String query = "SELECT owner, inventory FROM `backpacks` WHERE owner = ?";

        plugin.getStorageManager().executeQuery(query, owner.toString()).thenAccept(rs -> {

            try {

                if (rs == null) {

                    Logger.error("Error while loading Backpack for " + owner + ", ResultSet is null.");
                    future.complete(null);
                    return;

                }

                if (!rs.next()) {

                    if (!createIfNotFound) {
                        future.complete(null);
                    }

                    createBackpack(owner, true);

                    future.complete(getBackpack(owner));

                    return;

                }

                ItemStack[] contents;

                try {

                    contents = Backpack.deserializeContents(rs.getString("inventory"));

                } catch (Exception e) {

                    Logger.error("Error while deserializing Backpack contents for " + owner);
                    Logger.error(Arrays.toString(e.getStackTrace()));
                    future.completeExceptionally(e);
                    return;

                }

                Backpack backpack = new Backpack(owner, contents);
                backpacks.add(backpack);

                Logger.info("Loaded Backpack for " + owner);
                future.complete(backpack);

            } catch (SQLException e) {

                Logger.error("Error while loading Backpack for " + owner);
                Logger.error(Arrays.toString(e.getStackTrace()));
                future.completeExceptionally(e);

            } finally {

                try {

                    plugin.getStorageManager().closeResources(rs, rs.getStatement(), rs.getStatement().getConnection());

                } catch (SQLException e) {

                    Logger.error("Error while closing resources.");
                    Logger.error(Arrays.toString(e.getStackTrace()));

                }

            }

        });

        return future;

    }

    public void unloadBackpack(UUID owner) {

        Logger.info("Unloading Backpack for " + owner);

        boolean removed = backpacks.removeIf(backpack -> backpack.getOwner().equals(owner));

        if (!removed) {
            Logger.warn("Backpack for " + owner + ", not loaded. Skipping...");
            return;
        }

        Logger.info("Backpack for " + owner + " unloaded.");

    }

    public void createBackpack(UUID owner, boolean loadAfter) {

        Logger.info("Creating Backpack for " + owner);

        String query = "INSERT INTO `backpacks` (owner, inventory) VALUES (?, ?)";

        try {
            plugin.getStorageManager().executeUpdate(query, owner.toString(), Backpack.serializeContents(new ItemStack[0])).thenRun(() -> {

                Logger.info("Created Backpack for " + owner);

                if (loadAfter) {

                    loadBackpack(owner, false);

                }

            });
        } catch (Exception e) {

            Logger.error("Error while creating Backpack for " + owner);
            Logger.error(Arrays.toString(e.getStackTrace()));

        }

    }

    public CompletableFuture<Void> updateBackpack(Backpack backpack) {

        Logger.info("Updating Backpack for " + backpack.getOwner());

        String query = "INSERT INTO `backpacks` (owner, inventory) VALUES (?, ?)" +
                "ON DUPLICATE KEY UPDATE inventory = ?";

        try {

            String serialized = backpack.serializeContents();
            return plugin.getStorageManager()
                    .executeUpdate(query, backpack.getOwner().toString(), serialized, serialized)
                    .whenComplete((res, ex) -> {
                        if (ex != null) {
                            Logger.error("Error while updating Backpack for " + backpack.getOwner());
                            Logger.error(Arrays.toString(ex.getStackTrace()));
                        } else {
                            Logger.info("Updated Backpack for " + backpack.getOwner());
                        }
                    })
                    .thenApply(i -> null);

        } catch (Exception e) {

            Logger.error("Error while updating Backpack for " + backpack.getOwner());
            Logger.error(Arrays.toString(e.getStackTrace()));
            return CompletableFuture.failedFuture(e);

        }

    }

    public Backpack getBackpack(UUID owner) {

        for (Backpack backpack : backpacks) {

            if (backpack.getOwner().equals(owner)) {

                return backpack;

            }

        }

        return null;

    }

}

