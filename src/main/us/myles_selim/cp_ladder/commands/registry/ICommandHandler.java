package us.myles_selim.cp_ladder.commands.registry;

import java.util.List;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;

public interface ICommandHandler {

	public boolean executeCommand(String[] args, Message message, Guild guild, Channel channel)
			throws Exception;

	public List<ICommand> getAllCommands(Guild server);

	public List<ICommand> getCommandsByCategory(Guild server, String category);

	public List<String> getAllCategories(Guild server);

	public ICommand findCommand(Guild server, Message msg, String name);

	public DiscordClient getDiscordClient();

}
