package me.ice.ASMP2.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Objects;

public class BlockThrowAbility {

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
}