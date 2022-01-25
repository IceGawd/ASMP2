package me.ice.ASMP2.ability;

import me.ice.ASMP2.Main;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BlockThrowAbility {

    private static final int blockPickupDistance = 4;
    private static Set<UUID> playersPerformingAbility = new HashSet<>();

    public static void test(Player player, Vector velocity) {
        var world = player.getWorld();
        var blockInHand = player.getInventory().getItemInMainHand();

        //TODO Remove this code once misidentification issue is fixed.
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

        // Get the block the player is looking at.
        var result = player.rayTraceBlocks(blockPickupDistance);

        Block rayTracedBlock;

        // Ensure that ray-tracing returned a block. If the result is valid
        // play block break sound indicating that a block has been picked up.
        if (result == null || (rayTracedBlock = result.getHitBlock()) == null) return;
        player.playSound(rayTracedBlock.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);

        if (!player.isBlocking())
            return;

        playersPerformingAbility.add(player.getUniqueId());

        new BukkitRunnable() {
            private int blockingDuration;
            private boolean fullyCharged;

            @Override
            public void run() {
                // Check if the player is blocking. If they are, then they
                // are charging their shot.
                if (player.isBlocking()) {
                    if (blockingDuration < 8) {
                        blockingDuration++;
                        player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 0.5f);
                    } else {
                        // Check if the players shot is fully charged. If it is, then
                        // play a clicking sound. The boolean prevents the sound from being repeated while
                        // the players shot remains fully charged.
                        if (!fullyCharged) {
                            fullyCharged = true;
                            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                        }
                    }
                } else {
                    // The blocking duration is divided by 4 to cap the maximum speed a shot can get charged
                    // to 2.0.
                    shootBlock(player, rayTracedBlock, blockingDuration / 4.0f);

                    // Remove the player from the list of players that are currently performing this ability.
                    // If the player isn't removed then they will repeatedly shoot blocks.
                    playersPerformingAbility.remove(player.getUniqueId());

                    // Cancel this runnable task.
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 5, 5);
    }

    /**
     * Shoots a block from the players location in the direction they are facing at the specified speed.
     *
     * @param shooter The player shooting the block.
     * @param block   The block being shot.
     * @param speed   The speed of the block.
     */
    private static void shootBlock(Player shooter, Block block, float speed) {
        var blockProjectile = shooter.getWorld().spawnFallingBlock(shooter.getLocation(), block.getBlockData());
        blockProjectile.setVelocity(shooter.getLocation().getDirection().multiply(speed));
    }
}