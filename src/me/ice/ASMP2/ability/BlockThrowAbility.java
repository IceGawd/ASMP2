package me.ice.ASMP2.ability;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

public class BlockThrowAbility {

    public static void test(Player player, Vector velocity) {
        var world = player.getWorld();
        var blockInHand = player.getInventory().getItemInMainHand();
        var fallingBlock = world.spawnFallingBlock(player.getLocation(), Objects.requireNonNull(blockInHand.getData()));
        fallingBlock.setVelocity(velocity);
    }
}
