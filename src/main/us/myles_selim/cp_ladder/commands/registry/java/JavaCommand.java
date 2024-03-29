package us.myles_selim.cp_ladder.commands.registry.java;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import us.myles_selim.cp_ladder.commands.registry.ICommand;
import us.myles_selim.cp_ladder.commands.registry.ICommandHandler;
import us.myles_selim.cp_ladder.commands.registry.PrimaryCommandHandler;

public class JavaCommand implements ICommand {

	private final String name;
	private final String description;
	private String category;
	private ICommandHandler handler;

	public JavaCommand(String name) {
		this(name, null);
	}

	public JavaCommand(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public Permission requiredUsePermission() {
		return null;
	}

	@Override
	public PermissionSet getCommandPermissions() {
		return PermissionSet.of(Permission.SEND_MESSAGES);
	}

	@Override
	public Role requiredRole(Guild guild) {
		return null;
	}

	@Override
	public Channel requiredChannel(Guild guild) {
		return null;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(this.name);
		return aliases;
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
	public void execute(String[] args, Message message, Guild guild, Channel channel) throws Exception {}

	@Override
	public int compareTo(ICommand o) {
		if (this.category.equals(o.getCategory()))
			return name.compareTo(o.getName());
		return this.category.compareTo(o.getCategory());
	}

	protected void sendUsage(String prefix, Channel channel) {
		sendUsage(prefix, channel, false);
	}

	protected void sendUsage(String prefix, Channel channel, boolean adminUsage) {
		if (adminUsage)
			((TextChannel) channel).createMessage(
					String.format("**Admin Usage**: %s%s %s", prefix, getName(), getAdminUsage()));
		else
			((TextChannel) channel).createMessage(
					String.format("**Usage**: %s%s %s", prefix, getName(), getGeneralUsage()));
	}

	@Override
	public final void setCategory(String category) {
		if (this.category == null)
			this.category = category;
	}

	@Override
	public final String getCategory() {
		if (this.category == null)
			return PrimaryCommandHandler.DEFAULT_CATEGORY;
		return this.category;
	}

	@Override
	public final void setCommandHandler(ICommandHandler handler) {
		if (this.handler == null)
			this.handler = handler;
	}

	@Override
	public final ICommandHandler getCommandHandler() {
		return this.handler;
	}

}
