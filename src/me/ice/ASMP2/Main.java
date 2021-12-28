package me.ice.ASMP2;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	InfoToSave serverInfo;
	static final CivilizationType[] types = new CivilizationType[] {
		new CivilizationType("Produce", "PROD", ChatColor.GREEN), 
		new CivilizationType("Construct", "CONS", ChatColor.GRAY), 
		new CivilizationType("Technlogical", "TECH", ChatColor.GOLD), 
	};
	
	@Override
	public void onEnable() {
		serverInfo = new InfoToSave();
	}
	
	@Override
	public void onDisable() {
		
	}
	
	int getPlayerIndex(Player p) {
		UUID searchFor = p.getUniqueId();
		for (int x = 0; x < serverInfo.playersWhoHaveJoined.size(); x++) {
			if (serverInfo.playersWhoHaveJoined.get(x) == searchFor) {
				return x;
			}
		}
		return -1;
	}
	
	Player playerFromName(String name) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().equals(name)) {
				return p;
			}
		}	
		return null;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (label.equalsIgnoreCase("invite")) {
			if (args.length == 1) {
				Player on = playerFromName(args[0]);
				if (on == null) {
					sender.sendMessage(ChatColor.RED + "Player name is either misspelled or player is not online");
				}
				else {
					int index = getPlayerIndex(p);
					if (index == -1) {
						sender.sendMessage(ChatColor.RED + "You are not in a civilization!");					
					}
					else {
						sender.sendMessage(ChatColor.GREEN + "Invite sent!");
					}					
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Please run the command again but with a player name");
			}
		}
		
		if (label.equalsIgnoreCase("media")) {
			sender.sendMessage("Media my balls idiot");
			return true;
		}
		return false;
	}
}
