package com.neltra.fasthopper;

public class ConfigManager {

    private final NeltraFastHopper plugin;

    private int hopperTickSpeed;
    private int itemsPerTransfer;
    private boolean enabled;
    private boolean debug;

    private String msgPrefix;
    private String msgReloadSuccess;
    private String msgReloadFail;
    private String msgNoPermission;
    private String msgPluginInfo;

    public ConfigManager(NeltraFastHopper plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        // Clamp hopper tick speed between 1 and 200
        hopperTickSpeed = Math.max(1, Math.min(200, plugin.getConfig().getInt("hopper-tick-speed", 1)));
        // Clamp items per transfer between 1 and 64
        itemsPerTransfer = Math.max(1, Math.min(64, plugin.getConfig().getInt("items-per-transfer", 1)));
        enabled = plugin.getConfig().getBoolean("enabled", true);
        debug = plugin.getConfig().getBoolean("debug", false);

        msgPrefix = colorize(plugin.getConfig().getString("messages.prefix", "&8[&bNeltraFastHopper&8] "));
        msgReloadSuccess = colorize(plugin.getConfig().getString("messages.reload-success", "&aReload thành công!"));
        msgReloadFail = colorize(plugin.getConfig().getString("messages.reload-fail", "&cReload thất bại!"));
        msgNoPermission = colorize(plugin.getConfig().getString("messages.no-permission", "&cKhông có quyền!"));
        msgPluginInfo = colorize(plugin.getConfig().getString("messages.plugin-info", "&bNeltraFastHopper v0.1"));
    }

    public void reload() {
        load();
    }

    private String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "\u00a7");
    }

    // Getters
    public int getHopperTickSpeed() { return hopperTickSpeed; }
    public int getItemsPerTransfer() { return itemsPerTransfer; }
    public boolean isEnabled() { return enabled; }
    public boolean isDebug() { return debug; }

    public String getMsgPrefix() { return msgPrefix; }
    public String getMsgReloadSuccess() { return msgPrefix + msgReloadSuccess; }
    public String getMsgReloadFail() { return msgPrefix + msgReloadFail; }
    public String getMsgNoPermission() { return msgPrefix + msgNoPermission; }
    public String getMsgPluginInfo() { return msgPrefix + msgPluginInfo; }
}
