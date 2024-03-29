package us.myles_selim.cp_ladder.lua.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import us.myles_selim.cp_ladder.EventListener;
import us.myles_selim.cp_ladder.commands.registry.ICommand;
import us.myles_selim.cp_ladder.commands.registry.ICommandHandler;
import us.myles_selim.cp_ladder.lua.ScriptManager;

public class LuaCommandHandler implements ICommandHandler, EventListener {

	private static final String CATEGORY = "Lua";

	private final DiscordClient client;

	public LuaCommandHandler(DiscordClient client) {
		this.client = client;
	}

	@Override
	public boolean executeCommand(String[] args, Message message, Guild guild, Channel channel)
			throws Exception {
		if (args.length < 1)
			return false;
		ICommand cmd = findCommand(guild, message, args[0]);
		if (cmd == null)
			return false;
		cmd.execute(args, message, guild, channel);
		return true;
	}

	@Override
	public List<ICommand> getAllCommands(Guild guild) {
		List<ICommand> ret = new ArrayList<>();
		for (String n : ScriptManager.getCommandScripts(guild))
			ret.add(new LuaCommand(guild, n));
		return Collections.unmodifiableList(ret);
	}

	@Override
	public List<ICommand> getCommandsByCategory(Guild server, String category) {
		return getAllCommands(server);
	}

	@Override
	public ICommand findCommand(Guild server, Message message, String name) {
		for (ICommand c : getAllCommands(server))
			if (c.getName().equalsIgnoreCase(name))
				return c;
		return null;
	}

	@Override
	public List<String> getAllCategories(Guild server) {
		return Collections.singletonList(CATEGORY);
	}

	@Override
	public DiscordClient getDiscordClient() {
		return client;
	}

	private class LuaCommand implements ICommand {

		private final Guild server;
		private final String name;

		public LuaCommand(Guild server, String name) {
			this.server = server;
			this.name = name;
		}

		@Override
		public void execute(String[] args, Message message, Guild guild, Channel channel)
				throws Exception {
			ScriptManager.executeCommandScript(this.server, this.name, message, (TextChannel) channel,
					args);
		}

		@Override
		public int compareTo(ICommand o) {
			if (CATEGORY.equals(o.getCategory()))
				return name.compareTo(o.getName());
			return CATEGORY.compareTo(o.getCategory());
		}

		@Override
		public void setCategory(String category) {}

		@Override
		public String getCategory() {
			return CATEGORY;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public Permission requiredUsePermission() {
			return null;
		}

		@Override
		public PermissionSet getCommandPermissions() {
			return null;
		}

		@Override
		public Role requiredRole(Guild guild) {
			return null;
		}

		@Override
		public TextChannel requiredChannel(Guild guild) {
			return null;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public List<String> getAliases() {
			return Collections.singletonList(this.name);
		}

		@Override
		public String getGeneralUsage() {
			return null;
		}

		@Override
		public String getAdminUsage() {
			return null;
		}

		@Override
		public void setCommandHandler(ICommandHandler handler) {}

		@Override
		public final ICommandHandler getCommandHandler() {
			return LuaCommandHandler.this;
		}
	}

}
