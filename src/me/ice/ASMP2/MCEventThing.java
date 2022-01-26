package me.ice.ASMP2;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class MCEventThing implements Listener {
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent apce) {
		apce.setFormat("%1$s: %2$s");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent pje) {
		Player p = pje.getPlayer();
		for (int x = 0; x < Main.serverInfo.indexOfCivilization.size(); x++) {
			if (Main.serverInfo.playersWhoHaveJoined.get(x).equals(p.getUniqueId())) {
				Main.setName(p, Main.civilizationFromIndex(x));
			}
		}
	}
	
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent edbee) {
		Entity a = edbee.getDamager();
		Entity d = edbee.getEntity();
		
		if (Player.class.isInstance(a) && Player.class.isInstance(d)) {
			Player attacker = (Player) a;
			Player defender = (Player) d;
			
			int index1 = Main.getPlayerIndex(attacker);
			int index2 = Main.getPlayerIndex(defender);
			if (index1 != -1 && index2 != -1) {
				Civilization civ = Main.civilizationFromIndex(index1);
				if (civ == Main.civilizationFromIndex(index2) && !civ.friendlyFire) {
					edbee.setCancelled(true);
				}
			}
		}
	}
}