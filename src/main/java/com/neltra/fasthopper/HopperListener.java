package com.neltra.fasthopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * HopperListener - Lắng nghe sự kiện để quản lý hopper
 * Folia 1.21.11 compatible
 */
public class HopperListener implements Listener {

    private final NeltraFastHopper plugin;

    public HopperListener(NeltraFastHopper plugin) {
        this.plugin = plugin;
    }

    /**
     * Khi đặt hopper → đăng ký fast tick ngay
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) return;
        if (!plugin.getConfigManager().isEnabled()) return;

        plugin.getHopperManager().registerHopper(block.getLocation());
    }

    /**
     * Khi phá hopper → hủy task
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) return;

        plugin.getHopperManager().unregisterHopper(block.getLocation());
    }

    /**
     * Khi chunk load → đăng ký tất cả hopper trong chunk đó
     * Đây là bước quan trọng để hopper được quản lý khi server restart
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;

        // Folia: dùng RegionScheduler để chạy code trong đúng region thread
        Location chunkLoc = new Location(event.getWorld(),
                event.getChunk().getX() * 16,
                64,
                event.getChunk().getZ() * 16);

        plugin.getServer().getRegionScheduler().run(plugin, chunkLoc, task -> {
            for (org.bukkit.block.BlockState state : event.getChunk().getTileEntities(
                    entity -> entity instanceof org.bukkit.block.Hopper, true)) {
                plugin.getHopperManager().registerHopper(state.getLocation());
            }
        });
    }

    /**
     * Khi chunk unload → hủy tất cả task hopper trong chunk đó
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (org.bukkit.block.BlockState state : event.getChunk().getTileEntities(
                entity -> entity instanceof org.bukkit.block.Hopper, true)) {
            plugin.getHopperManager().unregisterHopper(state.getLocation());
        }
    }

    /**
     * Cancel vanilla hopper transfer event để tránh double transfer
     * Plugin sẽ tự xử lý transfer bằng RegionScheduler
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;

        // Chỉ cancel nếu nguồn là hopper
        if (event.getInitiator().getHolder() instanceof org.bukkit.block.Hopper) {
            event.setCancelled(true);
        }
    }
}
