package me.ice.ASMP2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("serial")
public class InfoToSave implements Serializable {
	// CONNECTED 
	ArrayList<UUID> playersWhoHaveJoined = new ArrayList<UUID>();
	ArrayList<Integer> indexOfCivilization = new ArrayList<Integer>();

	ArrayList<Civilization> civilizations = new ArrayList<Civilization>();
}
