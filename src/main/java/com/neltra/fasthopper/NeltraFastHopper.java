package com.neltra.fasthopper;

import org.bukkit.plugin.java.JavaPlugin;

public class NeltraFastHopper extends JavaPlugin {

    private static NeltraFastHopper instance;
    private ConfigManager configManager;
    private HopperManager hopperManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Init managers
        configManager = new ConfigManager(this);
        hopperManager = new HopperManager(this);

        // Register listener
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);

        // Register command
        getCommand("neltrahopper").setExecutor(new HopperCommand(this));
        getCommand("neltrahopper").setTabCompleter(new HopperCommand(this));

        getLogger().info("================================================");
        getLogger().info("  NeltraFastHopper v0.1 đã khởi động!");
        getLogger().info("  Folia 1.21.11 | Hopper tick: " + configManager.getHopperTickSpeed());
        getLogger().info("  Items/transfer: " + configManager.getItemsPerTransfer());
        getLogger().info("  Trạng thái: " + (configManager.isEnabled() ? "BẬT" : "TẮT"));
        getLogger().info("================================================");
    }

    @Override
    public void onDisable() {
        if (hopperManager != null) {
            hopperManager.shutdown();
        }
        getLogger().info("[NeltraFastHopper] Plugin đã tắt.");
    }

    public static NeltraFastHopper getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HopperManager getHopperManager() {
        return hopperManager;
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        hopperManager.reload();
        getLogger().info("[NeltraFastHopper] Config đã reload! Tick speed: " + configManager.getHopperTickSpeed());
    }
}
