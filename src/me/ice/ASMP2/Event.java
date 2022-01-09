package me.ice.ASMP2;

import org.bukkit.entity.Player;

public class Event {
	long timeStarted;
	int milliLength;
	Player effected;
	int civIndex;
	
	Event(Player p, int c, int m) {
		effected = p;
		civIndex = c;
		milliLength = m;
		timeStarted = System.currentTimeMillis();
	}
}
