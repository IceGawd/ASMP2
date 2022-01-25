package me.ice.ASMP2.ability;

import me.ice.ASMP2.Main;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BlockThrowAbility {

    private static Set<UUID> playersPerformingAbility = new HashSet<>();

    public static void test(Player player, Vector velocity) {
        var world = player.getWorld();
        var blockInHand = player.getInventory().getItemInMainHand();
        if (!blockInHand.getType().isBlock() || blockInHand.getType().isAir()) {
            player.sendMessage("The item in hand is not a block. (" + blockInHand.getType() + ")");
            return;
        }
        var fallingBlock = world.spawnFallingBlock(player.getLocation(), Objects.requireNonNull(blockInHand.getData()));
        fallingBlock.setVelocity(velocity);
        fallingBlock.setHurtEntities(true);
    }

    public static void performAbility(Player player) {
        // If the player is already performing the ability, return.
        if (playersPerformingAbility.contains(player.getUniqueId()))
            return;

        // Get the block the player is looking at. (Maximum 3 blocks away.)
        var result = player.rayTraceBlocks(3);

        Block hitBlock;

        // Ensure that ray-tracing returned a block. If the result is valid
        // play block break sound indicating that a block has been picked up.
        if (result == null || (hitBlock = result.getHitBlock()) == null) return;
        player.playSound(hitBlock.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);

        if (!player.isBlocking()) {
            player.sendMessage("Player is not blocking.");
            return;
        } else player.sendMessage("Player is blocking.");

        playersPerformingAbility.add(player.getUniqueId());

        new BukkitRunnable() {
            private int blockingTimer;
            private boolean fullyCharged;

            @Override
            public void run() {
                if (player.isBlocking()) {
                    if (blockingTimer < 8) {
                        blockingTimer++;
                        player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 0.5f);
                    } else {
                        // The players shot is fully charged.
                        if (!fullyCharged) {
                            fullyCharged = true;
                            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                        }
                    }
                } else {
                    // Spawn and discharge the block from the players position to where they are looking.
                    var block = player.getWorld().spawnFallingBlock(player.getLocation(), hitBlock.getBlockData());
                    block.setVelocity(player.getLocation().getDirection().multiply(2));
                    playersPerformingAbility.remove(player.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 5, 5);
    }
}