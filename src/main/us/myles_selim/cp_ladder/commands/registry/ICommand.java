package us.myles_selim.cp_ladder.commands.registry;

import java.util.List;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;

public interface ICommand extends Comparable<ICommand> {

	void setCategory(String category);

	public String getCategory();

	public String getDescription();

	public Permission requiredUsePermission();

	public PermissionSet getCommandPermissions();

	/**
	 * @deprecated Override, don't call. Use
	 *             {@link ICommand#hasRequiredRole(IGuild, User)} instead.
	 */
	@Deprecated
	public Role requiredRole(Guild guild);

	public default boolean hasRequiredRole(Guild guild, User user) {
		Role reqRole = requiredRole(guild);
		return reqRole == null || user.asMember(guild.getId()).block().getRoles().collectList().block()
				.contains(reqRole);
	}

	/**
	 * @deprecated Override, don't call. Use
	 *             {@link ICommand#isRequiredChannel(IGuild, User)} instead.
	 */
	@Deprecated
	public Channel requiredChannel(Guild guild);

	public default boolean isRequiredChannel(Guild guild, Channel ch) {
		Channel reqCh = requiredChannel(guild);
		return reqCh == null || requiredChannel(guild).equals(ch);
	}

	public String getName();

	public List<String> getAliases();

	public String getGeneralUsage();

	public String getAdminUsage();

	public void execute(String[] args, Message message, Guild guild, Channel channel) throws Exception;

	public void setCommandHandler(ICommandHandler handler);

	public ICommandHandler getCommandHandler();

}
