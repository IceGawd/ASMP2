package me.ice.ASMP2;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				checkForPeople();
			}
		}, 0, 20 * 60);	

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
	
	Civilization civilizationFromName(String name) {
		for (Civilization c : serverInfo.civilizations) {
			if (c.toString().equals(name)) {
				return c;
			}
		}
		return null;
	}
	
	// To clarify, the index is the index of a PLAYER in the server, mostly gotten from getPlayerIndex.
	Civilization civilizationFromIndex(int index) {
		return serverInfo.civilizations.get(serverInfo.indexOfCivilization.get(index));
	}
	
	void joinCivilization(Player p, int civIndex) {
		 int index = getPlayerIndex(p);
		 if (index == -1) {
			serverInfo.playersWhoHaveJoined.add(p.getUniqueId());
			serverInfo.indexOfCivilization.add(civIndex);
		 }
		 else {
			 p.sendMessage(ChatColor.RED + "You have left the " + civilizationFromIndex(index) + " civilization!");
			 civilizationCheck(civIndex);
			 serverInfo.indexOfCivilization.set(index, civIndex);
		 }
		 p.sendMessage(ChatColor.GREEN + "You have joined the " + civilizationFromIndex(index) + " civilization!");		
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (label.equalsIgnoreCase("y")) {
			 for (Event e : events) {
				 if (e.effected == p && e.milliLength + e.timeStarted < System.currentTimeMillis()) {
					 joinCivilization(p, e.civIndex);
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
					serverInfo.civilizations.add(new Civilization(t, args[1], p.getUniqueId()));
					joinCivilization(p, serverInfo.civilizations.size() - 1);
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Format your response with the type of civilization and name");
				p.sendMessage("Example: \"/create TECH yowhatup\" or \"create Technological yowhatup\"");
			}
		}
		
		if (label.equalsIgnoreCase("disband")) {
			int index = getPlayerIndex(p);
			if (index == -1) {
				p.sendMessage(ChatColor.RED + "You can't disband a civilization if you are not in a civilization!");				
			}
			else {
				int i2 = serverInfo.indexOfCivilization.get(index);
				Civilization c = serverInfo.civilizations.get(i2);
				if (c.leader == p.getUniqueId()) {
					for (int x = 0; x < serverInfo.playersWhoHaveJoined.size(); x++) {
						if (serverInfo.indexOfCivilization.get(x) == i2) {
							serverInfo.playersWhoHaveJoined.remove(x);
							serverInfo.indexOfCivilization.remove(x);
							x--;
						}
					}
				}
				else {
					p.sendMessage(ChatColor.RED + "You can't disband a civilization if you are not the leader!");
				}
			}
		}

		if (label.equalsIgnoreCase("kick")) {
			if (args.length == 1) {
				int yourIndex = getPlayerIndex(p);
				if (yourIndex == -1) {
					p.sendMessage(ChatColor.RED + "You are not in a civilization!");					
				}
				else {
					Civilization c = civilizationFromIndex(yourIndex);
					if (c.leader == p.getUniqueId()) {
						Player gonnaGetKicked = playerFromName(args[0]);
						int index = getPlayerIndex(gonnaGetKicked);
						if (index == -1 || civilizationFromIndex(index) != c) {
							p.sendMessage(ChatColor.RED + gonnaGetKicked.getName() + " is not in your civilization!");						
						}
						else {
							serverInfo.playersWhoHaveJoined.remove(index);
							serverInfo.indexOfCivilization.remove(index);
							p.sendMessage(ChatColor.GREEN + gonnaGetKicked.getName() + " got kicked!");							
						}
					}
					else {						
						p.sendMessage(ChatColor.RED + "Only the leader of the civilization can kick players!");						
					}
				}			
			}
			else {
				p.sendMessage(ChatColor.RED + "Give the name of the player you wish to kick!");				
			}
		}
		
		if (label.equalsIgnoreCase("list")) {
			for (Civilization c : serverInfo.civilizations) {
				p.sendMessage(c.toString());
			}
		}

		
		if (label.equalsIgnoreCase("types")) {
			for (CivilizationType ct : types) {
				p.sendMessage(ct.getName());
			}
		}
		
		if (label.equalsIgnoreCase("info")) {
			if (args.length == 0) {
				int index = getPlayerIndex(p);
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Give the name of the civilization that you want the info of!");				
				}
				else {
					printCivilization(p, civilizationFromIndex(index));					
				}
			}
			else if (args.length == 1) {
				Civilization c = civilizationFromName(args[0]);
				if (c == null) {
					p.sendMessage(ChatColor.RED + args[0] + " is not the name of a civilization!");
				}
				else {
					printCivilization(p, c);
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Too many arguments!");
			}
		}
		
		if (label.equalsIgnoreCase("rename")) {
			if (args.length == 1) {
				int index = getPlayerIndex(p);
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Can't rename a civilization you aren't in!");
				}
				else {
					Civilization c = civilizationFromIndex(index);
					if (c.leader == p.getUniqueId()) {
						p.sendMessage(ChatColor.GREEN + "Name changed from " + c.name + " to " + args[0]);
						c.name = args[0];
					}
					else {
						p.sendMessage(ChatColor.RED + "Can't rename if you are not the leader!");
					}
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Give the new name for the civilization!");
			}
		}

		if (label.equalsIgnoreCase("changecolor")) {
			if (args.length == 1) {
				int index = getPlayerIndex(p);	
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Can't change color of a civilization you aren't in!");
				}
				else {
					Civilization c = civilizationFromIndex(index);
					if (c.leader == p.getUniqueId()) {
						for (ChatColor color : ChatColor.values()) {
							if (args[0].toLowerCase().equals(color.name().toLowerCase())) {
								p.sendMessage(ChatColor.GREEN + "Color changed from " + c.cc + c.cc.toString() + ChatColor.GREEN + " to " + color + color.toString());
								c.cc = color;
							}
						}
					}
					else {
						p.sendMessage(ChatColor.RED + "Only the leader can change the color of the civilization!");				
					}
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Give the color for the civilization!");				
			}
		}

		if (label.equalsIgnoreCase("leave")) {
			int index = getPlayerIndex(p);	
			if (index == -1) {
				p.sendMessage(ChatColor.RED + "Can't leave a civilization that you aren't in!");
			}
			else {
				p.sendMessage(ChatColor.GREEN + "You have left the " + civilizationFromIndex(index).name + " civilization");
				serverInfo.playersWhoHaveJoined.remove(index);
				serverInfo.indexOfCivilization.remove(index);
			}
		}

		if (label.equalsIgnoreCase("transfer")) {
			if (args.length == 1) {
				int index = getPlayerIndex(p);	
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Can't transfer leaders of a civilization you aren't in!");
				}
				else {
					Civilization c = civilizationFromIndex(index);
					if (c.leader == p.getUniqueId()) {
						Player newLeader = playerFromName(args[0]);
						if (newLeader == null) {
							p.sendMessage(ChatColor.RED + "Player is not online right now");
						}
						else {
							c.leader = newLeader.getUniqueId();
							newLeader.sendMessage(ChatColor.GREEN + p.getName() + " transfered leadership to you. Congrats!");							
						}
					}
					else {
						p.sendMessage(ChatColor.RED + "You can't transfer leadership because you're not the leader!");
					}
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Give the name of the player you are making leader!");				
			}
		}
		
		if (label.equalsIgnoreCase("media")) {
			sender.sendMessage("OFFICIAL ASMP STREAM CHANNEL: https://www.twitch.tv/bubb1ebees");
			sender.sendMessage("SUPPORT THE OWNER: https://www.youtube.com/channel/UCfLi7Y8WOtu3zclT10NNXfw");
			return true;
		}
		return false;
	}

	private void printCivilization(Player p, Civilization c) {
		p.sendMessage("Name: " + c.name);
		p.sendMessage("Type: " + c.type.name);
		p.sendMessage("Level: " + Integer.toString(c.level));
		p.sendMessage("Leader: " + Bukkit.getPlayer(c.leader).getName());
		int count = 0;
		int indexOfCivilization = indexFromCivilization(c);
		
		if (indexOfCivilization == -1) {
			p.sendMessage("Message Ice God#5963 on discord because this message should not happen");
		}
		else {
			for (int civnum : serverInfo.indexOfCivilization) {
				if (civnum == indexOfCivilization) {
					count++;
				}
			}
		}
		p.sendMessage("People in civilization: " + Integer.toString(count));
	}

	private int indexFromCivilization(Civilization c) {
		for (int x = 0; x < serverInfo.civilizations.size(); x++) {
			if (serverInfo.civilizations.get(x) == c) {
				return x;
			}
		}
		return -1;
	}

	private void civilizationCheck(int civIndex) {
		for (int i : serverInfo.indexOfCivilization) {
			if (i == civIndex) {
				return;
			}
		}
		// Dead civilization lmao
		serverInfo.civilizations.remove(civIndex);
	}

	public void addEvent(Event event) {
		removePerson(event.effected);
		events.add(event);
	}

	public void checkForPeople() {
		for (int x = 0; x < events.size(); x++) {
			if (events.get(x).milliLength + events.get(x).timeStarted < System.currentTimeMillis()) {
				events.remove(x);
				x--;
			}
		}
	}
	
	private void removePerson(Player effected) {
		for (Event e : events) {
			if (effected == e.effected) {
				events.remove(e);
			}
		}		
	}
}
