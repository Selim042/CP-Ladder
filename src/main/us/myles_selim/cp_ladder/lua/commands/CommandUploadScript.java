package us.myles_selim.cp_ladder.lua.commands;

import java.io.File;
import java.util.Set;

import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommand;
import us.myles_selim.cp_ladder.lua.LuaUtils;
import us.myles_selim.cp_ladder.lua.ScriptManager;

public class CommandUploadScript extends JavaCommand {

	public CommandUploadScript() {
		super("uploadScript", "Uploads a Lua script to Starota.");
	}

	@Override
	public Permission requiredUsePermission() {
		return Permission.ADMINISTRATOR;
	}

	@Override
	public String getGeneralUsage() {
		return "[event/command/remove]";
	}

	@Override
	public void execute(String[] args, Message message, Guild server, Channel channel) {
		if (args.length < 2) {
			((TextChannel) channel)
					.createMessage("**Usage**: " + "." + getName() + " " + getGeneralUsage());
			return;
		}
		boolean uploaded = false;
		switch (args[1].toLowerCase()) {
		case "event":
		case "eventhandler":
		case "e":
			LuaUtils.clearEventHandlers(server);
			uploaded = ScriptManager.saveScript(server, "eventHandler" + ScriptManager.LUA_EXENSION,
					getAttachment((TextChannel) channel, message));
			break;
		case "command":
		case "cmd":
		case "c":
			if (args.length < 3) {
				((TextChannel) channel)
						.createMessage("**Usage**: " + "." + getName() + " command [cmdName]");
				return;
			}
			uploaded = ScriptManager.saveScript(server,
					"commands" + File.separator + args[2] + ScriptManager.LUA_EXENSION,
					getAttachment((TextChannel) channel, message));
			break;
		case "remove":
		case "delete":
		case "rem":
		case "del":
			if (args.length < 3) {
				((TextChannel) channel)
						.createMessage("**Usage**: " + "." + getName() + " remove [event/cmdName]");
				return;
			}
			String scriptName;
			switch (args[2].toLowerCase()) {
			case "event":
			case "eventHandler":
				scriptName = "eventHandler";
				break;
			default:
				scriptName = "commands" + File.separator + args[2];
				break;
			}
			if (ScriptManager.removeScript(server, scriptName + ScriptManager.LUA_EXENSION)) {
				((TextChannel) channel)
						.createMessage("Successfully deleted script \"" + scriptName + "\"");
				if (scriptName.equalsIgnoreCase("eventHandler"))
					LuaUtils.clearEventHandlers(server);
				return;
			} else {
				((TextChannel) channel).createMessage("Failed to remove script \"" + scriptName + "\"");
				return;
			}
		default:
			((TextChannel) channel)
					.createMessage("**Usage**: " + "." + getName() + " " + getGeneralUsage());
			return;
		}
		if (uploaded) {
			((TextChannel) channel).createMessage("Saved your new script");
			ScriptManager.executeEventScript(server);
		} else
			((TextChannel) channel).createMessage("Failed to save your script");
	}

	private Attachment getAttachment(TextChannel channel, Message message) {
		Set<Attachment> attachS = message.getAttachments();
		if (attachS == null || attachS.size() != 1) {
			channel.createMessage("Only uploading one script at a time is supported");
			return null;
		}
		Attachment attach = (Attachment) attachS.toArray(new Object[0])[0];
		if (!attach.getFilename().toLowerCase().endsWith(ScriptManager.LUA_EXENSION)) {
			channel.createMessage("Only `.lua` files are accepted");
			return null;
		}
		return attach;
	}

}
