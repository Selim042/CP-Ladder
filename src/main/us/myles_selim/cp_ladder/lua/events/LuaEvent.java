package us.myles_selim.cp_ladder.lua.events;

import org.squiddev.cobalt.LuaState;
import org.squiddev.cobalt.LuaValue;

import discord4j.core.object.entity.Guild;

public abstract class LuaEvent {

	private final Guild server;

	public LuaEvent(Guild server) {
		this.server = server;
	}

	public Guild getGuild() {
		return this.server;
	}

	public abstract LuaValue toLua(LuaState state);

}
