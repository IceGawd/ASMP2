package me.ice.ASMP2;

public class Civilization {
	CivilizationType type;
	String name;
	int level = 1;
	
	Civilization(CivilizationType t, String n) {
		type = t;
		name = n;
	}

	@Override
	public String toString() {
		return "[" + type.nickname + "]" + " " + name;
	}
}
