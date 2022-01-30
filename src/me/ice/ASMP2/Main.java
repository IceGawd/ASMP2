package me.ice.ASMP2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import me.ice.ASMP2.ability.BlockThrowAbility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {
	private static Main instance;
	static InfoToSave serverInfo;
	static ArrayList<Event> events = new ArrayList<Event>();
	// ICE stands for Internal Civilization Extention
	static final String path = "./civilizations.ice";
	static final String[] programmers = {
		"IceGod9001", 	
	};
	static final String owner = "827690fe-69f4-49bf-9540-f9a29088b9b8";
	static final CivilizationType[] types = new CivilizationType[] {
		new CivilizationType("Produce", "PROD"), 
		new CivilizationType("Construct", "CONS"), 
		new CivilizationType("Technological", "TECH"), 
		new CivilizationType("Friendly", "LOVE"), 

// 		LATER DEFAULT
//		new CivilizationType("Atlantic", "ATLA"), 
//		new CivilizationType("Taskmaster", "TASK"), 
//		new CivilizationType("Dwarven", "DWAR"), 
//		new CivilizationType("Traveler", "TRAV"), 

//		DLC
//		new CivilizationType("Necromancer", "NCRO"), 
//		new CivilizationType("Ranger", "RANG"), 
//		new CivilizationType("Transportation", "UBER"), 
//		new CivilizationType("Kung-Fu", "MONK"), 
	};
	
	@Override
	public void onEnable() {
		instance = this;
		getServer().getPluginManager().registerEvents(new MCEventThing(), this);

		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			serverInfo = (InfoToSave) in.readObject();
			in.close();
			fileIn.close();
		}
		catch (Exception e) {
			serverInfo = new InfoToSave();
			Bukkit.broadcastMessage("This might not be an actual error lol");
			e.printStackTrace();
		}
		
		for (int x = 0; x < serverInfo.playersWhoHaveJoined.size(); x++) {
			System.out.println("UUIDs: " + serverInfo.playersWhoHaveJoined.get(x));
		}
		
		System.out.println(serverInfo.playersWhoHaveJoined.size());
		System.out.println(serverInfo.indexOfCivilization.size());
		System.out.println(serverInfo.civilizations.size());
		
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				// Check if the invite request is over or not
				checkForPeople();
				// Check if civilizations are empty
				for (int x = 0; x < serverInfo.civilizations.size(); x++) {
					civilizationCheck(x);
				}
				// Change people's display names
				for (Player p : Bukkit.getOnlinePlayers()) {
					int index = serverInfo.playersWhoHaveJoined.indexOf(p.getUniqueId());
					if (index != -1) {
						Main.setName(p, civilizationFromIndex(index));
					}
				}
			}

		}, 0, 20 * 60);
	}

	public static void setName(Player p, Civilization civ) {
		if (civ == null) {
			p.setDisplayName(p.getName());
		}
		else {
			p.setDisplayName(civ.cc + civ.toString() + " | " + p.getName() + ChatColor.WHITE);
		}
	}	
	
	@Override
	public void onDisable() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(serverInfo);
			out.close();
			fileOut.close();
		} catch (Exception e) {
			Bukkit.broadcastMessage("This is prolly a bad message");
			e.printStackTrace();
		}
	}
	
	int getPlayerIndex(Player p) {
		UUID searchFor = p.getUniqueId();
		
		for (int x = 0; x < serverInfo.playersWhoHaveJoined.size(); x++) {
			if (serverInfo.playersWhoHaveJoined.get(x).equals(searchFor)) {
				return x;
			}
		}
		return -1;
	}
		
	Civilization civilizationFromName(String name) {
		for (Civilization c : serverInfo.civilizations) {
			if (c.name.toLowerCase().equals(name.toLowerCase())) {
				return c;
			}
		}
		return null;
	}
	
	// To clarify, the index is the index of a PLAYER in the server, mostly gotten from getPlayerIndex.
	public static Civilization civilizationFromIndex(int index) {
		return serverInfo.civilizations.get(serverInfo.indexOfCivilization.get(index));
	}
	
	void joinCivilization(Player p, int civIndex) {
		int index = getPlayerIndex(p);
		if (index == -1) {
			serverInfo.playersWhoHaveJoined.add(p.getUniqueId());
			serverInfo.indexOfCivilization.add(civIndex);
			index = serverInfo.indexOfCivilization.size() - 1;
		}
		else {
			p.sendMessage(ChatColor.RED + "You have left the " + civilizationFromIndex(index) + " civilization!");
			civilizationCheck(civIndex);
			serverInfo.indexOfCivilization.set(index, civIndex);
		}
		Civilization civ = civilizationFromIndex(index);
		p.sendMessage(ChatColor.GREEN + "You have joined the " + civ + " civilization!");
		setName(p, civ);
		for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
			Player player = Bukkit.getPlayer(serverInfo.playersWhoHaveJoined.get(x));
			if ((serverInfo.indexOfCivilization.get(x) == civIndex) && (player != null)) {
				player.sendMessage(ChatColor.GREEN + p.getName() + " joined!");
			}
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public void actualCommand(Player p, String label, String[] args) {
		if (label.equalsIgnoreCase("accept")) {
			 for (Event e : events) {
				 if (e.effected == p && e.milliLength + e.timeStarted > System.currentTimeMillis()) {
					 joinCivilization(p, e.civIndex);
				 }
			 }
		}
		if (label.equalsIgnoreCase("deny")) {
			removePerson(p);
		}

		if (label.equalsIgnoreCase("invite")) {
			if (args.length == 1) {
				Player on = Bukkit.getPlayer(args[0]);
				if (on == null) {
					p.sendMessage(ChatColor.RED + "Player name is either misspelled or player is not online");
				}
				else {
					int index = getPlayerIndex(p);
					if (index == -1) {
						p.sendMessage(ChatColor.RED + "You are not in a civilization!");					
					}
					else {
						int civindex = serverInfo.indexOfCivilization.get(index);
						Civilization civ = serverInfo.civilizations.get(civindex);
						p.sendMessage(ChatColor.GREEN + "Invite sent!");
						on.sendMessage("You have been invited to " + civ.toString() + " by " + p.getName());
						on.sendMessage("Reply with /civ accept (yes) or /civ deny (no) within 60 seconds");
						addEvent(new Event(on, civindex, 60000));
					}					
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Please run the command again but with a player name");
			}
		}
		
		if (label.equalsIgnoreCase("create")) {
			if (args.length == 2) {
//				p.sendMessage(args[0]);
//				p.sendMessage(args[1]);
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
					int index = getPlayerIndex(p);
					if (isCivilizationName(args[1])) {
						p.sendMessage(ChatColor.RED + "A civilization with that name already exists.");
					} else if (index == -1 || !civilizationFromIndex(index).leader.equals(p.getUniqueId())) {
						serverInfo.civilizations.add(new Civilization(t, args[1], p.getUniqueId()));
						joinCivilization(p, serverInfo.civilizations.size() - 1);
					}
					else {
						p.sendMessage(ChatColor.RED + "You cannot create a new civilization because you are the leader of the " + civilizationFromIndex(index) + " civilization!");
					}
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
				if (c.leader.equals(p.getUniqueId())) {
					for (int x = 0; x < serverInfo.playersWhoHaveJoined.size(); x++) {
						if (serverInfo.indexOfCivilization.get(x) == i2) {
							Player getFuckedLmao = Bukkit.getPlayer(serverInfo.playersWhoHaveJoined.get(x));
							if (getFuckedLmao != null) {
								getFuckedLmao.sendMessage(ChatColor.RED + c.toString() + " was disbanded!");
								setName(getFuckedLmao, null);
							}
							serverInfo.playersWhoHaveJoined.remove(x);
							serverInfo.indexOfCivilization.remove(x);
							x--;
						}
					}
					civilizationCheck(i2);
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
					int civIndex = serverInfo.indexOfCivilization.get(yourIndex);
					Civilization c = serverInfo.civilizations.get(civIndex);
					if (c.leader.equals(p.getUniqueId())) {
						Player gonnaGetKicked = Bukkit.getPlayer(args[0]);
						if (gonnaGetKicked == null) {
							p.sendMessage(ChatColor.RED + args[0] + " is either misspelled or player is not online");
						}
						else {
							int index = getPlayerIndex(gonnaGetKicked);
							if (index == -1 || civilizationFromIndex(index) != c) {
								p.sendMessage(ChatColor.RED + gonnaGetKicked.getName() + " is not in your civilization!");						
							}
							else {
								setName(gonnaGetKicked, null);
								serverInfo.playersWhoHaveJoined.remove(index);
								serverInfo.indexOfCivilization.remove(index);
								for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
									Player player = Bukkit.getPlayer(serverInfo.playersWhoHaveJoined.get(x));
									if ((serverInfo.indexOfCivilization.get(x) == civIndex) && (player != null)) {
										p.sendMessage(ChatColor.RED + gonnaGetKicked.getName() + " got kicked!");
									}
								}
							}
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
					int civIndex = serverInfo.indexOfCivilization.get(index);
					Civilization c = serverInfo.civilizations.get(civIndex);
					if (c.leader.equals(p.getUniqueId())) {
						String oldname = c.name;
						c.name = args[0];
						for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
							if (serverInfo.indexOfCivilization.get(x) == civIndex) {
								Player inCiv = Bukkit.getPlayer(serverInfo.playersWhoHaveJoined.get(x));
								if (inCiv != null) {
									setName(inCiv, c);
									inCiv.sendMessage(c.cc + "Name changed from " + oldname + " to " + c.name);
								}
							}
						}
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
					int civIndex = serverInfo.indexOfCivilization.get(index);
					Civilization c = serverInfo.civilizations.get(civIndex);
					if (c.leader.equals(p.getUniqueId())) {
						for (ChatColor color : ChatColor.values()) {
							if (args[0].toLowerCase().equals(color.name().toLowerCase())) {
								ChatColor oldColor = c.cc;
								c.cc = color;
								for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
									if (serverInfo.indexOfCivilization.get(x) == civIndex) {
										Player inCiv = Bukkit.getPlayer(serverInfo.playersWhoHaveJoined.get(x));
										if (inCiv != null) {
											setName(inCiv, c);
											inCiv.sendMessage(ChatColor.GREEN + "Color changed from " + oldColor + oldColor.name() + ChatColor.GREEN + " to " + c.cc + c.cc.name());
										}
									}
								}
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

		if (label.equalsIgnoreCase("colors")) {
			for (ChatColor cc : ChatColor.values()) {
				p.sendMessage(cc + cc.name());
			}
		}
		
		if (label.equalsIgnoreCase("leave")) {
			int index = getPlayerIndex(p);	
			if (index == -1) {
				p.sendMessage(ChatColor.RED + "Can't leave a civilization that you aren't in!");
			}
			else {
				int civIndex = serverInfo.indexOfCivilization.get(index);
				Civilization civ = serverInfo.civilizations.get(civIndex);
				p.sendMessage(ChatColor.GREEN + "You have left the " + civ.name + " civilization");
				serverInfo.playersWhoHaveJoined.remove(index);
				serverInfo.indexOfCivilization.remove(index);
				for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
					if (serverInfo.indexOfCivilization.get(x) == civIndex) {
						Player inCiv = Bukkit.getPlayer(serverInfo.playersWhoHaveJoined.get(x));
						if (inCiv != null) {
							inCiv.sendMessage(ChatColor.RED + p.getName() + " has left the " + civ.toString() + " civilization!");
						}
					}
				}
				civilizationCheck(civIndex);
				setName(p, null);
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
					if (c.leader.equals(p.getUniqueId())) {
						Player newLeader = Bukkit.getPlayer(args[0]);
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
		if (label.equalsIgnoreCase("tester")) {
			boolean work = false;
			for (String s : programmers) {
				if (s.equals(p.getName())) {
					work = true;
				}
			}
			if (work) {
				p.sendMessage("UUIDs: ");
				for (UUID id : serverInfo.playersWhoHaveJoined) {
					p.sendMessage(Bukkit.getOfflinePlayer(id).getName() + ": " + id);
				}
				p.sendMessage("Indexes: ");
				for (int i : serverInfo.indexOfCivilization) {
					p.sendMessage(Integer.toString(i));
				}
				for (Civilization c : serverInfo.civilizations) {
					printCivilization(p, c);
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Programmers only ;p");
			}
		}
		
		if (label.equalsIgnoreCase("clear")) {
			// Remove this temp ass Icegod shit
			if (p.getUniqueId().equals(UUID.fromString(owner)) || p.getName().equals("IceGod9001")) {
				if (args.length == 1) {
					Player clearer = Bukkit.getPlayer(args[0]);
					if (clearer == null) {
						p.sendMessage("Homie, " + args[0] + " is not online or smth idk");
					}
					else {
						int index = getPlayerIndex(clearer);
						if (index == -1) {
							p.sendMessage("Homie, I can't clear em, they aint in a civ");
						}
						else {
							serverInfo.indexOfCivilization.remove(index);
							serverInfo.playersWhoHaveJoined.remove(index);
							p.sendMessage("Cleared homie!");
						}
					}
				}
				else {
					p.sendMessage("Homie, you gotta gimme the name of the person");
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Crookit only!");
			}
		}
				
		if (label.equalsIgnoreCase("help")) {
			printHelp(p);
		}
		
		if (label.equalsIgnoreCase("cheat")) {
			Bukkit.broadcastMessage(ChatColor.GRAY + "[CONSOLE: Making Ice God and Crookit A GOD...]");
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (label.equalsIgnoreCase("civ")) {
			if (args.length > 0) {
				String subcommand = args[0];
				String[] actualArgs = new String[args.length - 1];
				for (int x = 1; x < args.length; x++) {
					actualArgs[x - 1] = args[x];
				}
				actualCommand(p, subcommand, actualArgs);
				return true;
			}
			else {
				printHelp(p);
			}
		}
		if (label.equalsIgnoreCase("help")) {
			printHelp(p);
		}
		if (label.equalsIgnoreCase("media")) {
			p.sendMessage("OFFICIAL ASMP STREAM CHANNEL: https://www.twitch.tv/bubb1ebees");
			p.sendMessage("SUPPORT THE OWNER: https://www.youtube.com/channel/UCfLi7Y8WOtu3zclT10NNXfw");
		}

		return false;
	}

	private void printHelp(Player p) {
		p.sendMessage("""
--------- Help: ASMP2 ---------------------------
Below is a list of all ASMP2 commands:
/civ invite 
Invite someone to your team!
/civ create 
Create your own team with this format: /civ [CIV TYPE] [CIV NAME]
/civ disband
Wipe your team off the SMP
/civ kick
Kick someone out of your team
/civ list
List all of the current existing teams
/civ types
List of all the types of civilizations on the SMP
/civ info
Info of your civilization
/civ rename
Rename your civilization
/civ changecolor
Change the color of your civilization
/civ leave
Leave your civilization : (
/civ transfer
Transfer team leadership to someone else
/civ colors
List of all the colors available for you team to change into
/civ accept
Accept a team invite 
/civ deny
Deny a team invite 
""");		
	}

	private void printCivilization(Player p, Civilization c) {
		p.sendMessage("Name: " + c.name);
		p.sendMessage("Type: " + c.type.name);
		p.sendMessage("Level: " + Integer.toString(c.level));
		p.sendMessage("Leader: " + Bukkit.getOfflinePlayer(c.leader).getName());
		int count = 0;
		int indexOfCivilization = indexFromCivilization(c);
		
		ArrayList<OfflinePlayer> inCivilization = new ArrayList<OfflinePlayer>();
		
		if (indexOfCivilization == -1) {
			p.sendMessage("Message Ice God#5963 on discord because this message should not happen");
		}
		else {
			for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
				if (serverInfo.indexOfCivilization.get(x) == indexOfCivilization) {
					count++;
					inCivilization.add(Bukkit.getOfflinePlayer(serverInfo.playersWhoHaveJoined.get(x)));
				}
			}
		}
		p.sendMessage("People in civilization: ");
		for (OfflinePlayer op : inCivilization) {
			p.sendMessage(op.getName());
		}
		p.sendMessage("Total people in civilization: " + Integer.toString(count));
	}

	private int indexFromCivilization(Civilization c) {
		for (int x = 0; x < serverInfo.civilizations.size(); x++) {
			if (serverInfo.civilizations.get(x) == c) {
				return x;
			}
		}
		return -1;
	}

	/**
	 * Check if a civilization with {@code name} already exists.
	 * @param name
	 * @return {@code true} if there's a match. {@code false} otherwise
	 */
	private boolean isCivilizationName(String name) {
		return serverInfo.civilizations.stream().anyMatch(civilization -> civilization.name.equalsIgnoreCase(name));
	}

	private void civilizationCheck(int civIndex) {
		for (int i : serverInfo.indexOfCivilization) {
			if (i == civIndex) {
				return;
			}
		}
		// Dead civilization lmao
		serverInfo.civilizations.remove(civIndex);
		// Fix other civIndexes
		for (int x = 0; x < serverInfo.indexOfCivilization.size(); x++) {
			int num = serverInfo.indexOfCivilization.get(x);
			if (num > civIndex) {
				serverInfo.indexOfCivilization.set(x, num - 1);
			}
		}
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
				return;
			}
		}		
	}

	public static Main getInstance() {
		return instance;
	}
}
