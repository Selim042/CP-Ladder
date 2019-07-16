package us.myles_selim.cp_ladder.lua.libraries;

import org.squiddev.cobalt.LuaState;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaValue;
import org.squiddev.cobalt.lib.LuaLibrary;

import discord4j.core.object.entity.Guild;
import us.myles_selim.cp_ladder.lua.conversion.ConversionHandler;

public class DiscordLib implements LuaLibrary {

	private final Guild server;

	public DiscordLib(Guild server) {
		this.server = server;
	}

	public Guild getGuild() {
		return server;
	}

	@Override
	public LuaValue add(LuaState state, LuaTable env) {
		// env.rawset("_DISCORD4J", ValueFactory.valueOf(Discord4J.VERSION));
		env.rawset("discord", ConversionHandler.convertToLua(state, getGuild()));
		return env;
	}

}
