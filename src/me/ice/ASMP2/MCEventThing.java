package me.ice.ASMP2;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
}