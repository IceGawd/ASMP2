/*package me.ice.ASMP2;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override 
	public void onEnable() {	
		this.getCommand("Media").setExecutor(new Media());

		
	}

	public void onDisable() {
		
	}
		}
		




*/
package me.ice.ASMP2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	ArrayList<Event> events = new ArrayList<Event>();
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
		new CivilizationType("Barbarian", "DEATH"),
// 		LATER DEFAULT
//		new CivilizationType("Atlantic", "ATLA"), 
//		new CivilizationType("Taskmaster", "TASK"), 
//		new CivilizationType("Dwarven", "DWAR"), 
//		new CivilizationType("Traveler", "TRAV"), 

//		DLC
//		new CivilizationType("Necromancer", "NCRO"), 
//		new CivilizationType("Ranger", "RANG"), 
//		new CivilizationType("Transportation", "UBER"), 
	};
	


	@Override
	public void onEnable() {
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
				checkForPeople();
				for (int x = 0; x < serverInfo.civilizations.size(); x++) {
					civilizationCheck(x);
				}
			}
		}, 0, 20 * 60);
		
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
	Civilization civilizationFromIndex(int index) {
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
		 p.sendMessage(ChatColor.GREEN + "You have joined the " + civilizationFromIndex(index) + " civilization!");		
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (label.equalsIgnoreCase("civaccept")) {
			 for (Event e : events) {
				 if (e.effected == p && e.milliLength + e.timeStarted > System.currentTimeMillis()) {
					 joinCivilization(p, e.civIndex);
				 }
			 }
		}
		if (label.equalsIgnoreCase("civdeny")) {
			removePerson(p);
		}

		if (label.equalsIgnoreCase("civinvite")) {
			if (args.length == 1) {
				Player on = Bukkit.getPlayer(args[0]);
				if (on == null) {
					sender.sendMessage(ChatColor.RED + "Player name is either misspelled or player is not online");
				}
				else {
					int index = getPlayerIndex(p);
					if (index == -1) {
						sender.sendMessage(ChatColor.RED + "You are not in a civilization!");					
					}
					else {
						int civindex = serverInfo.indexOfCivilization.get(index);
						Civilization civ = serverInfo.civilizations.get(civindex);
						sender.sendMessage(ChatColor.GREEN + "Invite sent!");
						on.sendMessage("You have been invited to " + civ.toString() + " by " + p.getName());
						on.sendMessage("Reply with /civaccept (yes) or /civdeny (no) within 60 seconds");
						addEvent(new Event(on, civindex, 60000));
					}					
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Please run the command again but with a player name");
			}
		}
		
		if (label.equalsIgnoreCase("civcreate")) {
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
					if (index == -1 || !civilizationFromIndex(index).leader.equals(p.getUniqueId())) {
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
		if (label.equalsIgnoreCase("civdisband")) {
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

		if (label.equalsIgnoreCase("civkick")) {
			if (args.length == 1) {
				int yourIndex = getPlayerIndex(p);
				if (yourIndex == -1) {
					p.sendMessage(ChatColor.RED + "You are not in a civilization!");					
				}
				else {
					Civilization c = civilizationFromIndex(yourIndex);
					if (c.leader.equals(p.getUniqueId())) {
						Player gonnaGetKicked = Bukkit.getPlayer(args[0]);
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
		
		if (label.equalsIgnoreCase("civlist")) {
			for (Civilization c : serverInfo.civilizations) {
				p.sendMessage(c.toString());
			}
		}

		
		if (label.equalsIgnoreCase("civtypes")) {
			for (CivilizationType ct : types) {
				p.sendMessage(ct.getName());
			}
		}
		
		if (label.equalsIgnoreCase("civinfo")) {
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
		
		if (label.equalsIgnoreCase("civrename")) {
			if (args.length == 1) {
				int index = getPlayerIndex(p);
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Can't rename a civilization you aren't in!");
				}
				else {
					Civilization c = civilizationFromIndex(index);
					if (c.leader.equals(p.getUniqueId())) {
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

		if (label.equalsIgnoreCase("civchangecolor")) {
			if (args.length == 1) {
				int index = getPlayerIndex(p);	
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Can't change color of a civilization you aren't in!");
				}
				else {
					Civilization c = civilizationFromIndex(index);
					if (c.leader.equals(p.getUniqueId())) {
						for (ChatColor color : ChatColor.values()) {
							if (args[0].toLowerCase().equals(color.name().toLowerCase())) {
								p.sendMessage(ChatColor.GREEN + "Color changed from " + c.cc + c.cc.name() + ChatColor.GREEN + " to " + color + color.name());
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

		if (label.equalsIgnoreCase("civcolors")) {
			for (ChatColor cc : ChatColor.values()) {
				p.sendMessage(cc + cc.name());
			}
		}
		
		if (label.equalsIgnoreCase("civleave")) {
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

		if (label.equalsIgnoreCase("civtransfer")) {
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
		if (label.equalsIgnoreCase("civtester")) {
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
		
		if (label.equalsIgnoreCase("civclear")) {
			// TODO: Remove this temp ass Icegod shit
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
		if (label.equalsIgnoreCase("civLevelUp")) {
			if (args.length == 0) {
				int index = getPlayerIndex(p);
				if (index == -1) {
					p.sendMessage(ChatColor.RED + "Give the name of the civilization that you want to level up!");				
				}				
			}
			else if (args.length == 1) {
				Civilization c = civilizationFromName(args[0]);
				if (c == null) {
					p.sendMessage(ChatColor.RED + args[0] + " is not the name of a civilization!");
				}
				else {
					c.level = c.level +1;
				}
			}
			else {
				p.sendMessage(ChatColor.RED + "Too many arguments!");
			}
		}
		
		//if (label.equalsIgnoreCase("civLevelUp")) {
		//	int civindex = serverInfo.indexOfCivilization.get(civindex);
		//	Civilization civ = serverInfo.civilizations.get(civindex);
		//	civ.level = civ.level +1;
			
			
		//}
		if (label.equalsIgnoreCase("civAbil1")) {
			int index  = getPlayerIndex(p);
			if (index == -1) {
				sender.sendMessage("Look at this loser who isn't in a civilization");
			}
			else if (index == 1) {
				int i2 = serverInfo.indexOfCivilization.get(index);
				Civilization c = serverInfo.civilizations.get(i2);
				int civindex = serverInfo.indexOfCivilization.get(index);
				Civilization civ = serverInfo.civilizations.get(civindex);
				if (civ.toString () == "Produce") {
					if (civ.level >= 2 ) {
						sender.sendMessage("insert power here");
					}
				if (civ.toString() == "Technology") {	
					if (civ.level >= 2 ) {
						sender.sendMessage("insert power here");
					}
				}
				if (civ.toString() == "Construction") {	
					if (civ.level >= 2 ) {
						sender.sendMessage("insert power here");
					}
				}

				/*	if(ProdLevel >= 1) {
						sender.sendMessage("insertabilityhere");
				}
				if (civ.toString() == "Technology"){
					if (TechLevel >= 1) {
					sender.sendMessage("Insertabilityhere");
					}
				}
				if (civ.toString() == "Construction"){
					if (TechLevel >= 1) {
					sender.sendMessage("Insertabilityhere");
					}
					*/
				}
					else {
						sender.sendMessage("Man your civ sucks");
					}
									}
			}
			
		
		if (label.equalsIgnoreCase("civmedia")) {
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
		p.sendMessage("Leader: " + Bukkit.getOfflinePlayer(c.leader).getName());
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
}
