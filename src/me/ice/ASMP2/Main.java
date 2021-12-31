package me.ice.ASMP2;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	InfoToSave serverInfo;
	ArrayList<Event> events;
	static final CivilizationType[] types = new CivilizationType[] {
		new CivilizationType("Produce", "PROD"), 
		new CivilizationType("Construct", "CONS"), 
		new CivilizationType("Technlogical", "TECH"), 
//		new CivilizationType("Necromancer", "NCRO"), 
//		new CivilizationType("Ranger", "RANG"), 
//		new CivilizationType("Nuker / Demolition", "NUKE / DEMO"), 
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
	
	Civilization civilizationFromIndex(int index) {
		return serverInfo.civilizations.get(serverInfo.indexOfCivilization.get(index));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (label.equalsIgnoreCase("y")) {
			 for (Event e : events) {
				 if (e.effected == p && e.milliLength + e.timeStarted < System.currentTimeMillis()) {
					 int index = getPlayerIndex(p);
					 if (index == -1) {
						serverInfo.playersWhoHaveJoined.add(p.getUniqueId());
						serverInfo.indexOfCivilization.add(e.civIndex);
					 }
					 else {
						 p.sendMessage(ChatColor.RED + "You have left the " + civilizationFromIndex(index) + " civilization!");
						 serverInfo.indexOfCivilization.set(index, e.civIndex);
					 }
					 p.sendMessage(ChatColor.GREEN + "You have joined the " + civilizationFromIndex(index) + " civilization!");
				 }
			 }
		}
		if (label.equalsIgnoreCase("n")) {
			removePerson(p);
		}

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
						Civilization civ = civilizationFromIndex(index);
						sender.sendMessage(ChatColor.GREEN + "Invite sent!");
						on.sendMessage("You have been invited to " + civ.toString() + " by " + p.getName());
						on.sendMessage("Reply with /y (yes) or /n (no) within 60 seconds");
						addEvent(new Event(on, index, 60000));
					}					
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Please run the command again but with a player name");
			}
		}
		
		if (label.equalsIgnoreCase("create")) {
			if (args.length == 2) {
				CivilizationType t = null;
				for (CivilizationType c : types) {
					if (c.equals(args[0])) {
						t = c;
					}
				}
				if (t == null) {
					p.sendMessage(ChatColor.RED + args[0] + " is not a civilization type!");					
				}
				else {
					serverInfo.civilizations.add(new Civilization(t, args[1]));
					int index = getPlayerIndex(p);
					if (index == -1) {
						p.sendMessage("You have been invited to " + civ.toString() + " by " + p.getName());
						p.sendMessage("Reply with /y (yes) or /n (no) within 60 seconds");						
					}
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Format your response with the type of civilization and name");
				p.sendMessage("Example: \"/create TECH yowhatup\" or \"create Technological yowhatup\"");
			}
		}
		
		if (label.equalsIgnoreCase("media")) {
			sender.sendMessage("Media my balls idiot");
			return true;
		}
		return false;
	}

	public void addEvent(Event event) {
		removePerson(event.effected);
		events.add(event);
	}

	private void removePerson(Player effected) {
		for (Event e : events) {
			if (effected == e.effected) {
				events.remove(e);
			}
		}		
	}
}
