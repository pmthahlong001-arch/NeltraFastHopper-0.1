package com.neltra.fasthopper;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HopperManager - Quản lý logic hút item nhanh cho hopper
 * Dành cho Folia 1.21.11 - sử dụng RegionScheduler thread-safe
 */
public class HopperManager {

    private final NeltraFastHopper plugin;

    // Map lưu task đang chạy cho từng hopper (key = location string)
    private final Map<String, ScheduledTask> activeTasks = new ConcurrentHashMap<>();

    public HopperManager(NeltraFastHopper plugin) {
        this.plugin = plugin;
    }

    /**
     * Đăng ký một hopper để chạy fast tick
     * Phải được gọi từ region thread của hopper đó
     */
    public void registerHopper(Location loc) {
        if (!plugin.getConfigManager().isEnabled()) return;

        String key = locationKey(loc);
        if (activeTasks.containsKey(key)) return; // đã có task rồi

        World world = loc.getWorld();
        if (world == null) return;

        int tickSpeed = plugin.getConfigManager().getHopperTickSpeed();

        // Dùng RegionScheduler của Folia - thread-safe cho từng region
        ScheduledTask task = plugin.getServer().getRegionScheduler().runAtFixedRate(
                plugin,
                loc,
                scheduledTask -> {
                    // Kiểm tra block vẫn là hopper
                    Block block = loc.getBlock();
                    if (block.getType() != Material.HOPPER) {
                        unregisterHopper(loc);
                        return;
                    }

                    // Kiểm tra plugin có bật không
                    if (!plugin.getConfigManager().isEnabled()) {
                        return;
                    }

                    // Thực hiện transfer item
                    performTransfer(block);
                },
                1L,        // delay ban đầu (1 tick)
                tickSpeed  // lặp lại mỗi N tick theo config
        );

        activeTasks.put(key, task);

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("[Debug] Registered hopper task at " + key
                    + " | tick=" + tickSpeed);
        }
    }

    /**
     * Hủy task của một hopper
     */
    public void unregisterHopper(Location loc) {
        String key = locationKey(loc);
        ScheduledTask task = activeTasks.remove(key);
        if (task != null) {
            task.cancel();
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info("[Debug] Unregistered hopper task at " + key);
            }
        }
    }

    /**
     * Logic transfer item từ hopper sang container bên dưới hoặc inventory xung quanh
     */
    private void performTransfer(Block hopperBlock) {
        if (!(hopperBlock.getState() instanceof Hopper hopperState)) return;

        Inventory hopperInv = hopperState.getInventory();
        if (isEmpty(hopperInv)) return;

        // Lấy block bên dưới hopper
        Block below = hopperBlock.getRelative(0, -1, 0);
        Inventory targetInv = getInventoryOfBlock(below);

        if (targetInv == null) return;

        int itemsToMove = plugin.getConfigManager().getItemsPerTransfer();
        int moved = 0;

        for (int i = 0; i < hopperInv.getSize() && moved < itemsToMove; i++) {
            ItemStack item = hopperInv.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            // Cố gắng thêm vào inventory đích
            ItemStack toMove = item.clone();
            toMove.setAmount(Math.min(item.getAmount(), itemsToMove - moved));

            Map<Integer, ItemStack> leftover = targetInv.addItem(toMove);

            int movedAmount = toMove.getAmount() - (leftover.isEmpty() ? 0 : leftover.get(0).getAmount());

            if (movedAmount > 0) {
                item.setAmount(item.getAmount() - movedAmount);
                if (item.getAmount() <= 0) {
                    hopperInv.setItem(i, null);
                }
                moved += movedAmount;

                if (plugin.getConfigManager().isDebug()) {
                    plugin.getLogger().info("[Debug] Hopper transferred " + movedAmount + "x "
                            + toMove.getType() + " at " + locationKey(hopperBlock.getLocation()));
                }
            }
        }
    }

    /**
     * Lấy Inventory của một block (chest, barrel, furnace, dropper, dispenser, v.v.)
     */
    private Inventory getInventoryOfBlock(Block block) {
        if (block == null) return null;
        if (!(block.getState() instanceof org.bukkit.inventory.InventoryHolder holder)) return null;
        return holder.getInventory();
    }

    private boolean isEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null && !item.getType().isAir()) return false;
        }
        return true;
    }

    /**
     * Tạo key string cho location
     */
    private String locationKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    /**
     * Reload - hủy tất cả task cũ (listener sẽ tự đăng ký lại khi hopper tick)
     */
    public void reload() {
        shutdown();
        plugin.getLogger().info("[NeltraFastHopper] HopperManager reloaded. Tick speed: "
                + plugin.getConfigManager().getHopperTickSpeed());
    }

    /**
     * Hủy tất cả task khi plugin tắt
     */
    public void shutdown() {
        for (ScheduledTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
    }

    public int getActiveTaskCount() {
        return activeTasks.size();
    }
}
