package me.ice.ASMP2;

import org.bukkit.ChatColor;

public class CivilizationType {
	String name;
	String nickname;
	ChatColor color;
	
	CivilizationType(String a, String i, ChatColor c) {
		name = a;
		nickname = i;
		color = c;
	}
	
	String getName() {
		return "[" + nickname + "]" + " " + name;
	}
	
	@Override
	public boolean equals(Object o) {
		
		// If the object is compared with itself then return true 
		if (o == this) {
			return true;
		}
		if (o instanceof String) {
			String s = (String) o;
			return s.equals(name) || s.equals(nickname);
		}
		return false;
	}
}
