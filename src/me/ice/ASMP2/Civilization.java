package me.ice.ASMP2;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.ChatColor;

@SuppressWarnings("serial")
public class Civilization implements Serializable{
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
		return cc + "[" + type.nickname + "]" + " " + name;
	}
}