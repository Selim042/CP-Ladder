package us.myles_selim.cp_ladder.commands.selim_pm;

import java.util.Collections;
import java.util.List;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import us.myles_selim.cp_ladder.CPLadder;
import us.myles_selim.cp_ladder.LadderConstants;
import us.myles_selim.cp_ladder.commands.CommandPing;
import us.myles_selim.cp_ladder.commands.registry.CommandHelp;
import us.myles_selim.cp_ladder.commands.registry.ICommand;
import us.myles_selim.cp_ladder.commands.registry.ICommandHandler;
import us.myles_selim.cp_ladder.commands.registry.PrimaryCommandHandler;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommand;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommandHandler;

public class SelimPMCommandHandler {

	private static PrimaryCommandHandler INSTANCE;
	private static JavaCommandHandler JAVA_HANDLER;

	private static boolean inited = false;

	@SuppressWarnings("deprecation")
	public static void init() {
		if (inited && CPLadder.FULLY_STARTED)
			return;
		inited = true;

		INSTANCE = new PrimaryCommandHandler(CPLadder.getClient(), (Channel c) -> isSelimPM(c));
		INSTANCE.setup(CPLadder.getClient().getEventDispatcher());
		JAVA_HANDLER = new JavaCommandHandler(CPLadder.getClient());
		INSTANCE.registerCommandHandler(JAVA_HANDLER);

		registerCommand(new CommandPing());

		registerCommand("Help", new CommandHelp());
	}

	// private final List<JavaCommand> COMMANDS = new CopyOnWriteArrayList<>();
	// private final List<String> CATEGORIES = new CopyOnWriteArrayList<>();
	//

	private SelimPMCommandHandler() {}

	@SuppressWarnings("unused")
	private static void registerCommand(JavaCommand cmd) {
		registerCommand(PrimaryCommandHandler.DEFAULT_CATEGORY, cmd);
	}

	private static void registerCommand(String category, JavaCommand cmd) {
		JAVA_HANDLER.registerCommand(category, cmd);
	}

	public static boolean isSelimPM(Channel channel) {
		if (channel instanceof PrivateChannel
				&& ((PrivateChannel) channel).getRecipientIds().contains(LadderConstants.SELIM_USER_ID))
			return true;
		return false;
	}

	protected abstract static class SelimPMCommand implements ICommand {

		private final String name;
		private String category;

		public SelimPMCommand(String name) {
			this.name = name;
		}

		@Override
		public final int compareTo(ICommand o) {
			if (this.category.equals(o.getCategory()))
				return name.compareTo(o.getName());
			return this.category.compareTo(o.getCategory());
		}

		@Override
		public final void setCategory(String category) {
			this.category = category;
		}

		@Override
		public final String getCategory() {
			return this.category;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public final Permission requiredUsePermission() {
			return null;
		}

		@Override
		public final Role requiredRole(Guild guild) {
			return null;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public List<String> getAliases() {
			return Collections.singletonList(getName());
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
		public abstract void execute(String[] args, Message message, Guild guild, Channel channel)
				throws Exception;

		@Override
		public final void setCommandHandler(ICommandHandler handler) {}

		@Override
		public final ICommandHandler getCommandHandler() {
			return JAVA_HANDLER;
		}

	}

}
