package me.ice.ASMP2;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.ChatColor;


public class Civilization implements Serializable{
<<<<<<< Updated upstream
	 CivilizationType type;
=======
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	CivilizationType type;
>>>>>>> Stashed changes
	String name;
	UUID leader;
	ChatColor cc = ChatColor.WHITE;
	int level = 1;
	
	Civilization(CivilizationType t, String n, UUID l) {
		type = t;
		name = n;
		leader = l;
	}

	public String toString() {
		return cc + "[" + type.nickname + "]" + " " + name;
	}
}
