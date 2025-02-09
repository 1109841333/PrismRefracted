package network.darkhelmet.prism.listeners;

import io.github.rothes.prismcn.CNLocalization;
import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.actionlibs.ActionFactory;
import network.darkhelmet.prism.actionlibs.RecordingQueue;
import network.darkhelmet.prism.utils.block.Utilities;
import network.darkhelmet.prism.utils.folia.PrismScheduler;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.List;

public class PrismWorldEvents implements Listener {

    /**
     * StructureGrowEvent.
     * @param event StructureGrowEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent event) {
        String type = "tree-grow";
        final TreeType species = event.getSpecies();
        if (species.name().toLowerCase().contains("mushroom")) {
            type = "mushroom-grow";
        }
        if (!Prism.getIgnore().event(type, event.getWorld())) {
            return;
        }
        for (final BlockState block : event.getBlocks()) {
            if (Utilities.isGrowableStructure(block.getType())) {
                if (event.getPlayer() != null) {
                    // Process by #onBlockFertilizeEvent below
                    return;
                } else {
                    RecordingQueue.addToQueue(ActionFactory.createGrow(type, block, "环境"));
                }
            }
        }
    }

    /**
     * Handle BlockFertilizeEvent.
     * @param event BlockFertilizeEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFertilizeEvent(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        List<BlockState> blocks = event.getBlocks();

        if (blocks.size() > 0 && Prism.getIgnore().event("block-fertilize", blocks.get(0).getWorld())) {
            for (BlockState newState : blocks) {
                Block oldBlock = newState.getBlock();
                if (player != null) {
                    RecordingQueue.addToQueue(ActionFactory.createBlockChange("block-fertilize", oldBlock.getType(), oldBlock.getBlockData(), newState, player));
                } else {
                    RecordingQueue.addToQueue(ActionFactory.createBlockChange("block-fertilize", oldBlock.getType(), oldBlock.getBlockData(), newState, "发射器"));
                }
            }
        }

        if (Prism.getIgnore().event("bonemeal-use", block)) {
            if (player != null) {
                RecordingQueue.addToQueue(ActionFactory.createBonemealUse(block, player));
            } else {
                RecordingQueue.addToQueue(ActionFactory.createBonemealUse(block, "发射器"));
            }
        }
    }

    /**
     * WorldLoadEvent.
     * @param event WorldLoadEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(final WorldLoadEvent event) {

        final String worldName = event.getWorld().getName();

        if (!Prism.prismWorlds.containsKey(worldName)) {
            Prism.getPrismDataSource().addWorldName(worldName);
        }
    }

    /**
     * Track portal creation events.
     * @param event PortalCreateEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent event) {
        final String type = "portal-create";
        if (!Prism.getIgnore().event(type, event.getWorld())) {
            return;
        }
        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            for (final BlockState newBlock : event.getBlocks()) {
                // Include only the nether portal blocks that were created.
                if (newBlock.getType() == Material.NETHER_PORTAL) {
                    final Entity e = event.getEntity();
                    final BlockState oldBlock = event.getWorld().getBlockAt(newBlock.getLocation()).getState();
                    if (e != null) {
                        // Run the second after the fire was placed (20 ticks), so that it is recorded after the fire.
                        // We have to do this because the database only records changes per second, not tick or instant,
                        // which can result in the fire being recorded after the nether portal blocks.
                        PrismScheduler.runTaskLater(() -> recordCreatePortal(event, type, newBlock, e, oldBlock),
                                newBlock.getLocation(), 20);
                    }
                }
            }
        } else if (event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) {
            // Record both the obsidian and portal blocks that were created.
            for (final BlockState newBlock : event.getBlocks()) {
                final Entity e = event.getEntity();
                final BlockState oldBlock = event.getWorld().getBlockAt(newBlock.getLocation()).getState();
                if (e != null) {
                    if (newBlock.getType() == Material.NETHER_PORTAL) {
                        // Run the second after the fire was placed (20 ticks), so that it is recorded after the fire.
                        // We have to do this because the database only records changes per second, not tick or instant,
                        // which can result in the fire being recorded after the nether portal blocks.
                        PrismScheduler.runTaskLater(() -> recordCreatePortal(event, type, newBlock, e, oldBlock),
                                newBlock.getLocation(), 20);
                    } else {
                        recordCreatePortal(event, type, newBlock, e, oldBlock);
                    }
                }
            }
        }
    }

    private void recordCreatePortal(PortalCreateEvent event, String type, BlockState newBlock, Entity e,
                                    BlockState oldBlock) {
        if (e instanceof Player) {
            RecordingQueue.addToQueue(ActionFactory.createPortal(type, newBlock, oldBlock, (Player) event.getEntity()));
        } else {
            RecordingQueue.addToQueue(ActionFactory.createPortal(type, newBlock, oldBlock
                    , CNLocalization.getEntityLocale(e.getType())));
        }
    }
}
