package me.ice.ASMP2.ability;

import me.ice.ASMP2.Main;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockThrowAbility {

    private static final int cooldownDuration = 5;
    private static final int maxChargeDuration = 3 * 20; // 3 * 20 = 60 ticks (3 seconds)
    private static final int blockPickupDistance = 2;
    private static final Sound blockPickupSound = Sound.BLOCK_STONE_BREAK;
    private static final Sound maxChargeSound = Sound.UI_BUTTON_CLICK;
    private static final Sound chargingSound = Sound.BLOCK_AMETHYST_BLOCK_STEP;
    private static final Sound entityHitSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    private static final Set<UUID> playersPerformingAbility = new HashSet<>();
    private static final Map<UUID, Integer> playersCoolingDown = new ConcurrentHashMap<>();

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                playersCoolingDown.entrySet().removeIf(entry -> entry.getValue() <= 0);
                playersCoolingDown.forEach((key, value) -> playersCoolingDown.put(key, value - 1));
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    /**
     * Attempts to have a player perform this ability. This method will return if the player is already performing the
     * ability, if they are currently cooling down from this ability, or if they perform any actions that would
     * cancel this ability (such as canceling their block).
     * @param player
     */
    public static void performAbility(Player player) {
        // If the player is already performing the ability, return.
        if (playersPerformingAbility.contains(player.getUniqueId()))
            return;
        if (playersCoolingDown.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + String.format("You must wait %d more seconds.", playersCoolingDown.get(player.getUniqueId())));
            return;
        }

        // Get the block the player is looking at.
        var result = player.rayTraceBlocks(blockPickupDistance);

        Block rayTracedBlock;

        // Ensure that ray-tracing returned a block. If the result is valid
        // play block break sound indicating that a block has been picked up.
        if (result == null || (rayTracedBlock = result.getHitBlock()) == null) return;
        player.playSound(rayTracedBlock.getLocation(), blockPickupSound, 1.0f, 1.0f);

        if (!player.isBlocking())
            return;

        playersPerformingAbility.add(player.getUniqueId());

        new BukkitRunnable() {
            private int chargeDuration;
            private boolean fullyCharged;

            @Override
            public void run() {
                // Check if the player is blocking. If they are, then they
                // are charging their shot.
                if (player.isBlocking()) {
                    if (chargeDuration < maxChargeDuration) {
                        if (chargeDuration > 0 && chargeDuration % 20 == 0) // Only play sound every second.
                            player.playSound(player, chargingSound, 0.5f, 0.5f);
                        chargeDuration++;
                    } else {
                        // Check if the players shot is fully charged. If it is, then
                        // play a clicking sound. The boolean prevents the sound from being repeated while
                        // the players shot remains fully charged.
                        if (!fullyCharged) {
                            fullyCharged = true;
                            player.playSound(player, maxChargeSound, 1.0f, 1.0f);
                        }
                    }
                } else {
                    shootBlock(player, rayTracedBlock, chargeDuration / 30.0f); // Dividing by 30 makes max speed 2

                    // Remove the player from the list of players that are currently performing this ability.
                    // If the player isn't removed then they will repeatedly shoot blocks.
                    playersPerformingAbility.remove(player.getUniqueId());
                    playersCoolingDown.put(player.getUniqueId(), cooldownDuration);

                    // Cancel this runnable task.
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 1, 1);
    }

    /**
     * Shoots a block from the players location in the direction they are facing at the specified speed.
     *
     * @param shooter The player shooting the block.
     * @param block   The block being shot.
     * @param speed   The speed of the block.
     */
    private static void shootBlock(Player shooter, Block block, float speed) {
        var eyeVector = shooter.getEyeLocation().getDirection().multiply(speed);
        var fallingBlock = shooter.getWorld().spawnFallingBlock(shooter.getEyeLocation(), block.getBlockData());
        fallingBlock.setVelocity(eyeVector);
        trackUntilCollision(new BlockProjectile(shooter, fallingBlock, speed * 5));
    }

    /**
     * Tracks a projectile in the air until it collides with something like a block or entity.
     * @param projectile The projectile to track.
     */
    private static void trackUntilCollision(BlockProjectile projectile) {
        var fallingBlock = projectile.block();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the falling block is valid (hasn't despawned or disappeared), and cancel tracking if true.
                if (!fallingBlock.isValid()) {
                    cancel();
                    return;
                }
                var nearbyEntities = getNearbyLivingEntities(fallingBlock);
                if (!isNearbyEntitiesListValid(nearbyEntities, projectile.shooter()))
                    return;
                applyDamageAndKnockback(nearbyEntities, fallingBlock.getVelocity(), projectile.damage(), fallingBlock);
                projectile.shooter().playSound(projectile.shooter(), entityHitSound, 1.0f, 1.0f);
                fallingBlock.remove();
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 1, 1);
    }

    /**
     * Gets other living entities near the specified entities in a horizontal radius of 0.5 meters and a vertical
     * radius of 1 meter.
     * @param from The entity whose radius is being scanned.
     * @return A list of living entities within the radius.
     */
    private static List<LivingEntity> getNearbyLivingEntities(Entity from) {
        return from.getNearbyEntities(0.5f, 1.0f, 0.5f).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .toList();
    }

    /**
     * Checks if the list of entities is valid (not empty, and doesn't just contain the {@code exception}).
     * @param entities The list validated.
     * @param exception The entity that should not be alone in the list.
     * @return True if the list is valid, false otherwise.
     */
    private static boolean isNearbyEntitiesListValid(List<LivingEntity> entities, LivingEntity exception) {
        return !(entities.isEmpty() || (entities.size() == 1 && entities.get(0).equals(exception)));
    }

    /**
     * Applies damage and knockback to a list of living entities.
     * @param entities The entities to apply damage and knockback to.
     * @param knockback The knockback force.
     * @param damage The amount of damage to apply.
     * @param source The damage source.
     */
    private static void applyDamageAndKnockback(List<LivingEntity> entities, Vector knockback, float damage, Entity source) {
        entities.forEach(entity -> {
            entity.damage(damage, source);
            entity.setVelocity(entity.getVelocity().add(knockback));
        });
    }
}

/**
 * Represents a projectile that is a block (falling block), containing the person who launched the projectile, and
 * the amount of damage this projectile should do upon hitting an entity.
 */
record BlockProjectile(Player shooter, FallingBlock block, float damage) {

}