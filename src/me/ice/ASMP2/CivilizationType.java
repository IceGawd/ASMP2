package me.ice.ASMP2;

public class CivilizationType {
	String name;
	String nickname;
	
	CivilizationType(String a, String i) {
		name = a;
		nickname = i;
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
