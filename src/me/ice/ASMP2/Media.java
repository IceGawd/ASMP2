package me.ice.ASMP2;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Media implements CommandExecutor {


//this will allow users who use the command to see a message with an embedded link that sends them to AbstractCells, the channel, and possibly discord later
	public Media() {
		// TODO Auto-generated constructor stub
	}

	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {

		//this is for when the sender is not a player, hence the response
		if (sender instanceof Player) {
			sender.sendMessage("Insert funny advertisement: https://www.youtube.com/channel/UCyjCHH2D84uysw-qrzENfvw");
		}
	return true;
	
	}
	}
