package us.myles_selim.cp_ladder.commands;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import us.myles_selim.cp_ladder.CPLadder;
import us.myles_selim.cp_ladder.EmbedBuilder;
import us.myles_selim.cp_ladder.StarotaConstants;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommand;

public class CommandCredits extends JavaCommand {

	public CommandCredits() {
		super("credits", "Displays the credits for " + CPLadder.BOT_NAME + ".");
	}

	@Override
	public PermissionSet getCommandPermissions() {
		return PermissionSet.of(Permission.SEND_MESSAGES, Permission.EMBED_LINKS);
	}

	@Override
	public void execute(String[] args, Message message, Guild guild, Channel channel) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.withTitle("Credits for " + CPLadder.BOT_NAME + " v" + StarotaConstants.VERSION);

		builder.appendDesc("**General Development**: Selim_042: [GitHub](http://github.com/Selim042) | "
				+ "[Twitter](http://twitter.com/Selim_042)\n");
		builder.appendDesc("**Discord4J**: [austinv11](https://github.com/austinv11)\n");

		((TextChannel) channel).createEmbed(builder.build());
	}

}
