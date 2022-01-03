package me.ice.ASMP2;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override 
	public void onEnable() {	
		this.getCommand("Media").setExecutor(new Media());

		
	}

	public void onDisable() {
		
	}
		}
		




