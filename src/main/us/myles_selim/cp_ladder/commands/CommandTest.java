package us.myles_selim.cp_ladder.commands;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommand;

public class CommandTest extends JavaCommand {

	public CommandTest() {
		super("test");
	}

	@Override
	public void execute(String[] args, Message message, Guild server, Channel channel) {

	}

}
