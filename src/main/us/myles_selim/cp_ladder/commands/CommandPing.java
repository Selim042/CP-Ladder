package us.myles_selim.cp_ladder.commands;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import us.myles_selim.cp_ladder.CPLadder;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommand;

public class CommandPing extends JavaCommand {

	public CommandPing() {
		super("ping", "Gets " + CPLadder.BOT_NAME + "s approx. ping to the Discord API.");
	}

	@Override
	public PermissionSet getCommandPermissions() {
		return PermissionSet.of(Permission.SEND_MESSAGES);
	}

	@Override
	public void execute(String[] args, Message message, Guild server, Channel channel) {
		Message msg = ((TextChannel) channel).createMessage("Pong!").block();
		msg.edit((e) -> e.setContent("Pong! ("
				+ (msg.getTimestamp().toEpochMilli() - message.getTimestamp().toEpochMilli()) + "ms)"));
	}

}
