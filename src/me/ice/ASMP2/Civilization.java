package me.ice.ASMP2;

import java.util.UUID;

import org.bukkit.ChatColor;

public class Civilization {
	CivilizationType type;
	String name;
	UUID leader;
	ChatColor cc = ChatColor.WHITE;
	int level = 1;
	
	Civilization(CivilizationType t, String n, UUID l) {
		type = t;
		name = n;
		leader = l;
	}

	@Override
	public String toString() {
		return "[" + type.nickname + "]" + " " + name;
	}
}
