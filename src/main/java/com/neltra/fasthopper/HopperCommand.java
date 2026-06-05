package com.neltra.fasthopper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * HopperCommand - Xử lý lệnh /neltrahopper
 */
public class HopperCommand implements CommandExecutor, TabCompleter {

    private final NeltraFastHopper plugin;

    public HopperCommand(NeltraFastHopper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();

        if (!sender.hasPermission("neltrahopper.admin")) {
            sender.sendMessage(cfg.getMsgNoPermission());
            return true;
        }

        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                try {
                    plugin.reload();
                    sender.sendMessage(cfg.getMsgReloadSuccess());
                    sender.sendMessage(cfg.getMsgPrefix()
                            + "\u00a77Tick speed: \u00a7b" + cfg.getHopperTickSpeed()
                            + " \u00a77| Items/transfer: \u00a7b" + cfg.getItemsPerTransfer()
                            + " \u00a77| Enabled: \u00a7b" + cfg.isEnabled());
                } catch (Exception e) {
                    sender.sendMessage(cfg.getMsgReloadFail());
                    plugin.getLogger().severe("Reload error: " + e.getMessage());
                }
            }
            case "info" -> sendInfo(sender);
            case "status" -> {
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Active tasks: \u00a7b"
                        + plugin.getHopperManager().getActiveTaskCount());
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Tick speed: \u00a7b"
                        + cfg.getHopperTickSpeed() + " tick");
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Items/transfer: \u00a7b"
                        + cfg.getItemsPerTransfer());
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Enabled: \u00a7b"
                        + cfg.isEnabled());
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Debug: \u00a7b"
                        + cfg.isDebug());
            }
            default -> {
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a7cLệnh không hợp lệ!");
                sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Dùng: /nfh [reload|info|status]");
            }
        }

        return true;
    }

    private void sendInfo(CommandSender sender) {
        ConfigManager cfg = plugin.getConfigManager();
        sender.sendMessage(cfg.getMsgPrefix() + "\u00a7bNeltraFastHopper \u00a7fv0.1");
        sender.sendMessage(cfg.getMsgPrefix() + "\u00a77Folia 1.21.11 Fast Hopper Plugin");
        sender.sendMessage(cfg.getMsgPrefix() + "\u00a77\u00a7lCommands:");
        sender.sendMessage(cfg.getMsgPrefix() + "\u00a77  /nfh reload \u00a7f- Reload config");
        sender.sendMessage(cfg.getMsgPrefix() + "\u00a77  /nfh status \u00a7f- Xem trạng thái");
        sender.sendMessage(cfg.getMsgPrefix() + "\u00a77  /nfh info   \u00a7f- Thông tin plugin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("neltrahopper.admin")) return Collections.emptyList();
        if (args.length == 1) {
            return Arrays.asList("reload", "info", "status");
        }
        return Collections.emptyList();
    }
}
